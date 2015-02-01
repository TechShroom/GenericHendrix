package com.techshroom.hendrix.mapping;

import com.techshroom.hendrix.asmsucks.ClassDescriptor;

/**
 * Represents a mapping inside a class.
 * 
 * @author Kenzie Togami
 */
public interface InClassMapping extends GenericMapping {

    /**
     * Gets the class that contains the object with the mapping.
     * 
     * @return The containing class
     */
    ClassDescriptor getContainingClass();

}
