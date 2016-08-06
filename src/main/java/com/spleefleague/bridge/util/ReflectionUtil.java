package com.spleefleague.bridge.util;

import com.spleefleague.bridge.SLBridge;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by Josh on 04/08/2016.
 */
public class ReflectionUtil {

    /**
     * Find all the classes in a package.
     *
     * @param pack      package to scan for.
     * @param mainClass main classloader class - IT SCANS THIS JAR FILE.
     * @param s         can be null, superclass/interface to scan for aswell.
     * @return set of classes found.
     */
    public static Set<Class<?>> find(String pack, Class mainClass, Class s) {
        Set<Class<?>> result = new HashSet<>();
        try {
            ZipFile zip = new ZipFile(mainClass.getProtectionDomain().getCodeSource().getLocation().getFile().replace("%20", " "));
            for (Enumeration<? extends ZipEntry> entries = zip
                    .entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && name.contains(pack.replace(".", "/"))) {
                    if (name.contains("$")) {
                        continue;
                    }
                    Class c = SLBridge.getInstance().getClass().getClassLoader().loadClass(name.replace("/", ".").replace(".class", ""));
                    if (s == null || (c.getSuperclass() == s || c.isInstance(s))) {
                        result.add(c);
                    }
                }
            }
        } catch (ZipException e) {
            System.out.println("Not a ZIP: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

}
