package com.techshroom.hendrix;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.byteio.BytecodeSupplier;
import com.techshroom.hendrix.byteio.EntryBytecode;
import com.techshroom.hendrix.byteio.FileBytecode;

import fj.data.Array;

/**
 * A factory for creating bytecode. The implementation is loaded at runtime from
 * the classpath.
 * 
 * @author Kenzie Togami
 */
public abstract class BytecodeSupplierFactory {
    /**
     * The default implementation transforms jars, directories, and classes into
     * suppliers.
     * 
     * @author Kenzie Togami
     */
    public static class DefaultImplementation extends BytecodeSupplierFactory {
        @Override
        protected Array<BytecodeSupplier> supplyBytecodeImpl(Array<Path> input) {
            return supplyBytecodeImpl(input, true);
        }

        @Override
        protected Array<BytecodeSupplier> supplyBytecodeImpl(Array<Path> input,
                        boolean process) {
            List<BytecodeSupplier> data = new ArrayList<>();
            for (Path path : input) {
                if (Files.isDirectory(path)) {
                    try {
                        Files.walkFileTree(path, visitorFor(data, process));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (Files.isRegularFile(path)) {
                    if (path.toString().endsWith(".class")) {
                        data.add(new FileBytecode(path, process));
                    } else if (path.toString().endsWith(".jar")) {
                        expandJarToParts(path, data, process);
                    } else {
                        System.err.println("ignoring " + path);
                    }
                } else {
                    System.err.println("ignoring " + path);
                }
            }
            return Array.iterableArray(data);
        }

        protected void expandJarToParts(Path path, List<BytecodeSupplier> data,
                        boolean process) {
            try {
                JarFile jar = new JarFile(path.toFile());
                for (Enumeration<JarEntry> entries = jar.entries(); entries
                                .hasMoreElements();) {
                    JarEntry e = entries.nextElement();
                    if (e.getName().endsWith(".class")) {
                        data.add(new EntryBytecode(jar, e, process));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Generate a visitor for adding the class files to the data.
         */
        protected FileVisitor<Path> visitorFor(
                        final List<BytecodeSupplier> data, final boolean process) {
            return new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        data.add(new FileBytecode(file, process));
                    }
                    return FileVisitResult.CONTINUE;
                }
            };
        }
    }

    private static final BytecodeSupplierFactory impl;
    static {
        ServiceLoader<BytecodeSupplierFactory> loader =
                        ServiceLoader.load(BytecodeSupplierFactory.class);
        ImmutableList<BytecodeSupplierFactory> implementations =
                        ImmutableList.copyOf(loader);
        BytecodeSupplierFactory chosen = new DefaultImplementation();
        if (!implementations.isEmpty()) {
            chosen = implementations.get(0);
            if (implementations.size() > 1) {
                System.err.println("Detected more than one implementation for BytecodeSupplierFactory");
                System.err.println("Out of ");
                for (BytecodeSupplierFactory factory : implementations) {
                    System.err.println("\t" + factory);
                }
                System.err.println(chosen + " has been chosen.");
            }
        }
        impl = chosen;
    }

    /**
     * Transform the given {@linkplain Path Paths} into an array of
     * {@linkplain BytecodeSupplier bytecode suppliers}. The bytecode suppliers
     * returned will be marked for processing.
     * 
     * @param input - The input paths
     * @return The resulting bytecode suppliers that may be used to read the
     *         data from the input
     */
    public static final Array<BytecodeSupplier> supplyBytecode(Array<Path> input) {
        return impl.supplyBytecodeImpl(input);
    }

    /**
     * Transform the given {@linkplain Path Paths} into an array of
     * {@linkplain BytecodeSupplier bytecode suppliers}.
     * 
     * @param input - The input paths
     * @param process - {@code true} if the supplied bytecode should be
     *        processed, {@code false} otherwise.
     * @return The resulting bytecode suppliers that may be used to read the
     *         data from the input
     */
    public static final Array<BytecodeSupplier> supplyBytecode(
                    Array<Path> input, boolean process) {
        return impl.supplyBytecodeImpl(input, process);
    }

    /**
     * Transform the given {@linkplain Path Paths} into an array of
     * {@linkplain BytecodeSupplier bytecode suppliers}. The bytecode suppliers
     * returned will be marked for processing.
     * 
     * @param input - The input paths
     * @return The resulting bytecode suppliers that may be used to read the
     *         data from the input
     */
    protected abstract Array<BytecodeSupplier> supplyBytecodeImpl(
                    Array<Path> input);

    /**
     * Transform the given {@linkplain Path Paths} into an array of
     * {@linkplain BytecodeSupplier bytecode suppliers}.
     * 
     * @param input - The input paths
     * @param process - {@code true} if the supplied bytecode should be
     *        processed, {@code false} otherwise.
     * @return The resulting bytecode suppliers that may be used to read the
     *         data from the input
     */
    protected abstract Array<BytecodeSupplier> supplyBytecodeImpl(
                    Array<Path> input, boolean process);
}
