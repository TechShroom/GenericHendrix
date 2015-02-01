package com.techshroom.hendrix.test;

/**
 * Some test constants.
 * 
 * @author Kenzie Togami
 */
public interface TestConstants {
    /**
     * The test data folder.
     */
    String DATA_FOLDER = "testData";
    /**
     * The folder that contains the classes.
     */
    String CLASSES_FOLDER = DATA_FOLDER + "/classes";
    /**
     * The folder that contains the results of the class processing.
     */
    String RESULT_FOLDER = DATA_FOLDER + "/results";
    /**
     * A file that cannot exist.
     */
    String MISSING_FILE = DATA_FOLDER + "/--cannotexist--";
}
