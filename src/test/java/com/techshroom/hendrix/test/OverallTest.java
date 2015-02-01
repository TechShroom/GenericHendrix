package com.techshroom.hendrix.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.techshroom.hendrix.Main;

/**
 * General test for ensuring this works at all.
 * 
 * @author Kenzie Togami
 */
public final class OverallTest implements TestConstants {

    /**
     * Loads our exit handler into the VM.
     */
    @BeforeClass
    public static void loadExitHandler() {
        ExitAsError.loadClass();
    }

    /**
     * Checks for existence of the missing file.
     */
    @BeforeClass
    public static void missingFileIsMissing() {
        assertFalse("missing file is not missing",
                        Files.exists(Paths.get(MISSING_FILE)));
    }

    /**
     * Cleans up all files that are thought to have been generated.
     */
    @After
    public void removeGeneratedFiles() {
        Path f = Paths.get(RESULT_FOLDER);
        nuke(f);
    }

    private void nuke(Path stuff) {
        if (!Files.exists(stuff, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        if (Files.isRegularFile(stuff, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.delete(stuff);
            } catch (IOException e) {
                e.printStackTrace();
                stuff.toFile().deleteOnExit();
            }
        } else {
            try {
                Files.walkFileTree(stuff, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                                    BasicFileAttributes attrs)
                                    throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir,
                                    IOException e) throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            // directory iteration failed
                            throw e;
                        }
                    }
                });
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }

    /**
     * Runs the overall test.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void overallTest() throws Exception {
        Main.main(("--input " + CLASSES_FOLDER + " --output " + RESULT_FOLDER + "")
                        .split(" "));
        // TODO: assertions
    }

    /**
     * Tests Hendrix's refusal of multiple outputs.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void refusesMultipleOutputs() throws Exception {
        try {
            Main.main(("--output " + RESULT_FOLDER + " --output " + RESULT_FOLDER)
                            .split(" "));
        } catch (Error expect) {
            assertEquals("1", expect.getMessage());
        }
    }

    /**
     * Tests Hendrix's refusal of non-existent inputs.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void refusesNonExistentInputs() throws Exception {
        try {
            Main.main(("--input " + MISSING_FILE + " --output " + RESULT_FOLDER)
                            .split(" "));
            fail("Should have thrown exception");
        } catch (IllegalArgumentException expect) {
        }
    }

    /**
     * Tests Hendrix's refusal of output and input being mixed.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void refusesMixed() throws Exception {
        try {
            Main.main(("--input " + CLASSES_FOLDER + " --output "
                            + CLASSES_FOLDER + "/" + RESULT_FOLDER).split(" "));
            fail("Should have thrown exception");
        } catch (IllegalStateException expect) {
        }
    }
}
