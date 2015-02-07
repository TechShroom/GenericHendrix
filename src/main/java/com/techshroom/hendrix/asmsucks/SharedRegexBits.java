package com.techshroom.hendrix.asmsucks;

import java.util.regex.Pattern;

import com.google.common.base.Joiner;

final class SharedRegexBits {
    private static final Joiner PIPE = Joiner.on('|');
    static final String JAVA_ID_PART = "\\p{javaJavaIdentifierStart}"
                    + noneOrMore("\\p{javaJavaIdentifierPart}");
    static final String JAVA_ID = __javaIdGen(".");
    static final String JAVA_BYTE_ID = __javaIdGen("/");
    static final String CLASS_DESCRIPTOR = noneOrMore(literal("["))
                    + oneOf(nonGroup("[^L]"), nonGroup("L"
                                    + JAVA_BYTE_ID
                                    + possibly(nonGroup("<" + oneOrMore(".")
                                                    + ">")) + ";"));

    static String __javaIdGen(String sep) {
        return __joinBySep(JAVA_ID_PART, sep);
    }

    static String __joinBySep(String join, String sep) {
        return __joinBySepEx(join, join, sep);
    }

    static String __joinBySepEx(String join, String last, String sep) {
        return noneOrMore(nonGroup(join + literal(sep))) + last;
    }

    private static String groupIfLong(String input) {
        return (input.length() < 2) ? input : nonGroup(input);
    }

    static String group(String stuff) {
        return "(" + stuff + ")";
    }

    static String nonGroup(String stuff) {
        return group("?:" + stuff);
    }

    static String literal(String stuff) {
        return nonGroup(Pattern.quote(stuff));
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
        first = groupIfLong(first);
        second = groupIfLong(second);
        for (int i = 0; i < rest.length; i++) {
            rest[i] = groupIfLong(rest[i]);
        }
        return nonGroup(PIPE.join(first, second, (Object[]) rest));
    }

    private SharedRegexBits() {
        throw new AssertionError();
    }

}
