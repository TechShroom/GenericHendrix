package com.techshroom.hendrix.mapping;

import com.google.auto.value.AutoValue;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;

/**
 * Represents a generic field mapping.
 * 
 * @author Kenzie Togami
 */
public interface FieldMapping extends InClassMapping {

    /**
     * Implementation for {@link FieldMapping}.
     * 
     * @author Kenzie Togami
     */
    @AutoValue
    abstract class Impl implements FieldMapping {

        /**
         * Creates a new FieldMapping. This is composed of the generic, the
         * containing class and the field name.
         * 
         * @param generic - The generic value
         * @param className - The class name
         * @param fieldName - The field name
         * @return The mapping
         */
        public static final FieldMapping of(ClassDescriptor generic,
                        ClassDescriptor className, String fieldName) {
            return new AutoValue_FieldMapping_Impl(generic, className,
                            fieldName);
        }

        Impl() {}

    }

    /**
     * Gets the field name.
     * 
     * @return The field name.
     */
    String getFieldName();

}
