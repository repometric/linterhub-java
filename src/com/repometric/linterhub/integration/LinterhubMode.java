package com.repometric.linterhub.integration;

public enum LinterhubMode {
    /**
     * Using 'dotnet' command
     */
    DOTNET,
    /**
     * Just run dll
     */
    NATIVE,
    /**
     * Run linterhub in docker
     */
    DOCKER
}
