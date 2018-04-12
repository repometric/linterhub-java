package com.repometric.linterhub.integration;

import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;

class Status {
    void update(boolean progress, String text)
    {
        for (IdeFrame frame : WindowManager.getInstance().getAllProjectFrames()) {
            frame.getStatusBar().setInfo("Linterhub: " + text);
        }
        System.out.println("Progress: " + String.valueOf(progress) + ". " + text);
    }
}
