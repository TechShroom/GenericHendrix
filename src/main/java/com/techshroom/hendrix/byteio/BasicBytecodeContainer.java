package com.techshroom.hendrix.byteio;

import com.google.common.base.Optional;

/**
 * Basic in-memory bytecode container. This class has a {@link #save()} method
 * and a {@link #load()} method which are called to save and load to some other
 * storage, if needed.
 * 
 * @author Kenzie Togami
 */
@SuppressWarnings("javadoc")
public class BasicBytecodeContainer implements BytecodeSupplier,
                BytecodeConsumer {
    private final boolean process;
    private transient Exception suppresionCollector;
    private transient byte[] loadedBytes;

    /**
     * Creates a new in-memory bytecode container that should be processed.
     * 
     */
    public BasicBytecodeContainer() {
        this(true);
    }

    /**
     * /** Creates a new in-memory bytecode container with a process flag of
     * {@code process}.
     * 
     * @param process - {@code true} if this bytecode should be processed,
     *        {@code false} otherwise.
     */
    public BasicBytecodeContainer(boolean process) {
        this.process = process;
    }

    @Override
    public void bytecode(byte[] bytecode) {
        this.loadedBytes = bytecode.clone();
        save(this.loadedBytes);
    }

    @Override
    public byte[] bytecode() {
        if (this.loadedBytes == null) {
            this.loadedBytes = load();
        }
        return this.loadedBytes;
    }

    protected void save(byte[] bytes) {}

    protected byte[] load() {
        return new byte[0];
    }

    @Override
    public boolean shouldBeProcessed() {
        return this.process;
    }

    @Override
    public BytecodeConsumer getConsumer() {
        return this;
    }

    /**
     * Suppress an exception.
     * 
     * @param ign - Exception to suppress
     */
    protected void suppress(Exception ign) {
        if (this.suppresionCollector == null) {
            this.suppresionCollector =
                            new Exception(FileBytecode.class.getName());
        }
        this.suppresionCollector.addSuppressed(ign);
    }

    /**
     * Gets the exception that holds other suppressed exceptions that occurred
     * during some operation. May be absent if no exceptions have occurred.
     * 
     * @return The exception that holds other suppressed exceptions
     */
    public Optional<Exception> getException() {
        return Optional.fromNullable(this.suppresionCollector);
    }
}
