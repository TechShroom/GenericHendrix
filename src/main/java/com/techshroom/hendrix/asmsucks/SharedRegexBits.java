package com.techshroom.hendrix.asmsucks;

import java.util.regex.Pattern;

import com.google.common.base.Joiner;

final class SharedRegexBits {
    private static final Joiner PIPE = Joiner.on('|');
    static final String JAVA_ID = __javaIdGen(".");
    static final String JAVA_BYTE_ID = __javaIdGen("/");
    static final String CLASS_DESCRIPTOR = noneOrMore(literal("["))
                    + oneOf("[^L]", "L"
                                    + JAVA_BYTE_ID
                                    + possibly(nonGroup("<" + oneOrMore(".")
                                                    + ">")) + ";");

    private static String __javaIdGen(String sep) {
        return noneOrMore(nonGroup("\\p{javaJavaIdentifierStart}"
                        + noneOrMore("\\p{javaJavaIdentifierPart}")
                        + literal(sep)))
                        + "\\p{javaJavaIdentifierStart}"
                        + noneOrMore("\\p{javaJavaIdentifierPart}");
    }

    static String group(String stuff) {
        return "(" + stuff + ")";
    }

    static String nonGroup(String stuff) {
        return group("?:" + stuff);
    }

    static String literal(String stuff) {
        return Pattern.quote(stuff);
    }

    static String possibly(String stuff) {
        return stuff + "?";
    }

    static String oneOrMore(String stuff) {
        return stuff + "+";
    }

    static String oneOrMoreLazy(String stuff) {
        return stuff + "+?";
    }

    static String noneOrMore(String stuff) {
        return stuff + "*";
    }

    static String noneOrMoreLazy(String stuff) {
        return stuff + "*?";
    }

    static String oneOf(String first, String second, String... rest) {
        return nonGroup(PIPE.join(first, second, (Object[]) rest));
    }

    private SharedRegexBits() {
        throw new AssertionError();
    }

}
