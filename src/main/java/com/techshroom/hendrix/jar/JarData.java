package com.techshroom.hendrix.jar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import com.google.common.io.ByteStreams;

/**
 * Support for writing only one entry to a jar.
 * 
 * @author Kenzie Togami
 */
public final class JarData {
    /**
     * Replace the data for entry {@code a} with {@code bytes} in {@code jar}.
     * 
     * @param jar - The jar to perform the action on
     * @param a - The entry to put the data in
     * @param bytes - The bytes to insert
     * @throws IOException All IOExceptions propagate
     */
    public static void replaceEntry(JarFile jar, JarEntry a, byte[] bytes)
                    throws IOException {
        File tmp = File.createTempFile("hendrix-jar-copy", ".jar");
        try (JarOutputStream jarStream =
                        new JarOutputStream(new FileOutputStream(tmp));) {
            jarStream.setComment(jar.getComment());

            Manifest man = jar.getManifest();
            if (man != null) {
                ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
                jarStream.putNextEntry(e);
                man.write(new BufferedOutputStream(jarStream));
                jarStream.closeEntry();
            }
            for (Enumeration<JarEntry> entries = jar.entries(); entries
                            .hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }
                if (a.getName().equals(entry.getName())) {
                    entry = a;
                    entry.setTime(System.currentTimeMillis());
                }
                jarStream.putNextEntry(entry);
                if (a.getName().equals(entry.getName())) {
                    jarStream.write(bytes);
                } else {
                    try (InputStream dataSource = jar.getInputStream(entry)) {
                        ByteStreams.copy(dataSource, new BufferedOutputStream(
                                        jarStream));
                    }
                }
                jarStream.closeEntry();
            }
            tmp.renameTo(new File(jar.getName()));
        } finally {
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }
    }

    /**
     * Remove entry {@code remove} from {@code jar}.
     * 
     * @param jar - The jar to perform the action on
     * @param remove - The entry to remove
     * @throws IOException All IOExceptions propagate
     */
    public static void removeEntry(JarFile jar, JarEntry remove)
                    throws IOException {
        File tmp = File.createTempFile("hendrix-jar-copy", ".jar");
        try (OutputStream fileOut = new FileOutputStream(tmp);
                        JarOutputStream jarStream =
                                        new JarOutputStream(
                                                        new ByteArrayOutputStream() {
                                                            @Override
                                                            public void flush()
                                                                            throws IOException {
                                                                byte[] data =
                                                                                toByteArray();
                                                                System.err.println(">>>data"
                                                                                + new String(
                                                                                                data)
                                                                                + "data<<<");
                                                                reset();
                                                                fileOut.write(data);
                                                            }
                                                        });) {
            jarStream.setComment(jar.getComment());

            Manifest man = jar.getManifest();
            if (man != null) {
                ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
                jarStream.putNextEntry(e);
                man.write(new BufferedOutputStream(jarStream));
                jarStream.closeEntry();
            }
            for (Enumeration<JarEntry> entries = jar.entries(); entries
                            .hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }
                if (remove.getName().equals(entry.getName())) {
                    continue;
                }
                jarStream.putNextEntry(entry);
                try (InputStream dataSource = jar.getInputStream(entry)) {
                    ByteStreams.copy(dataSource, jarStream);
                }
                jarStream.closeEntry();
            }
            tmp.renameTo(new File(jar.getName()));
        } finally {
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }
    }

    private JarData() {}
}
