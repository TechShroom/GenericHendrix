package com.techshroom.hendrix.asmsucks;

import static com.google.common.base.Preconditions.*;
import static com.techshroom.hendrix.asmsucks.SharedRegexBits.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.SharedData;

/**
 * A class descriptor describes a reference to a class. This bridges the gap
 * between bytecode and sourcecode names. <b>Note:</b> The {@code from*} methods
 * are not very strict, and may accept invalid inputs.
 * 
 * @author Kenzie Togami
 */
@AutoValue
public abstract class ClassDescriptor {
    private static final Pattern DESC_PATTERN = Pattern.compile("^"
                    + group(noneOrMore(literal("["))) // array parts
                    + oneOf(group("[^L]"), group("L") // type char
                                    + group(oneOrMore("[^<;]")) // type data
                                    + possibly(nonGroup("<"
                                                    + group(oneOrMore(".")) // generic
                                                    + ">")) + ";") + "$");
    private static final Pattern SRC_PATTERN = Pattern.compile("^"
                    + group(JAVA_ID) // type data
                    + possibly(nonGroup("<" + group(oneOrMore(".")) // generic
                                    + ">"))
                    + group(noneOrMore(nonGroup(literal("[]")))) + "$"); // array
    private static final Pattern SLASH = Pattern.compile("/", Pattern.LITERAL),
                    DOT = Pattern.compile(".", Pattern.LITERAL);
    private static final Joiner DESC_PATH_CHAR = Joiner.on('/'),
                    SRC_PATH_CHAR = Joiner.on('.'), DESC_ARRAY_CHAR = Joiner
                                    .on('[').useForNull(""),
                    SRC_ARRAY_CHARS = Joiner.on("[]").useForNull("");

    /**
     * Creates a class descriptor from a descriptor string.
     * 
     * @param desc - The descriptor string
     * @return A new descriptor object
     */
    public static final ClassDescriptor fromDescriptorString(String desc) {
        Matcher match =
                        DESC_PATTERN.matcher(checkNotNull(desc,
                                        "descriptor string cannot be null"));
        try {
            checkArgument(match.matches(), "Invalid class descriptor '%s'",
                            desc);
        } catch (IllegalArgumentException e) {
            if (SharedData.debug) {
                System.err.println("Matching against '"
                                + DESC_PATTERN.pattern() + "'");
            }
            throw e;
        }
        int arrayDepth = descArrayDepth(match);
        char type = descStringType(match);
        List<String> path = descStringPath(match);
        ClassDescriptor generic = descGeneric(match);
        return new AutoValue_ClassDescriptor(arrayDepth, type, path,
                        Optional.fromNullable(generic));
    }

    /**
     * Creates a class descriptor from a sourcecode reference string.
     * 
     * @param sourceRef - The sourcecode reference string
     * @return A new descriptor object
     */
    public static final ClassDescriptor fromSourcecodeReference(String sourceRef) {
        Matcher match =
                        SRC_PATTERN.matcher(checkNotNull(sourceRef,
                                        "source reference string cannot be null"));
        try {
            checkArgument(match.matches(),
                            "Invalid class source reference '%s'", sourceRef);
        } catch (IllegalArgumentException e) {
            if (SharedData.debug) {
                System.err.println("Matching against '" + SRC_PATTERN.pattern()
                                + "'");
            }
            throw e;
        }
        int arrayDepth = srcArrayDepth(match);
        char type = srcStringType(match);
        List<String> path = srcStringPath(match);
        ClassDescriptor generic = srcGeneric(match);
        return new AutoValue_ClassDescriptor(arrayDepth, type, path,
                        Optional.fromNullable(generic));
    }

    /**
     * Creates a class descriptor from a Class object.
     * 
     * @param source - The source Class object
     * @return A new descriptor object
     */
    public static final ClassDescriptor fromClass(Class<?> source) {
        checkNotNull(source, "source class cannot be null");
        return fromDescriptorString(source.getName());
    }

    private static int descArrayDepth(Matcher desc) {
        return desc.group(1).length();
    }

    private static char descStringType(Matcher desc) {
        // 2 = primitive, 3 = Type
        String type = desc.group(2);
        if (type == null) {
            type = desc.group(3);
        }
        checkState(type.length() == 1, "Length of '%s' larger than 1", type);
        return type.charAt(0);
    }

