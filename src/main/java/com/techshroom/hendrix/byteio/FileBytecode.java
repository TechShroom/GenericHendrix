package com.techshroom.hendrix.byteio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.ByteStreams;

/**
 * File-based bytecode container.
 * 
 * @author Kenzie Togami
 */
public class FileBytecode extends BasicBytecodeContainer {
    private final Path file;

    /**
     * Creates a new file-based bytecode container that should be processed.
     * 
     * @param file - The file to load and save from
     */
    public FileBytecode(Path file) {
        this(file, true);
    }

    /**
     * /** Creates a new file-based bytecode container with a process flag of
     * {@code process}.
     * 
     * @param file - The file to load and save from
     * @param process - {@code true} if this bytecode should be processed,
     *        {@code false} otherwise.
     */
    public FileBytecode(Path file, boolean process) {
        super(process);
        this.file = file;
    }

    @Override
    protected void save(byte[] bytes) {
        try (OutputStream out = Files.newOutputStream(this.file)) {
            out.write(bytes);
        } catch (IOException ign) {
            suppress(ign);
        }
    }

    @Override
    protected byte[] load() {
        try (InputStream in = Files.newInputStream(this.file)) {
            return ByteStreams.toByteArray(in);
        } catch (IOException ign) {
            suppress(ign);
            return new byte[0];
        }
    }
}
