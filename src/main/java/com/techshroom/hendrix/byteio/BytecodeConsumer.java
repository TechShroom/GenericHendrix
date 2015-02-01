package com.techshroom.hendrix.byteio;

/**
 * A consumer of bytecode. Takes a byte array.
 * 
 * @author Kenzie Togami
 */
public interface BytecodeConsumer {
    /**
     * Gives bytecode to this consumer.
     * 
     * @param bytecode - The bytecode
     */
    void bytecode(byte[] bytecode);
}
