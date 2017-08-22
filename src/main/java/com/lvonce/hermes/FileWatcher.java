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

public class FileWatcher {
	private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
	private static final Map<String, FileWatcher> fileWatchers = new LinkedHashMap<String, FileWatcher>();
	private static WatchService watchService = null;
	private static final int pollInterval = 1000; // ms
	
	private final String watchPath;
	private final FileWatcherHandler handler;
	private final Map<String, Long> fileTime; 
	
	public static synchronized FileWatcher createFileWatcher(String watchPath, FileWatcherHandler handler, boolean updateChild) {
		File file = new File(watchPath);
		if (!file.exists()) {
			logger.warn("try to register : "+ watchPath +" , but it is not existed!");
			return null;
		}
		if (!file.isDirectory()) {
			logger.warn("try to register file: "+ watchPath +" , please register directory instead!");
			return null;
		}
		if (watchService == null) {
			try {
				watchService = FileSystems.getDefault().newWatchService();
			} catch (IOException e) {
				watchService = null;
				e.printStackTrace();
			}
		}
		try {
			FileWatcher fileWatcher = new FileWatcher(file.getCanonicalPath(), handler);	
			fileWatcher.register();
			if (updateChild) {
				fileWatcher.updateChild();
			}
			return fileWatcher;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private FileWatcher(String watchPath, FileWatcherHandler handler) {
		this.watchPath = watchPath;
		this.handler = handler;	
		this.fileTime = new LinkedHashMap<String, Long>();
	}
	
	private void register() {
		try {
			logger.info("watch on: " + this.watchPath);
			Paths.get(this.watchPath).register(
				watchService, 
				ENTRY_CREATE,
				ENTRY_DELETE,
				ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getWatchFile(String fileName) {
		//logger.debug("getWatchFile("+fileName+")");
		File watchDir = Paths.get(this.watchPath).toFile();
		if (!watchDir.exists()) {
			return null;
		}
		File[] childFiles = watchDir.listFiles();
		for (File file : childFiles) {
			if (file.toString().endsWith(fileName)) {
				//logger.debug("found file: " + file.toString());
				return file;
			}
		}
		return null;
	}

	public void updateChild() {
		File[] childFiles = Paths.get(this.watchPath).toFile().listFiles();
		for (File childFile : childFiles) {
			if (childFile.isFile()) {
				this.handler.updateByFile(childFile);
			}
		}	
	}

	public synchronized void handleFile(File file) {
		try {
			if (file != null && file.isFile()) {
				String canonicalPath = file.getCanonicalPath();
				Long lastUpdateTime = this.fileTime.get(canonicalPath);
				if (lastUpdateTime == null || lastUpdateTime < file.lastModified()) {
					logger.debug("update file: " + canonicalPath + " " + file.lastModified());
					this.fileTime.put(canonicalPath, file.lastModified());
					this.handler.updateByFile(file);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}	
	
	public static synchronized void registerDir(String filePath, FileWatcherHandler handler, boolean updateChild) {
		logger.debug("registerDir("+filePath+")");
		File file = new File(filePath);
		if (!file.exists()) {
			logger.warn("registerDir : "+ filePath +" , but it is not existed!");
			return;
		}
		if (!file.isDirectory()) {
			logger.warn("registerDir "+ filePath +" , param is not directory, please register dir instead!");
			return;
		}

		try {
			String canonicalPath = file.getCanonicalPath();
			if (fileWatchers.containsKey(canonicalPath)) {
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		FileWatcher fileWatcher = createFileWatcher(file.toString(), handler, updateChild);
		fileWatchers.put(fileWatcher.watchPath, fileWatcher);
	}

	public static synchronized void registerDirRecursive(String rootPath, FileWatcherHandler handler, boolean updateChild) {
		logger.debug("registerDirRecursive("+rootPath+")");
		try {
			Path root = Paths.get(rootPath);
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        		@Override
        	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (dir.toFile().isDirectory()) {
						registerDir(dir.toString(), handler, updateChild);
					}
        	        return FileVisitResult.CONTINUE;
        	    }
        	});
		} catch (IOException e) {
			e.printStackTrace();
		}
   	}
	
	public static synchronized void register(String filePath, FileWatcherHandler handler) {
		register(filePath, handler, false);	
	}
	
	public static synchronized void register(String filePath, FileWatcherHandler handler, boolean updateChild) {
		File file = new File(filePath);
		if (file.isFile()) {
			logger.warn("register "+ filePath +" , param is not directory, please register dir instead!");
		} else {
			registerDirRecursive(filePath, handler, updateChild);
		}
	}

	public static Path findFile(String filePath) {
		try {
			Path root = Paths.get(filePath);
			final Path[] resultPath = new Path[1];
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        		@Override
        	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					logger.info("visit " + dir.toFile().getCanonicalPath());
					if (dir.toString().endsWith(filePath)) {
						resultPath[0] = Paths.get(dir.toFile().getCanonicalPath());
						return FileVisitResult.TERMINATE;
					}
        	        return FileVisitResult.CONTINUE;
        	    }
        	});
			return resultPath[0];
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void watch() {
		Runnable watchLoop = new Runnable(){
			public void run() {
				try {
					while (true) {
						WatchKey key = watchService.take();
						if (key != null) {
							for (WatchEvent<?> event : key.pollEvents()) {
								WatchEvent<Path> e = (WatchEvent<Path>)event;
								Path fileName = e.context();		
								if (fileName.startsWith(".") ||
									fileName.startsWith("~") ||
									fileName.endsWith(".") ||
									fileName.endsWith("~") ||
									fileName.endsWith(".swp")) {
									continue;
								}
								
								WatchEvent.Kind<?> kind = event.kind();
								if (kind == OVERFLOW || kind == ENTRY_DELETE) {
									continue;
								}

								if (kind == ENTRY_CREATE) {
									Map<String, FileWatcherHandler> createDirs = new LinkedHashMap<String, FileWatcherHandler>();	
									for (Map.Entry<String, FileWatcher> entry : fileWatchers.entrySet()) {
										FileWatcher fileWatcher = entry.getValue();
										File watchFile = fileWatcher.getWatchFile(fileName.toString());
										if (watchFile != null) {
											if (watchFile.isDirectory()) {
												createDirs.put(watchFile.getCanonicalPath(), fileWatcher.handler);
											} else {
												fileWatcher.handleFile(watchFile);	
											}
										}
									}
									for (Map.Entry<String, FileWatcherHandler> entry: createDirs.entrySet()) {
										registerDirRecursive(entry.getKey(), entry.getValue(), true);
									}
								}

								if (kind == ENTRY_MODIFY) {
									for (Map.Entry<String, FileWatcher> entry : fileWatchers.entrySet()) {
										FileWatcher fileWatcher = entry.getValue();
										File watchFile = fileWatcher.getWatchFile(fileName.toString());
										fileWatcher.handleFile(watchFile);	
									}
								}
							}
							key.reset();
						}
						Thread.sleep(pollInterval);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(watchLoop).start();
	}
}
