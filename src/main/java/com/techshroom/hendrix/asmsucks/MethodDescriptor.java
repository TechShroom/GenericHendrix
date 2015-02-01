package com.techshroom.hendrix.asmsucks;

import static com.google.common.base.Verify.verify;
import static com.techshroom.hendrix.asmsucks.SharedRegexBits.*;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autovalue.shaded.com.google.common.common.base.Joiner;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

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
    private static final Splitter SLASH = Splitter.on('/');
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
                    .compile(group(JAVA_BYTE_ID) + literal("(")
                                    + group(noneOrMore(CLASS_DESCRIPTOR))
                                    + literal(")") + group(CLASS_DESCRIPTOR));

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
        List<ClassDescriptor> args =
                        FluentIterable.from(SEMICOLON.split(match.group(ARGS)))
                                        .transform(TO_CD).toList();
        ClassDescriptor returnType = TO_CD.apply(match.group(RETURN));
        StringBuilder classNameBuilder = new StringBuilder();
        String methodName = null;
        for (Iterator<String> parts = SLASH.split(match.group(NAME)).iterator(); parts
                        .hasNext();) {
            String part = parts.next();
            if (parts.hasNext()) {
                // building class name
                classNameBuilder.append('.').append(part);
            } else {
                // last is method name
                methodName = part;
            }
        }
        verify(methodName != null, "no method name");
        return fromRaw(ClassDescriptor.fromSourcecodeReference(classNameBuilder
                        .toString()), methodName, args, returnType);
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
        return getContainingClass().toSourcecodeRef() + "." + getName() + "("
                        + Joiner.on("").join(getArguments()) + ")"
                        + getReturnClass().toDescriptorString();
    }

}
