package com.techshroom.hendrix.mapping;

import com.google.auto.value.AutoValue;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;

/**
 * Represents a generic class mapping.
 * 
 * @author Kenzie Togami
 */
public interface ClassMapping extends GenericMapping {

    /**
     * Implementation for {@link ClassMapping}.
     * 
     * @author Kenzie Togami
     */
    @AutoValue
    abstract class Impl implements ClassMapping {

        /**
         * Creates a new ClassMapping. This is composed of the generic and the
         * class name.
         * 
         * @param generic - The generic value
         * @param className - The class name
         * @return The mapping
         */
        public static final ClassMapping of(ClassDescriptor generic,
                        ClassDescriptor className) {
            return new AutoValue_ClassMapping_Impl(generic, className);
        }

        Impl() {}

    }

    /**
     * Gets the class name.
     * 
     * @return The class name.
     */
    ClassDescriptor getClassName();

}