    private static List<String> descStringPath(Matcher desc) {
        String primitive = desc.group(2);
        if (primitive != null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(SLASH.split(desc.group(4)));
    }

    private static ClassDescriptor descGeneric(Matcher desc) {
        String match = desc.group(5);
        if (match != null) {
            return fromDescriptorString(match);
        }
        return null;
    }

    private static int srcArrayDepth(Matcher sourceRef) {
        // Array depth is equal to the array parts "[]" over 2.
        return sourceRef.group(3).length() / 2;
    }

    private static char srcStringType(Matcher sourceRef) {
        String type = sourceRef.group(1);
        char car = 'L';
        if (type.equals("int")) {
            car = 'I';
        } else if (type.equals("void")) {
            car = 'V';
        } else if (type.equals("boolean")) {
            car = 'Z';
        } else if (type.equals("byte")) {
            car = 'B';
        } else if (type.equals("char")) {
            car = 'C';
        } else if (type.equals("short")) {
            car = 'S';
        } else if (type.equals("double")) {
            car = 'D';
        } else if (type.equals("float")) {
            car = 'F';
        } else if (type.equals("long")) {
            car = 'J';
        }
        return car;
    }

    private static List<String> srcStringPath(Matcher sourceRef) {
        char type = srcStringType(sourceRef);
        if (type != 'L') {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(DOT.split(sourceRef.group(1)));
    }

    private static ClassDescriptor srcGeneric(Matcher sourceRef) {
        String match = sourceRef.group(2);
        if (match != null) {
            return fromSourcecodeReference(match);
        }
        return null;
    }

    ClassDescriptor() {}

    /**
     * Gets the depth of the array. If this is not an array, {@code 0} will be
     * returned.
     * 
     * @return The depth of the array
     */
    public abstract int getArrayDepth();

    /**
     * Gets the type. This is in correspondence with the descriptor type
     * character.
     * 
     * @return The type
     */
    public abstract char getType();

    /**
     * Gets the parts of the fully qualified name. If the type is primitive then
     * the list is empty.
     * 
     * @return The parts of the fully qualified name
     */
    public abstract List<String> getPath();

    /**
     * Gets the generic descriptor.
     * 
     * @return The generic descriptor
     */
    public abstract Optional<ClassDescriptor> getGeneric();

    /**
     * Returns {@code true} if this descriptor is for a primitive class.
     * 
     * @return {@code true} if this descriptor is for a primitive class
     */
    public final boolean isPrimitive() {
        return getType() != 'L';
    }

    /**
     * Convert this descriptor to a descriptor string.
     * 
     * @return The generated descriptor string
     */
    public final String toDescriptorString() {
        StringBuilder build = new StringBuilder();
        // Add the array bits.
        if (getArrayDepth() > 0) {
            DESC_ARRAY_CHAR.appendTo(build, new Object[getArrayDepth() + 1]);
        }
        // Add the type.
        build.append(getType());
        if (getType() == 'L') {
            // Add path for Object types.
            DESC_PATH_CHAR.appendTo(build, getPath());
        }
        // Add the generic.
        if (getGeneric().isPresent()) {
            build.append('<').append(getGeneric().get().toDescriptorString())
                            .append('>');
        }
        // Add the semicolon.
        if (!isPrimitive()) {
            build.append(';');
        }
        return build.toString();
    }

    /**
     * Convert this descriptor to a sourcecode reference string.
     * 
     * @return The generated sourcecode reference string
     */
    public final String toSourcecodeRef() {
        StringBuilder build = new StringBuilder();
        // Add the type.
        char t = getType();
        if (t == 'I') {
            build.append("int");
        } else if (t == 'V') {
            build.append("void");
        } else if (t == 'Z') {
            build.append("boolean");
        } else if (t == 'B') {
            build.append("byte");
        } else if (t == 'C') {
            build.append("char");
        } else if (t == 'S') {
            build.append("short");
        } else if (t == 'D') {
            build.append("double");
        } else if (t == 'F') {
            build.append("float");
        } else if (t == 'J') {
            build.append("long");
        } else {
            // Add path for Object types.
            SRC_PATH_CHAR.appendTo(build, getPath());
        }
        // Add the generic.
        if (getGeneric().isPresent()) {
            build.append('<').append(getGeneric().get().toSourcecodeRef())
                            .append('>');
        }
        // Add the array bits.
        if (getArrayDepth() > 0) {
            SRC_ARRAY_CHARS.appendTo(build, new Object[getArrayDepth() + 1]);
        }
        return build.toString();
    }
}
