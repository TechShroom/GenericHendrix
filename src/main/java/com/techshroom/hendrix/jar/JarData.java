package com.techshroom.hendrix.jar;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.ByteStreams;

/**
 * Support for writing only one entry to a jar.
 * 
 * @author Kenzie Togami
 */
public final class JarData {
    private static class FieldReader<T, V> {
        public static <T, V> FieldReader<T, V> create(Class<T> clazz,
                        Class<V> fieldType, String name) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return new FieldReader<T, V>(field, fieldType);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        protected final Field field;
        private final Class<V> fieldType;

        FieldReader(Field field, Class<V> fieldType) {
            checkArgument(fieldType.isAssignableFrom(field.getType()));
            this.field = field;
            this.fieldType = fieldType;
        }

        @SuppressWarnings("unchecked")
        // already verified fieldType
        public V get(T instance) {
            try {
                return (V) this.field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unused")
        public Class<V> getType() {
            return this.fieldType;
        }
    }

    private static class FieldAccessor<T, V> extends FieldReader<T, V> {
        public static <T, V> FieldAccessor<T, V> create(Class<T> clazz,
                        Class<V> fieldType, String name) {

            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return new FieldAccessor<T, V>(field, fieldType);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        FieldAccessor(Field field, Class<V> fieldType) {
            super(field, fieldType);
            checkArgument(field.getType().isAssignableFrom(fieldType));
        }

        public void set(T instance, V value) {
            try {
                this.field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final FieldReader<FilterInputStream, InputStream> FIS_IN =
                    FieldReader.create(FilterInputStream.class,
                                    InputStream.class, "in");

    private static final FieldReader<ZipOutputStream, CRC32> ZOS_CRC =
                    FieldReader.create(ZipOutputStream.class, CRC32.class,
                                    "crc");

    private static final FieldAccessor<CRC32, Integer> CRC_VALUE =
                    FieldAccessor.create(CRC32.class, int.class, "crc");

    private static final FieldAccessor<ZipEntry, String> ZE_NAME =
                    FieldAccessor.create(ZipEntry.class, String.class, "name");

    /**
     * Replace the data for entry {@code a} with {@code bytes} in {@code jar}.
     * 
     * @param jarFile - The jar to perform the action on
     * @param a - The entry to put the data in
     * @param bytes - The bytes to insert
     * @throws IOException All IOExceptions propagate
     */
    public static void replaceEntry(File jarFile, JarEntry a, byte[] bytes)
                    throws IOException {
        JarFile jar = new JarFile(jarFile);
        File tmp = File.createTempFile("hendrix-jar-copy", ".jar");
        try {
            try (JarOutputStream jarStream =
                            new JarOutputStream(new FileOutputStream(tmp))) {
                // jarStream.setComment(jar.getComment());
                for (Enumeration<JarEntry> entries = jar.entries(); entries
                                .hasMoreElements();) {
                    JarEntry entry = entries.nextElement();
                    if (a.getName().equals(entry.getName())) {
                        entry.setTime(System.currentTimeMillis());
                        copyEntry(jarStream, entry.getName(), jar, entry, bytes);
                        continue;
                    }
                    copyEntry(jarStream, entry.getName(), jar, entry, null);
                }
            }
            jar.close();
            tmp.renameTo(jarFile);
        } finally {
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }
    }

    /**
     * Remove entry {@code remove} from {@code jar}.
     * 
     * @param jarFile - The jar to perform the action on
     * @param remove - The entry to remove
     * @throws IOException All IOExceptions propagate
     */
    public static void removeEntry(File jarFile, JarEntry remove)
                    throws IOException {
        JarFile jar = new JarFile(jarFile);
        File tmp = File.createTempFile("hendrix-jar-copy", ".jar");
        try {
            try (JarOutputStream jarStream =
                            new JarOutputStream(new FileOutputStream(tmp))) {
                // jarStream.setComment(jar.getComment());
                for (Enumeration<JarEntry> entries = jar.entries(); entries
                                .hasMoreElements();) {
                    JarEntry entry = entries.nextElement();
                    if (remove.getName().equals(entry.getName())) {
                        continue;
                    }
                    copyEntry(jarStream, entry.getName(), jar, entry, null);
                }
            }
            jar.close();
            tmp.renameTo(jarFile);
        } finally {
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }
    }

    /**
     * Copy a a jar entry to an output file without decompressing and
     * re-compressing the entry when it is {@link ZipEntry#DEFLATED}.
     *
     * @param jarOut The jar file being created or appended to.
     * @param name The resource name to write.
     * @param jarIn The input JarFile.
     * @param jarEntry The entry extracted from <code>jarIn</code>. The
     *        compression method passed in to this entry is preserved in the
     *        output file.
     * @throws IOException if there is a problem reading from {@code jarIn} or
     *         writing to {@code jarOut}.
     */
    static void copyEntry(JarOutputStream jarOut, String name, JarFile jarIn,
                    JarEntry jarEntry, byte[] replacementBytes)
                    throws IOException {
        JarEntry outEntry = new JarEntry(jarEntry);
        ZE_NAME.set(outEntry, name);

        if (outEntry.isDirectory()) {
            outEntry.setMethod(ZipEntry.STORED);
            outEntry.setSize(0);
            outEntry.setCompressedSize(0);
            outEntry.setCrc(0);
            jarOut.putNextEntry(outEntry);
            jarOut.closeEntry();
        } else if (jarEntry.getMethod() == ZipEntry.STORED) {
            try (InputStream is = jarIn.getInputStream(jarEntry)) {
                jarOut.putNextEntry(outEntry);
                if (replacementBytes == null) {
                    ByteStreams.copy(is, jarOut);
                } else {
                    jarOut.write(replacementBytes);
                }
            }
            jarOut.closeEntry();
        } else {
            try (FilterInputStream zis =
                            (FilterInputStream) jarIn.getInputStream(jarEntry)) {
                // Grab the underlying stream so we can read the compressed
                // bytes.

                InputStream is = FIS_IN.get(zis);

                // Start it as a DEFLATE....
                jarOut.putNextEntry(outEntry);

                // But swap out the method to STORE to the bytes don't get
                // compressed.
                // This works because ZipFile doesn't make a defensive copy.
                outEntry.setMethod(ZipEntry.STORED);
                outEntry.setSize(jarEntry.getCompressedSize());
                if (replacementBytes == null) {
                    ByteStreams.copy(is, jarOut);
                } else {
                    jarOut.write(replacementBytes);
                }

            }

            // The internal CRC is now wrong, so hack it before we close the
            // entry.
            CRC_VALUE.set(ZOS_CRC.get(jarOut),
                            Integer.valueOf((int) jarEntry.getCrc()));
            jarOut.closeEntry();

            // Restore entry back to normal, so it will be written out correctly
            // at the end.
            outEntry.setMethod(ZipEntry.DEFLATED);
            outEntry.setSize(jarEntry.getSize());
        }
    }

    private JarData() {}
}
