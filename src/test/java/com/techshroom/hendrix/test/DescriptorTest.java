package com.techshroom.hendrix.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.techshroom.hendrix.asmsucks.ClassDescriptor;

/**
 * Test for {@linkplain ClassDescriptor}.
 * 
 * @author Kenzie Togami
 */
public final class DescriptorTest implements TestConstants {
    private final List<String> expectedPrimitivePath = ImmutableList.of();
    private final List<String> expectedNonPrimitivePath = ImmutableList.of(
                    "java", "lang", "Object");
    private final BiMap<Character, String> descToSrcTypeMap =
                    new ImmutableBiMap.Builder<Character, String>()
                                    .put(Character.valueOf('I'), "int")
                                    .put(Character.valueOf('V'), "void")
                                    .put(Character.valueOf('Z'), "boolean")
                                    .put(Character.valueOf('B'), "byte")
                                    .put(Character.valueOf('C'), "char")
                                    .put(Character.valueOf('S'), "short")
                                    .put(Character.valueOf('D'), "double")
                                    .put(Character.valueOf('F'), "float")
                                    .put(Character.valueOf('J'), "long")
                                    .build();

    /**
     * Tests for modification of a descriptor string going in+out.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void descriptorStringNotModified() throws Exception {
        String testWithSemi = "Ljava/lang/Object;";
        String testArray = "[Ljava/lang/Object;";
        String testGeneric = "Ljava/util/List<Ljava/lang/Object;>;";
        String testGenericArray = "Ljava/util/List<[Ljava/lang/Object;>;";
        String testPrimitive = "I";
        String testPrimitiveArray = "[I";
        String testPrimitiveMultiArray = "[[I";
        descStringNMTestImpl(testWithSemi);
        descStringNMTestImpl(testArray);
        descStringNMTestImpl(testGeneric);
        descStringNMTestImpl(testGenericArray);
        descStringNMTestImpl(testPrimitive);
        descStringNMTestImpl(testPrimitiveArray);
        descStringNMTestImpl(testPrimitiveMultiArray);
    }

    private void descStringNMTestImpl(String test) {
        assertEquals(test, ClassDescriptor.fromDescriptorString(test)
                        .toDescriptorString());
    }

    /**
     * Tests for modification of a sourcecode reference string going in+out.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void sourceRefStringNotModified() throws Exception {
        String test = "java.lang.Object";
        String testArray = "java.lang.Object[]";
        String testGeneric = "java.util.List<java.lang.Object>";
        String testGenericArray = "java.util.List<java.lang.Object[]>";
        String testPrimitive = "int";
        String testPrimitiveArray = "int[]";
        String testPrimitiveMultiArray = "int[][]";
        sourceRefStringNMTestImpl(test);
        sourceRefStringNMTestImpl(testArray);
        sourceRefStringNMTestImpl(testGeneric);
        sourceRefStringNMTestImpl(testGenericArray);
        sourceRefStringNMTestImpl(testPrimitive);
        sourceRefStringNMTestImpl(testPrimitiveArray);
        sourceRefStringNMTestImpl(testPrimitiveMultiArray);
    }

    private void sourceRefStringNMTestImpl(String test) {
        assertEquals(test, ClassDescriptor.fromSourcecodeReference(test)
                        .toSourcecodeRef());
    }

    /**
     * Test for swapping sourcecode references and descriptors.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void swapsSourceAndDescriptor() throws Exception {
        String stest = "java.lang.Object";
        String stestArray = "java.lang.Object[]";
        String stestGeneric = "java.util.List<java.lang.Object>";
        String stestGenericArray = "java.util.List<java.lang.Object[]>";
        String stestPrimitive = "int";
        String stestPrimitiveArray = "int[]";
        String stestPrimitiveMultiArray = "int[][]";
        String dtest = "Ljava/lang/Object;";
        String dtestArray = "[Ljava/lang/Object;";
        String dtestGeneric = "Ljava/util/List<Ljava/lang/Object;>;";
        String dtestGenericArray = "Ljava/util/List<[Ljava/lang/Object;>;";
        String dtestPrimitive = "I";
        String dtestPrimitiveArray = "[I";
        String dtestPrimitiveMultiArray = "[[I";
        swapTestImpl(stest, dtest);
        swapTestImpl(stestArray, dtestArray);
        swapTestImpl(stestGeneric, dtestGeneric);
        swapTestImpl(stestGenericArray, dtestGenericArray);
        swapTestImpl(stestPrimitive, dtestPrimitive);
        swapTestImpl(stestPrimitiveArray, dtestPrimitiveArray);
        swapTestImpl(stestPrimitiveMultiArray, dtestPrimitiveMultiArray);
    }

    private void swapTestImpl(String s, String d) {
        assertEquals("conversion from SRC -> DESC incorrect", d,
                        ClassDescriptor.fromSourcecodeReference(s)
                                        .toDescriptorString());
        assertEquals("conversion from DESC -> SRC incorrect", s,
                        ClassDescriptor.fromDescriptorString(d)
                                        .toSourcecodeRef());
    }

    /**
     * Tests that the description string array count is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void descArrayCountCorrect() throws Exception {
        String dtest = "[[I";
        assertEquals("DESC array count inequal", 2, ClassDescriptor
                        .fromDescriptorString(dtest).getArrayDepth());
    }

    /**
     * Tests that the sourcecode array count is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void srcArrayCountCorrect() throws Exception {
        String stest = "int[][]";
        assertEquals("SRC array count inequal", 2, ClassDescriptor
                        .fromSourcecodeReference(stest).getArrayDepth());
    }

    /**
     * Tests that the description string path is correct for primitives and
     * non-primitives.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void descPathCorrect() throws Exception {
        Set<Character> primitives = this.descToSrcTypeMap.keySet();
        for (Character character : primitives) {
            assertEquals("Wrong primitive path",
                            this.expectedPrimitivePath,
                            ClassDescriptor.fromDescriptorString(
                                            character.toString()).getPath());
        }
        String nprimitive = "Ljava/lang/Object;";
        assertEquals("Wrong non-primitive path", this.expectedNonPrimitivePath,
                        ClassDescriptor.fromDescriptorString(nprimitive)
                                        .getPath());
    }

    /**
     * Tests that the sourcecode reference path is correct for primitives and
     * non-primitives.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void srcPathCorrect() throws Exception {
        // Primitives check.
        BiMap<String, Character> reverse = this.descToSrcTypeMap.inverse();
        for (String primitiveType : reverse.keySet()) {
            assertEquals("Wrong primitive path",
                            this.expectedPrimitivePath,
                            ClassDescriptor.fromSourcecodeReference(
                                            primitiveType).getPath());
        }
        String nprimitive = "java.lang.Object";
        assertEquals("Wrong non-primitive path", this.expectedNonPrimitivePath,
                        ClassDescriptor.fromSourcecodeReference(nprimitive)
                                        .getPath());
    }

    /**
     * Tests that the descriptor string type is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void destTypeCorrect() throws Exception {
        // Primitives check.
        Set<Character> primitives = this.descToSrcTypeMap.keySet();
        for (Character character : primitives) {
            assertEquals("wrong type", character.charValue(), ClassDescriptor
                            .fromDescriptorString(character.toString())
                            .getType());
        }
        // Type check.
        String type = "Ljava/lang/Object;";
        assertEquals("wrong type", 'L',
                        ClassDescriptor.fromDescriptorString(type).getType());
    }

    /**
     * Tests that the sourcecode reference type is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void srcTypeCorrect() throws Exception {
        // Primitives check.
        BiMap<String, Character> reverse = this.descToSrcTypeMap.inverse();
        for (String primitiveType : reverse.keySet()) {
            assertEquals("wrong type",
                            reverse.get(primitiveType).charValue(),
                            ClassDescriptor.fromSourcecodeReference(
                                            primitiveType).getType());
        }
        // Type check.
        String type = "java.lang.Object";
        assertEquals("wrong type", 'L', ClassDescriptor
                        .fromSourcecodeReference(type).getType());
    }

    /**
     * Tests that the descriptor string generic is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void destGenericCorrect() throws Exception {
        String baseType = "Ljava/util/List";
        String generic = "Ljava/lang/Object;";
        String type = baseType + "<" + generic + ">;";
        Optional<ClassDescriptor> genericDesc =
                        ClassDescriptor.fromDescriptorString(type).getGeneric();
        assertTrue("generic missing", genericDesc.isPresent());
        assertEquals("generic incorrect", generic, genericDesc.get()
                        .toDescriptorString());
    }

    /**
     * Tests that the sourcecode reference generic is correct.
     * 
     * @throws Exception exceptions propagate
     */
    @Test
    public void srcGenericCorrect() throws Exception {
        String baseType = "java.util.List";
        String generic = "java.lang.Object";
        String type = baseType + "<" + generic + ">";
        Optional<ClassDescriptor> genericDesc =
                        ClassDescriptor.fromSourcecodeReference(type)
                                        .getGeneric();
        assertTrue("generic missing", genericDesc.isPresent());
        assertEquals("generic incorrect", generic, genericDesc.get()
                        .toSourcecodeRef());
    }
}
