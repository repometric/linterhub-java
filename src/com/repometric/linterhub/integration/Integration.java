package com.repometric.linterhub.integration;

import com.google.common.io.Files;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.TextRange;
import groovy.json.internal.Charsets;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Integration {
    private static String linterhub_version;
    private static LinterhubCli linterhub;
    public static String project = "";
    public static HashMap<String, String> checks = new HashMap<>();
    public static HashMap<String, Vector<Problem>> cache = new HashMap<>();
    private static Status status;
    private static boolean active = false;

    private static Settings settings;


    public static boolean isActive()
    {
        return active;
    }

    /**
     * Returns current settings
     */
    public static Settings getSettings() {
        return settings;
    }

    private static void initializeLinterhub() {
        System.out.println("Initialize Linterhub..");
        active = true;
        linterhub = new LinterhubCli(settings.cliPath, settings.mode);
    }

    public static void initialize(Settings settings, String version) {
        Integration.status = new Status();
        Integration.linterhub_version = version;
        Integration.settings = settings;
        if (settings.cliPath == null || settings.mode == null) {
            install();
        }
        else {
            File f = new File(Integration.settings.cliPath);
            if(!f.exists()) install();
            else initializeLinterhub();
        }
    }

    private static void saveConfig()
    {
        // TODO
    }

    private static void install() {
        new Thread(() -> {
            status.update(true, "Start install process..");
            LinterhubInstallation installation = new LinterhubInstallation();
            settings.mode = LinterhubMode.NATIVE;
            if(installation.getDotnetVersion() != null)
            {
                settings.mode = LinterhubMode.DOTNET;
            }
            System.out.println("Start download.");

            String result = installation.install(settings.mode, settings.cliRoot, status, linterhub_version);
            if(result == null) {
                System.err.println("Catch some errors");
                status.update(false, "Catch errors while installation");
            }
            else {
                status.update(false, "Active");
                settings.cliPath = result;
                initializeLinterhub();
                saveConfig();
            }
        }).start();
    }

    public static void initialize(Settings _settings)
    {
        settings = _settings;
        initializeLinterhub();
    }


    /**
     * Analyze project.
     * @return Vector of Problems
     */
    public static Vector<Problem> analyze(){
        if(!active) return null;
        System.out.println("Analyze project");
        status.update(true, "Analyzing project...");
        String data = linterhub.analyze();
        Vector<Problem> res = null;
        if(data != null){
            res = sendDiagnostics(data);
            System.out.println("Finish analyze project");
        }
        else
        {
            System.err.println("Catch error while analyzing project");
        }
        status.update(false, "Active");
        return res;
    }

    /**
     * Analyze single file.
     * @param path The relative path to file.
     * @return Vector of Problems
     */
    public static Vector<Problem> analyzeFile(String path) {
        if(!active) return null;
        System.out.println("Analyze file " + path);
        status.update(true, "Analyzing file...");
        String data = linterhub.analyzeFile(normalizePath(path));
        Vector<Problem> res = null;
        if(data != null){
            res = sendDiagnostics(data);
            System.out.println("Finish analyze file " + path);
        }
        else
        {
            System.err.println("Catch error while analyzing file " + path);
        }
        status.update(false, "Active");

        if(Integration.cache.get(path) != null)
        {
            Integration.cache.remove(path);
        }
        Integration.cache.put(path, res);

        return res;
    }

    private static String normalizePath(String path) // TODO test and fix
    {
        return path;
    }

    private static Vector<Problem> sendDiagnostics(String data) // TODO Convert to IDEA model
    {
        Vector<Problem> result = new Vector<>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(data);

            JSONArray linters = (JSONArray) obj;
            for (Object _linter : linters) {
                JSONObject linter = (JSONObject) _linter;
                JSONArray files = (JSONArray) ( (JSONObject) linter.get("Model") ).get("Files");
                for(Object _file : files) {
                    JSONObject file = (JSONObject) _file;
                    JSONArray errors = (JSONArray) file.get("Errors");
                    String content = Files.toString(new File(Paths.get(Integration.project,file.get("Path").toString()).toString()), Charsets.UTF_8);
                    char[] text = content.toCharArray();
                    for(Object _error : errors) {
                        JSONObject error = (JSONObject) _error;
                        Problem problem = new Problem();
                        problem.message = error.get("Message").toString();
                        problem.linter = linter.get("Name").toString();
                        switch(Integer.parseInt(error.get("Severity").toString()))
                        {
                            case 0:
                                problem.type = ProblemHighlightType.ERROR;
                                break;
                            case 1:
                                problem.type = ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
                                break;
                            default:
                                problem.type = ProblemHighlightType.INFORMATION;
                        }
                        problem.rule = ( (JSONObject) error.get("Rule") ).get("Name").toString();
                        int _line = Integer.parseInt(error.get("Line").toString()) - 1;
                        int _column_start = Integer.parseInt(( (JSONObject) error.get("Column") ).get("Start").toString()) - 1;
                        int _column_end = Integer.parseInt(( (JSONObject) error.get("Column") ).get("End").toString()) - 1;
                        int offset = 0;

                        // start of file
                        if (_line == 0) { // start of file errors
                            offset = _column_start;

                            // further search required
                        } else {
                            // start from end of cached data
                            int line = 0;

                            int column = 0;
                            for (int i = offset; i < text.length; ++i) {
                                final char character = text[i];

                                // for linefeeds we need to handle CR, LF and CRLF,
                                // hence we accept either and only trigger a new
                                // line on the LF of CRLF.
                                final char nextChar = (i + 1) < text.length ? text[i + 1] : '\0';
                                if (character == '\n' || character == '\r' && nextChar != '\n') {
                                    ++line;
                                    ++offset;
                                    column = 0;
                                } else {
                                    ++column;
                                    ++offset;
                                }

                                // need to go to end of line though
                                if (_line == line && _column_start == column) {
                                    break;
                                }
                            }
                        }
                        problem.range = new TextRange(offset, offset + (_column_end - _column_start));
                        result.add(problem);
                    }
                }
            }

        } catch (ParseException e) {
            System.err.println("Catch error while parsing catalog json");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Get linters catalog.
     * @return Vector of LinterResults
     */
    public static Vector<Types.LinterResult> catalog() {
        if(!active) return null;
        status.update(true, "Getting linters catalog..");
        String data = linterhub.catalog();
        System.out.println(data);
        Vector<Types.LinterResult> result = new Vector<>();
        if (data == null) {
            System.err.println("Catch error while requesting catalog");
        }
        else
        {
            JSONParser parser = new JSONParser();
            try {

                Object obj = parser.parse(data);

                JSONArray linters = (JSONArray) obj;
                for (Object _linter : linters) {
                    JSONObject linter = (JSONObject) _linter;
                    Types.LinterResult res = new Types.LinterResult();
                    res.description = linter.get("description").toString();
                    res.name = linter.get("name").toString();
                    res.languages = linter.get("languages").toString();
                    res.Active = Boolean.getBoolean(linter.get("active").toString());
                    result.add(res);
                }

            } catch (ParseException e) {
                System.err.println("Catch error while parsing catalog json");
                e.printStackTrace();
            }
        }
        status.update(false, "Active");
        return result;
    }

    /**
     * Activate linter.
     * @param name The linter name.
     */
    public static String activate(String name) {
        if(!active) return null;
        status.update(true, "Activating " + name + "...");
        if(linterhub.activate(name) == null)
        {
            System.err.println("Catch error while activating " + name);
        }
        status.update(false, "Active");
        return name;
    }

    /**
     * Ignore warning.
     * @param params Describes warning.
     */
    public static String ignoreWarning(Types.IgnoreWarningParams params) {
        if(!active) return null;
        String result = linterhub.ignoreWarning(params);
        if(result == null)
        {
            System.err.println("Catch error while sending ignore request ");
        }
        else
        {
            System.out.println("Rule added!");
        }
        return result;
    }

    /**
     * Get the linter version.
     * @param name The linter name.
     * @param install Install linter or not
     */
    public static Types.LinterVersionResult linterVersion(String name, boolean install) {
        if(!active) return null;
        status.update(true, "Getting linter version..");
        String data = linterhub.linterVersion(name, install);
        Types.LinterVersionResult result = null;
        if (data == null) {
            System.err.println("Catch error while requesting " + name + " version");
        }
        else
        {
            JSONParser parser = new JSONParser();
            try {

                Object obj = parser.parse(data);

                JSONObject jsonObject = (JSONObject) obj;

                result.LinterName = (String) jsonObject.get("LinterName");
                result.Installed = (boolean) jsonObject.get("Installed");
                result.Version = (String) jsonObject.get("Version");

            } catch (ParseException e) {
                System.err.println("Catch error while parsing " + name + " version");
                e.printStackTrace();
            }
        }
        status.update(false, "Active");
        return result;
    }

    /**
     * Deactivate linter.
     * @param name The linter name.
     */
    public static String deactivate(String name) {
        if(!active) return null;
        status.update(true, "Deactivating " + name + "...");
        if(linterhub.deactivate(name) == null)
        {
            System.err.println("Catch error while deactivating " + name);
        }
        status.update(false, "Active");
        return name;
    }

    /**
     * Get linterhub and other versions.
     */
    public static String version() {
        if(!active) return null;
        return linterhub.version();
    }
}
