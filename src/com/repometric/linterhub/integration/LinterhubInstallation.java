package com.repometric.linterhub.integration;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;

public class LinterhubInstallation {

    String install(LinterhubMode mode, String folder, Status status, String version)
    {
        if(mode == LinterhubMode.DOCKER)
        {
            return this.downloadDock("repometric/linterhub-cli");
        }
        else
        {
            try {
                LinterhubPackage pack = new LinterhubPackage(folder, mode == LinterhubMode.NATIVE, version);
                System.out.println("Name: " + pack.getPackageFullName());

                URL url = new URL(pack.getPackageUrl());
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                long completeFileSize = httpConnection.getContentLength();

                java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
                java.io.FileOutputStream fos = new java.io.FileOutputStream(pack.getPackageFullFileName());
                java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                byte[] data = new byte[1024];
                long downloadedFileSize = 0;
                int x;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    downloadedFileSize += x;

                    // calculate progress
                    final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                    status.update(true, "Downloaded: " + currentProgress + "%");

                    bout.write(data, 0, x);
                }
                bout.close();
                in.close();

                try {
                    ZipFile zipFile = new ZipFile(pack.getPackageFullFileName());
                    zipFile.extractAll(folder);
                } catch (ZipException e) {
                    e.printStackTrace();
                }

                return FileSystems.getDefault().getPath(folder, "bin", pack.getPackageName()).toString();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * This function returns Docker version
     * @return Stdout of command
     */
    public String getDockerVersion() {
        ExecuteParams ex = new ExecuteParams();
        ex.command = "docker";
        ex.params.add("version");
        ex.params.add("--format='{{.Server.Version}}'");
        return removeNewLine(LinterhubCli.executeChildProcess(ex));
    }

    /**
     * This function returns Dotnet version
     * @return Stdout of command
     */
    String getDotnetVersion() {
        ExecuteParams ex = new ExecuteParams();
        ex.command = "dotnet";
        ex.params.add("--version");
        return removeNewLine(LinterhubCli.executeChildProcess(ex));
    }

    private String removeNewLine(String out) {
        if(out == null) return null;
        return out.replace('\n', '\0').replace('\r', '\0');
    }

    /**
     * Function downloads Docker Image
     * @param name Name of image to download
     * @return Stdout of command
     */
    private String downloadDock(String name) {
        ExecuteParams ex = new ExecuteParams();
        ex.command = "docker";
        ex.params.add("pull");
        ex.params.add(name);
        return LinterhubCli.executeChildProcess(ex);
    }
}
