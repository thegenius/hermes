package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import javax.xml.crypto.URIReferenceException;

import java.util.zip.ZipEntry;
import java.lang.management.ManagementFactory;
public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void walkRoot(Path root) {
        try {
            File file = root.toFile();
            if (file.isFile()) {
                logger.debug("walkUrl -> {}", file.getName());
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File childFile : files) {
                    walkRoot(childFile.toPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void walkUrls(Enumeration<URL> urls) {
        logger.debug("walkUrls({}) hasMoreElements -> {}", urls, urls.hasMoreElements());
        try {
            while (urls.hasMoreElements()) {
                Path path = Paths.get(urls.nextElement().toURI());
                walkRoot(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("---------------------------------------------------");
    }

    public static void walkUrls2(Enumeration<URL> urls) {
        logger.debug("walkUrls({}) hasMoreElements -> {}", urls, urls.hasMoreElements());
        try {
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                logger.info("{}", url);
                logger.info("{}", url.toURI());

                int index = url.toString().lastIndexOf("!");
                String path = url.toString().substring(index + 2);
                logger.info("path -> {}", path);
                InputStream ins1 = RuntimeFileUtils.class.getClassLoader().getResourceAsStream(path);
                InputStream ins2 = RuntimeFileUtils.class.getClassLoader().getSystemResourceAsStream(path);
                logger.info("inputstream == null -> {}", ins1 == null);
                if (ins1 != null) {
                    logger.info("inputstream avaliable -> {}", ins1.available());
                }
                logger.info("inputstream == null -> {}", ins2 == null);
                if (ins2 != null) {
                    logger.info("inputstream avaliable -> {}", ins2.available());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("---------------------------------------------------");
    }

    public static void loadResourceTest() {
        try {
            Enumeration<URL> urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources("");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("/");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources("/");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources(".");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources(".");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources("com/lvonce/hermes");
            walkUrls2(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("com/lvonce/hermes");
            walkUrls2(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("com/lvonce");
            walkUrls2(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("com");
            walkUrls2(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("/");
            walkUrls2(urls);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadResourceTest2(String path) {
        try {
            Enumeration<URL> urls;
            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources(path);
            walkUrls2(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources(path);
            walkUrls2(urls);
        } catch (IOException e) {
        }
    }

    private static int count = 0;
    private static int mainCount = 0;
    public static void main(String[] args) throws Exception {
        //        loadResourceTest2(args[0]);File jarName = null;
        // ZipFile zf = null;
        // try {
        //     String jarName = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        //     int index = jarName.indexOf(":");
        //     jarName = jarName.substring(index + 1);
        //     logger.debug("jar name -> {}", jarName);
        //     File jarFile = new File(jarName);
        //     zf = new ZipFile(jarFile.getCanonicalPath());
        //     Enumeration e = zf.entries();
        //     while (e.hasMoreElements()) {
        //         ZipEntry ze = (ZipEntry) e.nextElement();
        //         System.out.println(ze.getName());
        //     }

        //     Class<?>[] classes = RuntimeFileUtils.getAllPackageClasses();
        //     for (Class<?> classType : classes) {
        //         logger.info("class -> {}", classType.getName());
        //     }


        // } catch (IOException e) {
        //     e.printStackTrace();
        // } finally {
        //     if (zf != null) {
        //         zf.close();
        //     }
        // }
        String name = ManagementFactory.getRuntimeMXBean().getName();    
        System.out.println(name);    
        // get pid    
        String pid = name.split("@")[0];    
        System.out.println("Pid is:" + pid); 

            mainCount ++;
            logger.info("main count -> {}", mainCount); 
                while (true) {
                    try {
                        logger.debug("main thread loop ...");
                        int c = System.in.read();
                        logger.debug("input -> {}", c);
                        count ++ ;
                        logger.debug("loop count[{}]", count);
                        Object hello = Hermes.create("com.lvonce.hermes.Hello");
                        logger.info("test -> {}", Hermes.invoke(hello, "test"));
                        logger.debug("loop end ...");
                    } catch (Exception e) {
                        logger.debug("Error !!!!!!!!!!!!!!!!!!!!!!!");
                        e.printStackTrace();
                    }
                }

        // Thread t = new Thread() {
        //     public void run() {
        //     }
        // };
        // t.run();
        // t.join();
    }
}