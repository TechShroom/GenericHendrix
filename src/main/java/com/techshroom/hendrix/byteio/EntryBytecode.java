package com.techshroom.hendrix.byteio;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.io.ByteStreams;
import com.techshroom.hendrix.jar.JarData;

/**
 * JarEntry-based bytecode container.
 * 
 * @author Kenzie Togami
 */
public class EntryBytecode extends BasicBytecodeContainer {
    private final JarFile sourceJar;
    private final JarEntry entry;

    /**
     * Creates a new file-based bytecode container that should be processed.
     * 
     * @param sourceJar - The original jar file the entry is from
     * @param entry - The entry to bind to
     */
    public EntryBytecode(JarFile sourceJar, JarEntry entry) {
        this(sourceJar, entry, true);
    }

    /**
     * Creates a new file-based bytecode container with a process flag of
     * {@code process}.
     * 
     * @param sourceJar - The original jar file the entry is from
     * @param entry - The entry to bind to
     * @param process - {@code true} if this bytecode should be processed,
     *        {@code false} otherwise.
     */
    public EntryBytecode(JarFile sourceJar, JarEntry entry, boolean process) {
        super(process);
        this.sourceJar = sourceJar;
        this.entry = entry;
    }

    @Override
    protected void save(byte[] bytes) {
        try {
            JarData.replaceEntry(this.sourceJar, this.entry, bytes);
        } catch (IOException e) {
            suppress(e);
        }
    }

    @Override
    protected byte[] load() {
        try (InputStream in = this.sourceJar.getInputStream(this.entry)) {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            suppress(e);
            return new byte[0];
        }
    }
}
