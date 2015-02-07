package com.techshroom.hendrix.process;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import autovalue.shaded.com.google.common.common.collect.ImmutableMap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.techshroom.hendrix.SharedData;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;
import com.techshroom.hendrix.asmsucks.MethodDescriptor;
import com.techshroom.hendrix.byteio.BytecodeConsumer;
import com.techshroom.hendrix.byteio.BytecodeSupplier;
import com.techshroom.hendrix.mapping.ClassMapping;
import com.techshroom.hendrix.mapping.FieldMapping;
import com.techshroom.hendrix.mapping.GenericMapping;
import com.techshroom.hendrix.mapping.MethodMapping;
import com.techshroom.hendrix.mapping.load.MappingProvider;

/**
 * Entry point for the processing of classes. All you need is the classes.
 * 
 * @author Kenzie Togami
 */
public final class ProcessEntry {
    static final Joiner SLASH = Joiner.on('/');
    private final List<BytecodeSupplier> classesIn;
    /**
     * Mapping from class -> mapping
     */
    private final Map<ClassDescriptor, ClassMapping> classMappings;
    /**
     * Mapping from class + field name -> mapping
     */
    private final Table<ClassDescriptor, String, FieldMapping> fieldMappings;
    /**
     * Mapping from class + method
     */
    private final Map<MethodDescriptor, MethodMapping> methodMappings;
    private final Map<BytecodeSupplier, ClassWriter> results = new HashMap<>();

    /**
     * Create a new entry point for processing.
     * 
     * @param input - The classes to process
     * @param mappings - The mappings provided
     */
    public ProcessEntry(Iterable<? extends BytecodeSupplier> input,
                    Iterable<? extends MappingProvider> mappings) {
        this.classesIn = ImmutableList.copyOf(input);
        Iterable<GenericMapping> allMappings = Iterables.concat(mappings);
        ImmutableMap.Builder<ClassDescriptor, ClassMapping> classMap =
                        ImmutableMap.builder();
        ImmutableMap.Builder<MethodDescriptor, MethodMapping> methodMap =
                        ImmutableMap.builder();
        ImmutableTable.Builder<ClassDescriptor, String, FieldMapping> fieldTable =
                        ImmutableTable.builder();
        for (GenericMapping mapping : allMappings) {
            if (mapping instanceof ClassMapping) {
                ClassMapping cMap = (ClassMapping) mapping;
                classMap.put(cMap.getClassName(), cMap);
            } else if (mapping instanceof ClassMapping) {
                ClassMapping cMap = (ClassMapping) mapping;
                classMap.put(cMap.getClassName(), cMap);
            } else if (mapping instanceof ClassMapping) {
                ClassMapping cMap = (ClassMapping) mapping;
                classMap.put(cMap.getClassName(), cMap);
            } else {
                System.err.println("Unhandled mapping type '"
                                + mapping.getClass().getName() + "'");
            }
        }
        this.classMappings = classMap.build();
        this.methodMappings = methodMap.build();
        this.fieldMappings = fieldTable.build();
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
        visitClassesAndApplyMappings();
        dumpMappings();
    }

    private void dumpMappings() {
        for (BytecodeSupplier clazz : this.classesIn) {
            BytecodeConsumer classOut = clazz.getConsumer();
            classOut.bytecode(this.results.get(clazz).toByteArray());
        }
    }

    private void visitClassesAndApplyMappings() {
        for (BytecodeSupplier clazz : this.classesIn) {
            ClassReader reader = new ClassReader(clazz.bytecode());
            ClassWriter writer = new ClassWriter(reader, SharedData.NO_FLAGS);
            reader.accept(new ClassVisitor(SharedData.ASM_VERSION, writer) {
                private ClassDescriptor classRef;

                @Override
                public void visit(int version, int access, String name,
                                String signature, String superName,
                                String[] interfaces) {
                    checkOutOfClass();
                    this.classRef =
                                    ClassDescriptor.fromDescriptorString("L"
                                                    + name + ";");
                    ClassMapping mapping =
                                    ProcessEntry.this.classMappings
                                                    .get(this.classRef);
                    if (mapping != null) {
                        if (signature != null

                                        && !ClassDescriptor
                                                        .fromDescriptorString(
                                                                        signature)
                                                        .equals(mapping.getGeneric())) {
                            System.err.println("Warning: overriding class signature '"
                                            + signature
                                            + "' with '"
                                            + mapping.getGeneric()
                                                            .toDescriptorString()
                                            + "' for " + name);
                        }
                        signature = mapping.getGeneric().toDescriptorString();
                    }
                    super.visit(version, access, name, signature, superName,
                                    interfaces);
                }

                @Override
                public FieldVisitor visitField(int access, String name,
                                String desc, String signature, Object value) {
                    checkInClass();
                    FieldMapping mapping =
                                    ProcessEntry.this.fieldMappings.get(
                                                    this.classRef, name);
                    if (mapping != null) {
                        if (signature != null

                                        && !ClassDescriptor
                                                        .fromDescriptorString(
                                                                        signature)
                                                        .equals(mapping.getGeneric())) {
                            System.err.println("Warning: overriding field signature '"
                                            + signature
                                            + "' with '"
                                            + mapping.getGeneric()
                                                            .toDescriptorString()
                                            + "' for " + name);
                        }
                        signature = mapping.getGeneric().toDescriptorString();
                    }
                    return super.visitField(access, name, desc, signature,
                                    value);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name,
                                String desc, String signature,
                                String[] exceptions) {
                    MethodMapping mapping =
                                    ProcessEntry.this.methodMappings
                                                    .get(makeMethodDescriptor(
                                                                    name, desc));
                    if (mapping != null) {
                        if (signature != null

                                        && !ClassDescriptor
                                                        .fromDescriptorString(
                                                                        signature)
                                                        .equals(mapping.getGeneric())) {
                            System.err.println("Warning: overriding method signature '"
                                            + signature
                                            + "' with '"
                                            + mapping.getGeneric()
                                                            .toDescriptorString()
                                            + "' for " + name + desc);
                        }
                        signature = mapping.getGeneric().toDescriptorString();
                    }
                    checkInClass();
                    return super.visitMethod(access, name, desc, signature,
                                    exceptions);
                }

                private MethodDescriptor makeMethodDescriptor(String name,
                                String desc) {
                    return MethodDescriptor.fromDescriptorString(SLASH
                                    .join(this.classRef.getPath())
                                    + '/'
                                    + name
                                    + desc);
                }

                @Override
                public void visitEnd() {
                    checkInClass();
                    this.classRef = null;
                    super.visitEnd();
                }

                private void checkInClass() {
                    checkArgument(this.classRef != null,
                                    "method called outside class parse");
                }

                private void checkOutOfClass() {
                    checkArgument(this.classRef == null,
                                    "method called inside class parse");
                }
            }, SharedData.NO_FLAGS);
            this.results.put(clazz, writer);
        }
    }
}
