package com.repometric.linterhub.integration;

import org.apache.commons.lang.SystemUtils;
import java.nio.file.FileSystems;

public class LinterhubPackage {
    private String version;
        private boolean is_native;
        private String folder;

        LinterhubPackage(String folder, boolean is_native, String version) {
            this.is_native = is_native;
            this.folder = folder;
            this.version = version;
        }

        public String getPackageVersion(){
            return this.version;
        }

        String getPackageName() {
            if (!this.is_native) {
                return "dotnet";
            }
            // TODO: Improve name conversion
            if (SystemUtils.IS_OS_MAC_OSX) {
                return "osx.10.11-x64";
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                return "win10-x64";
            }
            if (SystemUtils.IS_OS_LINUX) {
                return "debian.8-x64";
            }
            return "unknown";
        }

        String getPackageFullName() {
            return "linterhub-cli-" + this.getPackageName();
        }

        private String getPackageFileName() {
            return this.getPackageFullName() + ".zip";
        }

        String getPackageFullFileName() {
            return FileSystems.getDefault().getPath(this.folder, this.getPackageFileName()).toString();
        }

        String getPackageUrl() {
            String prefix = "https://github.com/Repometric/linterhub-cli/releases/download/";
            return prefix + this.version + "/" + this.getPackageFileName();
        }
}
