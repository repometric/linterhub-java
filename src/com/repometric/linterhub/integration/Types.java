package com.repometric.linterhub.integration;

public class Types {

    public static class NoParams {

    }

    public static class StatusParams {
        public String id;
        public Object State;
    }

    public static class ActivateParams {
        public boolean activate;
        public String linter;
    }

    public static class AnalyzeParams {
        public boolean full;
        public String path;
    }

    static class IgnoreWarningParams {
        String file;
        int line;
        String error;
    }

    public static class LinterVersionParams {
        public String linter;
    }

    public static class NoResult {
    }

    static class LinterResult {
        String name;
        String description;
        String languages;
        boolean Active;
    }

    static class LinterVersionResult {
        String LinterName;
        boolean Installed;
        String Version;
    }

    public static class CatalogResult {
        public LinterResult[] linters;
    }

    public static class InstallResult {
        public String path;
    }
}