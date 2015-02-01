package com.techshroom.hendrix;

import static com.google.common.base.Preconditions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.ValueConverter;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.techshroom.hendrix.byteio.BytecodeSupplier;
import com.techshroom.hendrix.process.ProcessEntry;

import fj.data.Array;

/**
 * Entry point of Hendrix.
 * 
 * @author Kenzie Togami
 */
public final class Main {
    private static final ValueConverter<Path> TO_PATH =
                    new ValueConverter<Path>() {
                        @Override
                        public Path convert(String value) {
                            return Paths.get(value);
                        }

                        @Override
                        public Class<? extends Path> valueType() {
                            return Path.class;
                        }

                        @Override
                        public String valuePattern() {
                            return null;
                        }
                    };
    private static final OptionParser PARSER = new OptionParser();
    private static final ArgumentAcceptingOptionSpec<Path> INPUT =
                    PARSER.acceptsAll(
                                    Arrays.asList("i", "input"),
                                    "Input file(s). May be one or more of a .jar, a .class, or a directory."
                                                    + " Defaults to the current directory.")
                                    .withRequiredArg()
                                    .withValuesConvertedBy(TO_PATH)
                                    .defaultsTo(Paths.get("."))
                                    .withValuesSeparatedBy(
                                                    File.pathSeparatorChar);
    private static final ArgumentAcceptingOptionSpec<Path> CLASSPATH =
                    PARSER.acceptsAll(
                                    Arrays.asList("c", "classpath"),
                                    "The classpath for the input class(es)."
                                                    + " Used to fully construct the class graph, but will not be modified.")
                                    .withRequiredArg()
                                    .withValuesConvertedBy(TO_PATH)
                                    .withValuesSeparatedBy(
                                                    File.pathSeparatorChar);
    private static final ArgumentAcceptingOptionSpec<Path> OUTPUT =
                    PARSER.acceptsAll(
                                    Arrays.asList("o", "output"),
                                    "The output directory."
                                                    + " Input structure will be preserved."
                                                    + " As of now, this may not be the current directory.")
                                    .withRequiredArg()
                                    .withValuesConvertedBy(TO_PATH).required();
    private static final ArgumentAcceptingOptionSpec<Path> MANUAL_MAPPINGS =
                    PARSER.acceptsAll(
                                    Arrays.asList("m", "manual"),
                                    "The manual mapping(s)."
                                                    + " Used to add manual mappings to the mappings graph.")
                                    .withRequiredArg()
                                    .withValuesConvertedBy(TO_PATH)
                                    .withValuesSeparatedBy(
                                                    File.pathSeparatorChar);
    private static final List<ExitHandler> exitHandlers;
    static {
        ServiceLoader<ExitHandler> handlerLoader =
                        ServiceLoader.load(ExitHandler.class);
        exitHandlers = ImmutableList.copyOf(handlerLoader);
    }

    private Main() {}

    /**
     * Main method, loads arguments to pass in.
     * 
     * @param args - The arguments
     */
    public static void main(String[] args) {
        OptionSet opts;
        try {
            opts = PARSER.parse(args);
        } catch (OptionException error) {
            System.err.println(error.getMessage());
            throw exit(1);
        }
        Array<Path> inputSources = checkInput(opts);
        Array<Path> classpath = checkClasspath(opts);
        Path output = checkOutput(opts);
        for (Path check : FluentIterable.from(inputSources).append(classpath)) {
            if (Files.isDirectory(check)) {
                // output may not share directory
                checkADoesntStartWithB(check, output);
                checkADoesntStartWithB(output, check);
            } else if (check.toString().endsWith(".jar")
                            || check.toString().endsWith(".class")) {
                // file may not be in output directory
                checkADoesntStartWithB(check, output);
            } else {
                // not allowed
                throw new IllegalArgumentException("Invalid file: " + check);
            }
        }
        Array<BytecodeSupplier> transformBytecode =
                        BytecodeSupplierFactory.supplyBytecode(inputSources);
        Array<BytecodeSupplier> classpathBytecode =
                        BytecodeSupplierFactory.supplyBytecode(classpath);
        if (transformBytecode.isEmpty()) {
            System.err.println("Nothing to do.");
            throw exit(0);
        }
        ProcessEntry entry =
                        new ProcessEntry(Iterables.concat(transformBytecode,
                                        classpathBytecode),
                                        checkManualMappings(opts));
        entry.process();
    }

    private static void checkADoesntStartWithB(Path a, Path b) {
        checkState(!a.startsWith(b), "%s may not start with %s", a, b);
    }

    private static Array<Path> checkInput(OptionSet opts) {
        List<Path> inputs = opts.valuesOf(INPUT);
        for (Path path : inputs) {
            checkArgument(Files.exists(path), "%s doesn't exist",
                            path.toAbsolutePath());
        }
        return Array.iterableArray(inputs);
    }

    private static Array<Path> checkClasspath(OptionSet opts) {
        List<Path> classpath = opts.valuesOf(CLASSPATH);
        for (Path path : classpath) {
            checkArgument(Files.exists(path), "%s doesn't exist",
                            path.toAbsolutePath());
        }
        return Array.iterableArray(classpath);
    }

    private static Path checkOutput(OptionSet opts) {
        try {
            return opts.valueOf(OUTPUT);
        } catch (OptionException tooMany) {
            System.err.println(tooMany.getMessage());
            throw exit(1);
        }
    }

    private static Array<Path> checkManualMappings(OptionSet opts) {
        List<Path> mappings = opts.valuesOf(MANUAL_MAPPINGS);
        for (Path path : mappings) {
            checkArgument(Files.exists(path), "%s doesn't exist",
                            path.toAbsolutePath());
        }
        return Array.iterableArray(mappings);
    }

    /**
     * Normally calls System.exit, but gets replaced by tests to return a known
     * Error.
     * 
     * @param code - The exit code
     * @return An error to throw, under test conditions
     */
    private static Error exit(int code) {
        if (exitHandlers.size() > 0) {
            // use a exit handler
            return exitHandlers.get(0).exit(code);
        }
        System.exit(code);
        // doesn't return, actually unreachable code
        return null;
    }
}
