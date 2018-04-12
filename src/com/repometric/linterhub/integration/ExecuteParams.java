package com.repometric.linterhub.integration;

import java.util.Vector;

class ExecuteParams
{
    String command;
    String prefix;
    String WorkingDirectory;
    Vector<String> params;

    ExecuteParams()
    {
        this.params = new Vector<>();
    }

    String generateString()
    {
        StringBuilder builder = new StringBuilder();
        for(String s : this.params) {
            builder.append(" ").append(s);
        }
        return prefix + " " + command + builder.toString();
    }
}