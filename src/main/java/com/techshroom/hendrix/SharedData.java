package com.techshroom.hendrix;

import org.objectweb.asm.Opcodes;

/**
 * A data storage area for Hendrix.
 * 
 * @author Kenzie Togami
 */
public final class SharedData {

    /**
     * The ASM version in use by Hendrix.
     */
    public static final int ASM_VERSION = Opcodes.ASM5;

    /**
     * Constant for passing no flags.
     */
    public static final int NO_FLAGS = 0;

    /**
     * A debug flag, used in places to spit out information.
     */
    public static boolean debug;

    private SharedData() {
        throw new AssertionError("Unshared usage.");
    }

}
