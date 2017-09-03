package com.lvonce.hermes;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.nio.file.FileSystems;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.function.Consumer;
import java.io.File;

public class FileWatcherNext {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherNext.class);
    private static final int POLL_INTERVAL = 200; // 200 ms
    private final Path watchDirPath;
    private final WatchService watchService;
    private final Consumer<File> handleFunc;
    private Map<String, FileWatcherNext> childWatchers;

    public static FileWatcherNext create(String watchDirPath, Consumer<File> handleFunc, boolean recursive) {
        Path path = Paths.get(watchDirPath);
        File watchDirFile = path.toFile();
        if (!watchDirFile.exists()) {
            return null;
        }
        if (!watchDirFile.isDirectory()) {
            return null;
        }

        WatchService watchService = null;
        LinkedHashMap<String, FileWatcherNext> childWatchers = new LinkedHashMap<>();
        try {
            path = Paths.get(watchDirFile.getCanonicalPath());
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            logger.debug("register dir: {}", path.toString());
            if (recursive) {
                File[] childFiles = path.toFile().listFiles();
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        String childCanonicalPath = childFile.getCanonicalPath();
                        FileWatcherNext childWatcher = create(childCanonicalPath, handleFunc, true);
                        if (childWatcher != null) {
                            childWatchers.put(childCanonicalPath, childWatcher);
                        }
                    }
                }
            }
            if (childWatchers.size() == 0) {
                childWatchers = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        FileWatcherNext watcher = new FileWatcherNext(path, watchService, handleFunc, childWatchers);
        return watcher;
    }

    private void addChileWatcher(File childFile, boolean recursive) {
        try {
            if (childFile.exists() && childFile.isDirectory()) {
                String childCanonicalPath = childFile.getCanonicalPath();
                FileWatcherNext childWatcher = create(childCanonicalPath, handleFunc, recursive);
                if (childWatcher != null) {
                    if (this.childWatchers == null) {
                        this.childWatchers = new LinkedHashMap<String, FileWatcherNext>();
                    }
                    this.childWatchers.put(childCanonicalPath, childWatcher);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileWatcherNext(Path watchDirPath, WatchService watchService, Consumer<File> handleFunc,
            Map<String, FileWatcherNext> childWatchers) {
        this.handleFunc = handleFunc;
        this.watchDirPath = watchDirPath;
        this.watchService = watchService;
        this.childWatchers = childWatchers;
    }

    public void walk(boolean recursive) {
        walk(this.watchDirPath, this.handleFunc, recursive);
    }

    public static void walk(Path watchDirPath, Consumer<File> handleFunc, boolean recursive) {
        File[] childFiles = watchDirPath.toFile().listFiles();
        for (File childFile : childFiles) {
            if (childFile.isFile()) {
                handleFunc.accept(childFile);
            } else if (childFile.isDirectory() && recursive) {
                walk(childFile.toPath(), handleFunc, true);
            }
        }
    }

    public void poll(boolean recursive) {
        if (recursive && (this.childWatchers != null)) {
            for (Map.Entry<String, FileWatcherNext> entry : this.childWatchers.entrySet()) {
                FileWatcherNext watcher = entry.getValue();
                watcher.poll(true);
            }
        }

        WatchKey key = watchService.poll();
        if (key == null) {
            return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent<Path> e = (WatchEvent<Path>) event;
            Path filePath = e.context();
            if (filePath.startsWith(".") || filePath.startsWith("~") || filePath.endsWith(".") || filePath.endsWith("~")
                    || filePath.endsWith(".swp")) {
                continue;
            }

            WatchEvent.Kind<?> kind = event.kind();
            if (kind == OVERFLOW || kind == ENTRY_DELETE) {
                continue;
            }

            filePath = this.watchDirPath.resolve(filePath);
            logger.debug("poll detect updated file: {}", filePath);
            File file = filePath.toFile();
            if (!file.exists()) {
                continue;
            }
            if (kind == ENTRY_CREATE) {
                if (file.isDirectory()) {
                    this.addChileWatcher(file, true);
                    walk(file.toPath(), handleFunc, true);
                } else if (file.isFile()) {
                    this.handleFunc.accept(file);
                }
            }

            if (kind == ENTRY_MODIFY) {
                if (file.isFile()) {
                    this.handleFunc.accept(file);
                }
            }
        }
        key.reset();
    }

    public void watch() {
        new Thread(() -> {
            try {
                while (true) {
                    this.poll(true);
                    Thread.sleep(POLL_INTERVAL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
