package com.lvonce.hermes;

import java.io.File;
import java.util.Map;

public class OSValidator {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static String getOS() {
        if (isWindows()) {
            return "win";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "uni";
        } else if (isSolaris()) {
            return "sol";
        } else {
            return "err";
        }
    }

    public static String getSystemClassPath(String defaultClassPath) {
        String classPath = defaultClassPath;
        Map<String, String> envs = System.getenv();
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            String envKey = entry.getKey();
            envKey = envKey.toLowerCase();
            if (envKey.equals("classpath")) {
                if (OSValidator.isWindows()) {
                    classPath.replace("/", File.separator);
                    return classPath + ";" + entry.getValue();
                } else {
                    classPath.replace("\\", File.separator);
                    return classPath + ":" + entry.getValue();
                }
            }
        }
        return classPath;
    }
}
