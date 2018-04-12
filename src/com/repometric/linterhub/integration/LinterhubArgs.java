package com.repometric.linterhub.integration;

import java.nio.file.FileSystems;

class LinterhubArgs {

    private String cliRoot;
    private String cliPath;
    private LinterhubMode mode;
    private String prefix;

    /**
     * Constructor
     * @param cliRoot Directory where extension can find Linterhub Cli
     * @param mode Describes how to run Cli
     */
    LinterhubArgs(String cliRoot, LinterhubMode mode) {
        this.cliRoot = cliRoot;
        this.mode = mode;
        this.cliPath = this.getPrefixes()[1];
        this.prefix = this.getPrefixes()[0];
    }

    private String[] getPrefixes(){
        switch (this.mode) {
            case DOTNET:
                return new String[]{"dotnet", FileSystems.getDefault().getPath(this.cliRoot, "cli.dll").toString()};
            case NATIVE:
                return new String[]{null, FileSystems.getDefault().getPath(this.cliRoot, "cli").toString()};
            case DOCKER:
                return new String[]{null, null}; // TODO
        }

        return new String[]{null, null};
    }

    /**
     * Analyze whole project
     * @return Instance of ExecuteParams
     */
    ExecuteParams analyze() {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=analyze");
        ex.params.add("--project=" + Integration.project);

        return ex;
    }

    /**
     * Analyze single file
     * @param file Path to this file
     * @return Instance of ExecuteParams
     */
    ExecuteParams analyzeFile(String file) {
        // TODO: Improve this code.
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=analyze");
        ex.params.add("--project=" + Integration.project);
        String normalizedPath = file.replace("file://", "")
                .replace(Integration.project + '/', "")
                .replace(Integration.project + "\\", "");
        ex.params.add("--file=" + normalizedPath);

        return ex;
    }

    /**
     * Activate linter
     * @param linter Name of linter
     * @return Instance of ExecuteParams
     */
    ExecuteParams activate(String linter) {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=activate");
        ex.params.add("--project=" + Integration.project);
        ex.params.add("--active=true");
        ex.params.add("--linter=" + linter);

        return ex;
    }

    /**
     * Install or/and get linter version
     * @param linter Name of linter
     * @param install Try to install linter or not (need su)
     * @return Instance of ExecuteParams
     */
    ExecuteParams linterVersion(String linter, boolean install) {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add(install ? "--mode=LinterInstall" : "--mode=LinterVersion");
        ex.params.add("--linter=" + linter);
        return ex;
    }

    /**
     * Deactivate linter
     * @param linter Name of linter
     * @return Instance of ExecuteParams
     */
    ExecuteParams deactivate(String linter) {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=activate");
        ex.params.add("--project=" + Integration.project);
        ex.params.add("--active=false");
        ex.params.add("--linter=" + linter);
        return ex;
    }

    /**
     * Get list of available linters
     * @return Instance of ExecuteParams
     */
    ExecuteParams catalog() {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=catalog");
        ex.params.add("--project=" + Integration.project);
        return ex;
    }

    /**
     * Add ignore rule
     * @param params Describes warning.
     * @return Instance of ExecuteParams
     */
    ExecuteParams ignoreWarning(Types.IgnoreWarningParams params){
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=ignore");
        ex.params.add("--project=" + Integration.project);
        if (params.error != null) {
            ex.params.add("--error=" + params.error);
        }
        if (params.file != null) {
            ex.params.add("--file=" + params.file);
        }
        if (params.line != 0) {
            ex.params.add("--line=" + params.line);
        }
        return ex;
    }

    /**
     * Receive version of CLI, Linterhub etc
     * @return Instance of ExecuteParams
     */
    ExecuteParams version() {
        ExecuteParams ex = new ExecuteParams();
        ex.command = this.cliPath;
        ex.prefix = this.prefix;
        ex.params.add("--mode=version");
        return ex;
    }
}
