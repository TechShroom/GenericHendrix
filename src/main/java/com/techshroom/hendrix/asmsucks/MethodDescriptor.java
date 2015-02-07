package com.techshroom.hendrix.asmsucks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.techshroom.hendrix.asmsucks.SharedRegexBits.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.SharedData;
import com.techshroom.hendrix.Util;

import fj.data.Array;

/**
 * A method descriptor describes a reference to a method. There's not a big
 * correlation to ASM as the method has no easily accessible relation in ASM.
 * <b>Note:</b> The {@code from*} methods are not very strict, and may accept
 * invalid inputs.
 * 
 * @author Kenzie Togami
 */
@AutoValue
public abstract class MethodDescriptor {
    private static final Splitter SEMICOLON = Splitter.on(';');
    private static final Function<String, ClassDescriptor> TO_CD =
                    new Function<String, ClassDescriptor>() {
                        @Override
                        public ClassDescriptor apply(String input) {
                            return ClassDescriptor.fromDescriptorString(input);
                        }
                    };
    private static final int NAME = 1, ARGS = 2, RETURN = 3;
    private static final Pattern DESC_METHOD_PAT = Pattern
                    .compile(group(__joinBySepEx(JAVA_ID_PART,
                                    oneOf(JAVA_ID_PART, "<init>", "<clinit>"),
                                    "/"))
                                    + literal("(")
                                    + group(noneOrMore(CLASS_DESCRIPTOR))
                                    + literal(")") + group(CLASS_DESCRIPTOR));
    private static final Function<ClassDescriptor, String> TO_GENERIC_REF =
                    new Function<ClassDescriptor, String>() {
                        @Override
                        public String apply(ClassDescriptor input) {
                            return input.toDescriptorString();
                        }
                    };

    /**
     * Creates a MethodDescriptor from a ClassNode and a MethodName.
     * 
     * @param clazz - The class node containing the method
     * @param method - The method node
     * @return The new MethodDescriptor
     */
    public static final MethodDescriptor fromClassAndMethodNode(
                    ClassNode clazz, MethodNode method) {
        return fromDescriptorString(clazz.name + "/" + method.name
                        + method.desc);
    }

    /**
     * Parses a descriptor string into a method descriptor.
     * 
     * <p>
     * Format:
     * {@code <fully qualified name, with slashes>([arguments as class descriptors...])<return
     * type, as a class descriptor>}
     * </p>
     * 
     * @param desc - The descriptor string. Format is explained above
     * @return The parsed method descriptor
     */
    public static final MethodDescriptor fromDescriptorString(String desc) {
        Matcher match = DESC_METHOD_PAT.matcher(desc);
        try {
            checkArgument(match.matches(), "Invalid method descriptor '%s'",
                            desc);
        } catch (IllegalArgumentException e) {
            if (SharedData.debug) {
                System.err.println("Matching against '"
                                + DESC_METHOD_PAT.pattern() + "'");
            }
            throw e;
        }
        List<ClassDescriptor> args =
                        FluentIterable.from(
                                        SEMICOLON.omitEmptyStrings().split(
                                                        match.group(ARGS)))
                                        .transform(TO_CD).toList();
        ClassDescriptor returnType = TO_CD.apply(match.group(RETURN));
        Array<String> classAndMethod =
                        Util.splitReplaceAndPopLast(match.group(NAME), '/', '.');
        return fromRaw(ClassDescriptor.fromSourcecodeReference(classAndMethod
                        .get(0)), classAndMethod.get(1), args, returnType);
    }

    /**
     * Create a new method descriptor from the raw argument data.
     * 
     * @param containingClass - The class that contains the method
     * @param name - The name of the method
     * @param arguments - The arguments of the method
     * @param returnClass - The return type
     * @return The created method descriptor
     */
    public static final MethodDescriptor fromRaw(
                    ClassDescriptor containingClass, String name,
                    List<ClassDescriptor> arguments, ClassDescriptor returnClass) {
        return new AutoValue_MethodDescriptor(containingClass, name,
                        ImmutableList.copyOf(arguments), returnClass);
    }

    MethodDescriptor() {}

    /**
     * Gets the class that contains this method.
     * 
     * @return The class that contains this method
     */
    public abstract ClassDescriptor getContainingClass();

    /**
     * Gets the name.
     * 
     * @return The name
     */
    public abstract String getName();

    /**
     * Gets the list of arguments.
     * 
     * @return The list of arguments
     */
    public abstract List<ClassDescriptor> getArguments();

    /**
     * Gets the return type.
     * 
     * @return The return type
     */
    public abstract ClassDescriptor getReturnClass();

    @Override
    public String toString() {
        return getContainingClass().toSourcecodeRef().replace('.', '/')
                        + "/"
                        + getName()
                        + "("
                        + Joiner.on("").join(
                                        Collections2.transform(getArguments(),
                                                        TO_GENERIC_REF)) + ")"
                        + getReturnClass().toDescriptorString();
    }

}
