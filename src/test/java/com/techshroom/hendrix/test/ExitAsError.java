package com.techshroom.hendrix.test;

import com.google.auto.service.AutoService;
import com.techshroom.hendrix.ExitHandler;

/**
 * Exit handler for tests.
 * 
 * @author Kenzie Togami
 */
@AutoService(ExitHandler.class)
public class ExitAsError implements ExitHandler {
    static void loadClass() {
        // nothing to do, just for loading this class
    }

    @Override
    public Error exit(int code) {
        return new Error(Integer.toString(code));
    }
}
