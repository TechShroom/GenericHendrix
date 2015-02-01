package com.techshroom.hendrix.process;

import org.objectweb.asm.tree.ClassNode;

import com.google.auto.value.AutoValue;
import com.techshroom.hendrix.byteio.BytecodeSupplier;

/**
 * Value class for a ClassNode and associated data.
 * 
 * @author Kenzie Togami
 */
@AutoValue
public abstract class ClassNodeData {

    /**
     * Creates a new ClassNodeData from the given values.
     * 
     * @param node - The ClassNode
     * @param source - The original data source for the ClassNode
     * @return The new ClassNodeData
     */
    public static final ClassNodeData create(ClassNode node,
                    BytecodeSupplier source) {
        return new AutoValue_ClassNodeData(node, source);
    }

    ClassNodeData() {}

    /**
     * Gets the class node.
     * 
     * @return The class node
     */
    public abstract ClassNode getClassNode();

    /**
     * Gets the source of the associated ClassNode.
     * 
     * @return The source of the associated ClassNode
     */
    public abstract BytecodeSupplier getSource();
}
