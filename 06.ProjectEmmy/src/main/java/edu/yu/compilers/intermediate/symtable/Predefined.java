/**
 * <h1>Predefined</h1>
 * <p>Enter the predefined Pascal types, identifiers, and constants
 * into the symbol table.</p>
 * <p>Adapted from:</p>
 * <p>Copyright (c) 2020 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */

package edu.yu.compilers.intermediate.symtable;

import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.CONSTANT;
import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.TYPE;
import static edu.yu.compilers.intermediate.type.Typespec.Form.SCALAR;
import static edu.yu.compilers.intermediate.type.Typespec.Form.UNKNOWN;

import edu.yu.compilers.intermediate.type.Typespec;

public class Predefined {
    // Predefined types.
    public static Typespec integerType;
    public static Typespec realType;
    public static Typespec booleanType;
    public static Typespec stringType;
    public static Typespec noneType;

    // Predefined identifiers.
    public static SymTableEntry integerId;
    public static SymTableEntry realId;
    public static SymTableEntry booleanId;
    public static SymTableEntry stringId;
    public static SymTableEntry falseId;
    public static SymTableEntry trueId;
    public static SymTableEntry noneId;

    /**
     * Initialize a symbol table stack with predefined identifiers.
     *
     * @param symTableStack the symbol table stack to initialize.
     */
    public static void initialize(SymTableStack symTableStack) {
        initializeTypes(symTableStack);
        initializeConstants(symTableStack);
        initializeStandardRoutines(symTableStack);
    }

    /**
     * Initialize the predefined types.
     *
     * @param symTableStack the symbol table stack to initialize.
     */
    private static void initializeTypes(SymTableStack symTableStack) {
        // Type integer.
        integerId = symTableStack.enterLocal("integer", TYPE);
        integerType = new Typespec(SCALAR);
        integerType.setIdentifier(integerId);
        integerId.setType(integerType);

        // Type real.
        realId = symTableStack.enterLocal("real", TYPE);
        realType = new Typespec(SCALAR);
        realType.setIdentifier(realId);
        realId.setType(realType);

        // Type boolean.
        booleanId = symTableStack.enterLocal("boolean", TYPE);
        booleanType = new Typespec(SCALAR);
        booleanType.setIdentifier(booleanId);
        booleanId.setType(booleanType);

        // Type string.
        stringId = symTableStack.enterLocal("string", TYPE);
        stringType = new Typespec(SCALAR);
        stringType.setIdentifier(stringId);
        stringId.setType(stringType);

        // Undefined type.
        noneId = symTableStack.enterLocal("none", TYPE);
        noneType = new Typespec(UNKNOWN);
        noneType.setIdentifier(noneId);
        noneId.setType(noneType);
    }

    /**
     * Initialize the predefined constant.
     *
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeConstants(SymTableStack symTabStack) {
        // Boolean enumeration constant false.
        falseId = symTabStack.enterLocal("false", CONSTANT);
        falseId.setType(booleanType);
        falseId.setValue(false);

        // Boolean enumeration constant true.
        trueId = symTabStack.enterLocal("true", CONSTANT);
        trueId.setType(booleanType);
        trueId.setValue(true);
    }

    /**
     * Initialize the standard procedures and functions.
     *
     * @param symTableStack the symbol table stack to initialize.
     */
    private static void initializeStandardRoutines(SymTableStack symTableStack) {
    }

}
