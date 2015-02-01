package com.techshroom.hendrix.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.techshroom.hendrix.jar.JarData;

/**
 * Jar testing for {@linkplain JarData}.
 * 
 * @author Kenzie Togami
 */
public class JarTest {
    private File testJarLocation;

    /**
     * Chooses the test JAR location.
     */
    @Before
    public void chooseUJarLocation() {
        this.testJarLocation = new File("testData/jars/jartest.jar");
        assertTrue("jar doesn't exist", this.testJarLocation.exists());
    }

    /**
     * Run a replaceEntry with a non-existent entry. Nothing should be changed.
     */
    @Test
    public void rewritesNothing() {
        byte[] data;
        try (InputStream in = new FileInputStream(this.testJarLocation)) {
            data = ByteStreams.toByteArray(in);
        } catch (IOException cantRead) {
            failException(cantRead);
            return;
        }
        try {
            JarData.replaceEntry(new JarFile(this.testJarLocation),
                            new JarEntry("i/don't/exist/.txt"), new byte[0]);
        } catch (IOException e) {
            failException(e);
            return;
        }
        byte[] match;
        try (InputStream in = new FileInputStream(this.testJarLocation)) {
            match = ByteStreams.toByteArray(in);
        } catch (IOException cantRead) {
            failException(cantRead);
            return;
        }
        System.out.println(Arrays.toString(data));
        System.out.println(Arrays.toString(match));
        assertArrayEquals("jar data doesn't match", data, match);
    }

    private void failException(IOException e) {
        e.printStackTrace();
        fail(e.getLocalizedMessage());
    }
}
