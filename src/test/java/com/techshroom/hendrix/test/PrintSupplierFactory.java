package com.techshroom.hendrix.test;

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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.auto.service.AutoService;
import com.techshroom.hendrix.BytecodeSupplierFactory;
import com.techshroom.hendrix.byteio.BytecodeSupplier;
import com.techshroom.hendrix.byteio.EntryBytecode;
import com.techshroom.hendrix.byteio.FileBytecode;

import fj.data.Array;

/**
 * Sorta rewritten version of the default implementation that prints some data
 * on loading.
 * 
 * @author Kenzie Togami
 */
@AutoService(BytecodeSupplierFactory.class)
public class PrintSupplierFactory extends
                BytecodeSupplierFactory.DefaultImplementation {
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
                    System.err.println("[regular] adding " + path);
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

    @Override
    protected void expandJarToParts(Path path, List<BytecodeSupplier> data,
                    boolean process) {
        try {
            System.err.println("[jar] opening " + path);
            JarFile jar = new JarFile(path.toFile());
            for (Enumeration<JarEntry> entries = jar.entries(); entries
                            .hasMoreElements();) {
                JarEntry e = entries.nextElement();
                System.err.println("[jar] testing " + e.getName());
                if (e.getName().endsWith(".class")) {
                    System.err.println("[jar] adding " + e.getName());
                    data.add(new EntryBytecode(jar, e, process));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected FileVisitor<Path> visitorFor(final List<BytecodeSupplier> data,
                    final boolean process) {
        return new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) throws IOException {
                System.err.println("[dir] testing " + file);
                if (file.toString().endsWith(".class")) {
                    System.err.println("[dir] adding " + file);
                    data.add(new FileBytecode(file, process));
                }
                return FileVisitResult.CONTINUE;
            }
        };
    }
}
