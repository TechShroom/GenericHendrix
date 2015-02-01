package com.techshroom.hendrix.test;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import com.techshroom.hendrix.BytecodeSupplierFactory;
import com.techshroom.hendrix.asmsucks.FieldNode_;
import com.techshroom.hendrix.byteio.BytecodeSupplier;
import com.techshroom.hendrix.process.GenericFieldSolver;

import fj.data.Array;

/**
 * Tests for {@linkplain GenericFieldSolver}.
 * 
 * @author Kenzie Togami
 */
public class FieldSolverTest implements TestConstants {
    /**
     * Checks that making a generic field solver doesn't modify the field
     * values.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void fieldPassesUnchanged() throws Exception {
        Array<BytecodeSupplier> suppliers =
                        BytecodeSupplierFactory.supplyBytecode(Array
                                        .single(Paths.get(CLASSES_FOLDER)));
        boolean testedSomething = false;
        for (BytecodeSupplier supplier : suppliers) {
            ClassReader reader = new ClassReader(supplier.bytecode());
            ClassNode build = new ClassNode(Opcodes.ASM5);
            reader.accept(build, 0);
            List<FieldNode> nodes = build.fields;
            if (nodes.isEmpty()) {
                continue;
            } else {
                for (FieldNode fieldNode : nodes) {
                    FieldNode expected = copy(fieldNode);
                    FieldNode result =
                                    GenericFieldSolver.forField(build,
                                                    fieldNode).getSolvedField();
                    testedSomething = true;
                    try {
                        assertTrue("inequal fields",
                                        FieldNode_.equal(expected, result));
                    } catch (AssertionError failed) {
                        FieldNode_.printNodes(expected, result);
                        throw failed;
                    }
                }
            }
        }
        assertTrue("no fields to test", testedSomething);
    }

    private FieldNode copy(FieldNode f) {
        FieldNode fv =
                        new FieldNode(Opcodes.ASM5, f.access, f.name, f.desc,
                                        f.signature, f.value);
        int i, n;
        n = f.visibleAnnotations == null ? 0 : f.visibleAnnotations.size();
        for (i = 0; i < n; ++i) {
            AnnotationNode an = f.visibleAnnotations.get(i);
            an.accept(fv.visitAnnotation(an.desc, true));
        }
        n = f.invisibleAnnotations == null ? 0 : f.invisibleAnnotations.size();
        for (i = 0; i < n; ++i) {
            AnnotationNode an = f.invisibleAnnotations.get(i);
            an.accept(fv.visitAnnotation(an.desc, false));
        }
        n =
                        f.visibleTypeAnnotations == null ? 0
                                        : f.visibleTypeAnnotations.size();
        for (i = 0; i < n; ++i) {
            TypeAnnotationNode an = f.visibleTypeAnnotations.get(i);
            an.accept(fv.visitTypeAnnotation(an.typeRef, an.typePath, an.desc,
                            true));
        }
        n =
                        f.invisibleTypeAnnotations == null ? 0
                                        : f.invisibleTypeAnnotations.size();
        for (i = 0; i < n; ++i) {
            TypeAnnotationNode an = f.invisibleTypeAnnotations.get(i);
            an.accept(fv.visitTypeAnnotation(an.typeRef, an.typePath, an.desc,
                            false));
        }
        n = f.attrs == null ? 0 : f.attrs.size();
        for (i = 0; i < n; ++i) {
            fv.visitAttribute(f.attrs.get(i));
        }
        fv.visitEnd();
        return fv;
    }
}
