package com.techshroom.hendrix.process;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

import com.techshroom.hendrix.asmsucks.ClassDescriptor;

/**
 * Solves generics for a given field.
 * 
 * @author Kenzie Togami
 */
public final class GenericFieldSolver {
    private static final Map<String, GenericFieldSolver> fieldNameToSolver =
                    new HashMap<>();
    private static final ClassDescriptor OBJECT_DESCRIPTOR = ClassDescriptor
                    .fromSourcecodeReference("java.lang.Object");

    /**
     * Get the solver for the given field, creating it if it doesn't exist.
     * 
     * @param holder - The class that contains the field
     * @param field - The field to get the solver for
     * @return The solver for the field
     */
    public static GenericFieldSolver forField(ClassNode holder, FieldNode field) {
        String key = key(holder, field);
        GenericFieldSolver solver = fieldNameToSolver.get(key);
        if (solver == null) {
            fieldNameToSolver.put(key, solver =
                            new GenericFieldSolver(holder, field));
        }
        return solver;
    }

    private static String key(ClassNode c, FieldNode f) {
        return c.name + "." + f.name;
    }

    private final ClassNode holder;
    private final FieldNode field;
    private final boolean isGenericFieldType;
    private ClassDescriptor lowestCommonData = OBJECT_DESCRIPTOR;

    private GenericFieldSolver(ClassNode holder, FieldNode field) {
        checkArgument(holder.fields.contains(field),
                        "Holder does not contain field");
        this.holder = holder;
        this.field = field;
        this.isGenericFieldType = checkForGenericType();
        updateSignature();
    }

    /**
     * Returns {@code true} only when we're working with a generic type and
     * there is no in-place signature.
     */
    private boolean shouldUseData() {
        return this.isGenericFieldType && this.field.signature == null;
    }

    private boolean checkForGenericType() {
        String sig = this.field.signature;
        if (sig != null) {
            // signature data not missing -> generic
            ClassDescriptor desc = ClassDescriptor.fromDescriptorString(sig);
            // should be true for nearly every case, just a pre-caution
            if (desc.getGeneric().isPresent()) {
                // update signature
                this.lowestCommonData = desc.getGeneric().get();
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the signature of the field to the current LCD.
     */
    private void updateSignature() {
        if (this.isGenericFieldType) {
            // signature = desc<generic>
            String descBeforeSemiColon =
                            this.field.desc.substring(0,
                                            this.field.desc.length() - 1);
            this.field.signature =
                            descBeforeSemiColon + "<"
                                            + generateGenericDescriptor()
                                            + ">;";
        } else {
            // signature = null
            this.field.signature = null;
        }
    }

    private String generateGenericDescriptor() {
        return this.lowestCommonData.toDescriptorString();
    }

    /**
     * Add a field set to the graph for the solver.
     * 
     * @param setNode - The field instruction that is doing the set
     */
    public void addSet(FieldInsnNode setNode) {

    }

    /**
     * Get the field node that represents the currently solved field.
     * 
     * @return The field node that represents the currently solved field
     */
    public FieldNode getSolvedField() {
        return this.field;
    }

    @Override
    public String toString() {
        return "Solver(class=" + this.holder.name + ", field="
                        + this.field.name + ")";
    }
}
