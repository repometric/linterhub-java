package com.repometric.linterhub.integration;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.TextRange;

public class Problem {
    public String message;
    public TextRange range;
    public ProblemHighlightType type;
    public String linter;
    public String rule;
}
