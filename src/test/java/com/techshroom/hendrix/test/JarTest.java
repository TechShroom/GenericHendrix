package com.techshroom.hendrix.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;

import com.google.common.io.ByteStreams;
import com.techshroom.hendrix.jar.JarData;

/**
 * Jar testing for {@linkplain JarData}.
 * 
 * @author Kenzie Togami
 */
public class JarTest {
    private File testJarLocation;
    private File testJarTarget;

    /**
     * Chooses the test JAR location.
     */
    @Before
    public void chooseJarLocation() {
        this.testJarLocation = new File("testData/jars/jartest.jar");
        assertTrue("jar doesn't exist", this.testJarLocation.exists());
        this.testJarTarget = new File("testData/jars/jartest_target.jar");
        try {
            assertTrue("jar does exist",
                            this.testJarTarget.exists() ? this.testJarTarget
                                            .delete() : this.testJarTarget
                                            .createNewFile());
        } catch (IOException cannotCreate) {
            failException(cannotCreate);
            return;
        }
        try (InputStream in = new FileInputStream(this.testJarLocation);
                        OutputStream out =
                                        new FileOutputStream(this.testJarTarget)) {
            ByteStreams.copy(in, out);
        } catch (IOException cantRead) {
            failException(cantRead);
            return;
        }
        this.testJarTarget.deleteOnExit();
    }

    /**
     * Kill the jars.
     */
    @After
    public void killJar() {
        if (!this.testJarTarget.delete()) {
            this.testJarTarget.deleteOnExit();
        }
    }

    private void failException(IOException e) {
        e.printStackTrace();
        fail(e.getLocalizedMessage());
    }
}
