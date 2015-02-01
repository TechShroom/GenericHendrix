package com.techshroom.hendrix.byteio;

/**
 * A source of bytecode. Provides a byte array.
 * 
 * @author Kenzie Togami
 */
public interface BytecodeSupplier {
    /**
     * Gets the bytecode from this supplier.
     * 
     * @return The bytecode
     */
    byte[] bytecode();

    /**
     * Checks if this bytecode should be processed by the processor.
     * 
     * @return {@code true} if this bytecode should be processed
     */
    boolean shouldBeProcessed();

    /**
     * Get the consumer that should be used for transformed output.
     * 
     * @return The BytecodeConsumer that is linked to this supplier
     */
    BytecodeConsumer getConsumer();
}
