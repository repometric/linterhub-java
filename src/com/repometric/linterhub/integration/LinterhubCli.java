package com.repometric.linterhub.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

class LinterhubCli {
    private LinterhubArgs args;
    private String cliRoot;

    /**
     * Constructor
     * @param cliRoot Directory where extension can find Linterhub Cli
     * @param mode Describes how to run Cli
     */
    LinterhubCli(String cliRoot, LinterhubMode mode) {
        this.args = new LinterhubArgs(cliRoot, mode);
        this.cliRoot = cliRoot;
    }

    /**
     * Function that execute command (used to communicate with cli)
     * @param params Instance of ExecuteParams
     * @return Returns stdout
     */
    static String executeChildProcess(ExecuteParams params){
        String res = "";
        Vector<String> commandParams = new Vector<>();
        if(params.prefix != null) commandParams.add(params.prefix);
        commandParams.add(params.command);
        commandParams.addAll(params.params);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandParams);
        if(params.WorkingDirectory != null) builder.directory(new File(params.WorkingDirectory));
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                res += line + System.getProperty("line.separator");
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            return null;
        }
        return res;
    }

    private String execute(ExecuteParams params){
        // TODO: Return ChildProcess in order to stop analysis when document is closed
        System.out.println("Execute command: " + params.generateString());
        params.WorkingDirectory = this.cliRoot;
        return LinterhubCli.executeChildProcess(params);
    }
    String analyze(){
        return this.execute(this.args.analyze());
    }

    String analyzeFile(String file){
        return this.execute(this.args.analyzeFile(file));
    }

    String catalog(){
        return this.execute(this.args.catalog());
    }

    String activate(String linter) {
        return this.execute(this.args.activate(linter));
    }

    String ignoreWarning(Types.IgnoreWarningParams params){
        return this.execute(this.args.ignoreWarning(params));
    }

    String linterVersion(String linter, boolean install){
        return this.execute(this.args.linterVersion(linter, install));
    }

    String deactivate(String linter) {
        return this.execute(this.args.deactivate(linter));
    }
    String version() {
        return this.execute(this.args.version());
    }
}