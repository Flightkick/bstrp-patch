package com.rgkuijt.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Main {
    //Set at startup
    private static Path userDir;
    private static Pattern pattern;

    //Options
    private static boolean verbose = false;
    private static boolean overwrite = false;

    public static void main(String[] args) {
        for (String s: args) {
            if (s.equals("-verbose") || s.equals("-v")) {
                verbose = true;
            }
            if (s.equals("-overwrite")) {
                overwrite = true;
            }
        }

        userDir = Paths.get(System.getProperty("user.dir"));
        pattern = Pattern.compile("(visible-(xs|sm|md|lg))([ \".,\\{])");

        DebugLog("Removing old output directory and files");
        DeleteDirectory(Paths.get(userDir.toString(), "bstrp-patch-output").toFile());

        ArrayList<Path> files = ListFiles();
        FindAndReplace(files);
        System.out.println("Done.");
    }

    private static ArrayList<Path> ListFiles() {
        ArrayList<Path> files = new ArrayList<>();
        try {
            DebugLog("Listing directories");
            Files.walk(userDir).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    files.add(filePath);
                    DebugLog(filePath.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    private static void FindAndReplace(ArrayList<Path> files) {
        Charset charset = StandardCharsets.UTF_8;
        files.forEach(file -> {
            String content = null;
            try {
                DebugLog("Processing file: " + file);
                content = new String(Files.readAllBytes(file), charset);
                if (pattern.matcher(content).find()) {
                    content = pattern.matcher(content).replaceAll("$1-block$3");
                    Path outputFile = Paths.get(userDir.toString(), "bstrp-patch-output", file.toString().replace(userDir.toString(), ""));
                    try {
                        DebugLog("Writing to disk (overwrite: " + overwrite + "): " + file);
                        Files.createDirectories(outputFile.getParent());
                        Files.write(overwrite ? file : outputFile, content.getBytes(charset));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static boolean DeleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for (File file : files) {
                    if (file.isDirectory()) {
                        DebugLog("Removing old output directory: " + file);
                        DeleteDirectory(file);
                    } else {
                        DebugLog("Removing old output file: " + file);
                        file.delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    private static void DebugLog(String line) {
        if (verbose) {
            System.out.println(line);
        }
    }
}
