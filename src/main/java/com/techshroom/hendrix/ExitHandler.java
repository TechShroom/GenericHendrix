package com.techshroom.hendrix;

/**
 * Exit handler for exiting.
 * 
 * @author Kenzie Togami
 */
public interface ExitHandler {
    /**
     * Called when an exit request is made.
     * 
     * @param code - The exit code
     * @return An error that will be thrown
     */
    Error exit(int code);
}
