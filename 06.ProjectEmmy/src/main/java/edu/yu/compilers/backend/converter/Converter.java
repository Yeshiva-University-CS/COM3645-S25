package edu.yu.compilers.backend.converter;

import java.util.Hashtable;

import antlr4.EmmyBaseVisitor;

/**
 * Convert Emmy programs to Java.
 */
public class Converter extends EmmyBaseVisitor<Object> {

    // Map an Emmy datatype name to the Java datatype name.
    private static final Hashtable<String, String> typeNameTable;

    static {
        typeNameTable = new Hashtable<>();
        typeNameTable.put("integer", "int");
        typeNameTable.put("real", "double");
        typeNameTable.put("boolean", "boolean");
        typeNameTable.put("string", "String");
        typeNameTable.put("none", "Void");
    }

    private CodeGenerator code;

    private String programName;

    public String getProgramName() {
        return programName;
    }

}
