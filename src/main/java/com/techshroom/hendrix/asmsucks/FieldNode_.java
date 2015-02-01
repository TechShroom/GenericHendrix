package com.techshroom.hendrix.asmsucks;

import java.lang.reflect.Field;

import org.objectweb.asm.tree.FieldNode;

import com.google.common.base.MoreObjects;

import autovalue.shaded.com.google.common.common.base.Objects;

/**
 * FieldNode utilities, because ASM doesn't provide them.
 * 
 * @author Kenzie Togami
 */
public final class FieldNode_ {
    private FieldNode_() {
        throw new AssertionError();
    }

    /**
     * Check for FieldNode equality.
     * 
     * @param a - FieldNode #1
     * @param b - FieldNode #2
     * @return {@code a == b}
     */
    public static boolean equal(FieldNode a, FieldNode b) {
        return a.equals(b)
                        || (a.access == b.access
                                        && Objects.equal(a.value, b.value)
                                        && Objects.equal(a.attrs, b.attrs)
                                        && Objects.equal(a.desc, b.desc)
                                        && Objects.equal(
                                                        a.invisibleAnnotations,
                                                        b.invisibleAnnotations)
                                        && Objects.equal(
                                                        a.invisibleTypeAnnotations,
                                                        b.invisibleTypeAnnotations)
                                        && Objects.equal(a.name, b.name)
                                        && Objects.equal(a.signature,
                                                        b.signature)
                                        && Objects.equal(a.visibleAnnotations,
                                                        b.visibleAnnotations) && Objects
                                            .equal(a.visibleTypeAnnotations,
                                                            b.visibleTypeAnnotations));
    }

    /**
     * {@link Object#toString()} implementation for FieldNode.
     * 
     * @param node - FieldNode to convert to a String
     * @return A String representation of the node
     */
    public static String toString(FieldNode node) {
        return MoreObjects
                        .toStringHelper(node)
                        .add("access", node.access)
                        .add("value", node.value)
                        .add("attrs", node.attrs)
                        .add("desc", node.desc)
                        .add("invisibleAnnotations", node.invisibleAnnotations)
                        .add("invisibleTypeAnnotations",
                                        node.invisibleTypeAnnotations)
                        .add("name", node.name)
                        .add("signature", node.signature)
                        .add("visibleAnnotations", node.visibleAnnotations)
                        .add("visibleTypeAnnotations",
                                        node.visibleTypeAnnotations).toString();
    }

    /**
     * Print the given nodes in a pretty way.
     * 
     * @param nodes - Nodes to print
     */
    public static void printNodes(FieldNode... nodes) {
        for (FieldNode fieldNode : nodes) {
            System.err.println(fieldNode + " {");
            for (Field f : fieldNode.getClass().getDeclaredFields()) {
                try {
                    System.err.println("\t" + f.getName() + " = "
                                    + f.get(fieldNode));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println("\t" + f.getName() + " = unknown");
                }
            }
            System.err.println("}");
        }
    }
}
