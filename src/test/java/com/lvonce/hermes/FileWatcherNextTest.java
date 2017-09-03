package com.lvonce.hermes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.function.Consumer;

public class FileWatcherNextTest {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherNextTest.class);
    private ArrayList<String> updateList = new ArrayList<>();

    public static int commandLine(String cmd) {
        try {
            logger.debug("commandLine:{}", cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            logger.debug("commandLine:{}, return {}", cmd, exitValue);
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean createFile(String pathString) {
        try {
            Path path = Paths.get(pathString);
            return path.toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createDir(String pathString) {
        Path path = Paths.get(pathString);
        return path.toFile().mkdirs();
    }

    public static boolean deleteDir(String pathString) {
        File file = Paths.get(pathString).toFile();
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File childFile : childFiles) {
                deleteDir(childFile.getPath().toString());
            }
            file.delete();
        } else {
            file.delete();
        }

        return true;
    }

    @Test
    public void test() {
        try {
            Consumer<File> consumer = (File file) -> {
                logger.info("update file {}", file.toPath().toString());
                updateList.add(file.toPath().toString());
            };
            Path basePath = Paths.get(new File("").getCanonicalPath());
            // commandLine("mkdir -p test_tmp");
            deleteDir(basePath.resolve("test_tmp").toString());
            createDir(basePath.resolve("test_tmp").toString());
            FileWatcherNext watcher = FileWatcherNext.create("test_tmp", consumer, true);
            // commandLine("touch test_tmp/hello.txt");
            createFile(basePath.resolve("test_tmp/hello.txt").toString());
            watcher.walk(true);
            assertTrue(updateList.get(updateList.size() - 1).endsWith("hello.txt"));

            watcher.watch();
            // commandLine("touch test_tmp/hello2.txt");
            logger.info("after watch()");
            createFile(basePath.resolve("test_tmp/hello2.txt").toString());
            Thread.sleep(2000);
            logger.info("2: {}", updateList.get(updateList.size() - 1));
            assertTrue(updateList.get(updateList.size() - 1).endsWith("hello2.txt"));

            // commandLine("mkdir -p test_tmp/test_tmp/test_tmp");
            // commandLine("touch test_tmp/test_tmp/test_tmp/hello3.txt");
            createDir(basePath.resolve("test_tmp/test_tmp/test_tmp").toString());
            createFile(basePath.resolve("test_tmp/test_tmp/test_tmp/hello3.txt").toString());
            Thread.sleep(2000);
            logger.info("3: {}", updateList.get(updateList.size() - 1));
            assertTrue(updateList.get(updateList.size() - 1).endsWith("hello3.txt"));
            // commandLine("rm -r test_tmp");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}