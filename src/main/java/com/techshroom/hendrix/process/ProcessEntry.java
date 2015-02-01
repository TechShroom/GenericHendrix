package com.techshroom.hendrix.process;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.byteio.BytecodeSupplier;

import fj.data.Array;

/**
 * Entry point for the processing of classes. All you need is the classes.
 * 
 * @author Kenzie Togami
 */
public final class ProcessEntry {
    private final ImmutableList<BytecodeSupplier> classesIn;
    private List<ClassNodeData> classNodes = new ArrayList<>();

    /**
     * Create a new entry point for processing.
     * 
     * @param input - The classes to process
     * @param manualMappings - The mappings provided by the user
     */
    public ProcessEntry(Iterable<BytecodeSupplier> input,
                    Array<Path> manualMappings) {
        this.classesIn = ImmutableList.copyOf(input);
    }

    /**
     * Get the input class data.
     * 
     * @return The input class data
     */
    public List<BytecodeSupplier> getClassesIn() {
        return this.classesIn;
    }

    /**
     * Process all of the classes given to the entry point.
     */
    public void process() {
        loadAllClassNodes();
    }

    private void loadAllClassNodes() {
        for (BytecodeSupplier clazz : this.classesIn) {
            ClassReader reader = new ClassReader(clazz.bytecode());
            ClassNode build = new ClassNode(Opcodes.ASM5);
            reader.accept(build, 0);
            this.classNodes.add(ClassNodeData.create(build, clazz));
        }
        for (ClassNodeData classData : this.classNodes) {
            System.err.println(classData);
            System.err.println(classData.getClassNode().name);
            for (FieldNode node : classData.getClassNode().fields) {
                System.err.println(node.desc + " " + node.name + "<"
                                + node.signature + ">");
            }
            for (MethodNode node : classData.getClassNode().methods) {
                System.err.println(node.desc + "() aka " + node.name);
            }
        }
    }
}
