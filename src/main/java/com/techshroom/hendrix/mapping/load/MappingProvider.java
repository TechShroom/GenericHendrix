package com.techshroom.hendrix.mapping.load;

import java.util.Iterator;

import com.techshroom.hendrix.mapping.GenericMapping;

/**
 * Represents a mapping provider, which provides {@link GenericMapping
 * GenericMappings} for Hendrix to map.
 * 
 * @author Kenzie Togami
 */
public interface MappingProvider extends Iterable<GenericMapping> {

    /**
     * Gets the Iterator of generic mappings.
     * 
     * @return The Iterator of generic mappings
     */
    @Override
    Iterator<GenericMapping> iterator();

}
