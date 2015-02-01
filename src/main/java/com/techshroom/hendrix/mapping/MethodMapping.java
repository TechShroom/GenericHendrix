package com.techshroom.hendrix.mapping;

import com.google.auto.value.AutoValue;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;
import com.techshroom.hendrix.asmsucks.MethodDescriptor;

/**
 * Represents a generic method mapping.
 * 
 * @author Kenzie Togami
 */
public interface MethodMapping extends InClassMapping {

    /**
     * Implementation for {@link MethodMapping}.
     * 
     * @author Kenzie Togami
     */
    @AutoValue
    abstract class Impl implements MethodMapping {

        /**
         * Creates a new MethodMapping. This is composed of the generic and the
         * containing the method descriptor.
         * 
         * @param generic - The generic value
         * @param method - The method descriptor
         * @return The mapping
         */
        public static final MethodMapping of(ClassDescriptor generic,
                        MethodDescriptor method) {
            return new AutoValue_MethodMapping_Impl(generic,
                            method.getContainingClass(), method);
        }

        Impl() {}

    }

    /**
     * Gets the method descriptor.
     * 
     * @return The method descriptor
     */
    MethodDescriptor getMethod();

}
