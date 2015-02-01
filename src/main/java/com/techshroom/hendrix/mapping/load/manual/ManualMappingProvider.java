package com.techshroom.hendrix.mapping.load.manual;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;
import com.techshroom.hendrix.asmsucks.MethodDescriptor;
import com.techshroom.hendrix.mapping.ClassMapping;
import com.techshroom.hendrix.mapping.FieldMapping;
import com.techshroom.hendrix.mapping.GenericMapping;
import com.techshroom.hendrix.mapping.MethodMapping;
import com.techshroom.hendrix.mapping.load.MappingProvider;

/**
 * Loads mappings from a file and provides them.
 * 
 * @author Kenzie Togami
 */
public class ManualMappingProvider implements MappingProvider {
    private static final Splitter SLASH = Splitter.on('/');
    private static final char FIELD = 'f', METHOD = 'm', CLASS = 'c';
    private static final Pattern MAPPING_ENTRY = Pattern.compile("^(" + FIELD
                    + "|" + METHOD + "|" + CLASS + ") (.+) (.+)$");
    private final Path mappingFile;
    private transient List<GenericMapping> mappings;

    /**
     * Creates a new mapping provider that uses the given path to create the
     * mappings.
     * 
     * @param file - The file to load mappings from
     */
    public ManualMappingProvider(Path file) {
        this.mappingFile = file;
    }

    @Override
    public Iterator<GenericMapping> iterator() {
        if (this.mappings == null) {
            lazyInit();
        }
        return this.mappings.iterator();
    }

    private void lazyInit() {
        try (Scanner s = new Scanner(this.mappingFile)) {
            List<GenericMapping> mappings = new ArrayList<>();
            while (s.hasNextLine()) {
                String ln = s.nextLine();
                mappings.add(readMapping(ln));
            }
            this.mappings = ImmutableList.copyOf(mappings);
        } catch (IOException e) {
            System.err.println("Error loading file '"
                            + this.mappingFile.toAbsolutePath().toString()
                            + "'");
            e.printStackTrace();
        }
    }

    private GenericMapping readMapping(String ln) {
        Matcher match = MAPPING_ENTRY.matcher(ln);
        if (match.matches()) {
            checkState(match.group(1).length() == 1,
                            "Type larger than one char in line %s", ln);
            char type = match.group(1).charAt(0);
            String name = match.group(2);
            ClassDescriptor generic =
                            ClassDescriptor.fromSourcecodeReference(match
                                            .group(3));
            if (type == CLASS) {
                return ClassMapping.Impl.of(generic,
                                ClassDescriptor.fromSourcecodeReference(name));
            } else if (type == METHOD) {
                return MethodMapping.Impl.of(generic,
                                MethodDescriptor.fromDescriptorString(name));
            } else if (type == FIELD) {
                StringBuilder classNameBuilder = new StringBuilder();
                String fieldName = null;
                for (Iterator<String> parts = SLASH.split(name).iterator(); parts
                                .hasNext();) {
                    String part = parts.next();
                    if (parts.hasNext()) {
                        // building class name
                        classNameBuilder.append('.').append(part);
                    } else {
                        // last is field name
                        fieldName = part;
                    }
                }
                verify(fieldName != null, "no field name");
                return FieldMapping.Impl.of(generic, ClassDescriptor
                                .fromSourcecodeReference(classNameBuilder
                                                .toString()), fieldName);
            }
        }
        throw new IllegalArgumentException("Line invalid: " + ln);
    }
}
