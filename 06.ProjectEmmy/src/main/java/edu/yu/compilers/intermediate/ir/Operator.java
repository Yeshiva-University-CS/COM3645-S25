package edu.yu.compilers.intermediate.ir;

/**
 * The Operator enum defines all the operators used in the IR.
 */
public enum Operator {
    // Program structure
    PROGRAM, FUNCTION, PARAM, END_FUNCTION, END_PROGRAM,

    // Variable operations
    ASSIGN,

    // Arithmetic operations
    ADD, SUB, MUL, DIV,

    // Logical operations
    AND, OR, NOT,

    // Comparison operations
    EQ, NEQ, GT, GTE, LT, LTE,

    // Control flow
    IF, GOTO, LABEL, RETURN,

    // I/O operations
    PRINT,

    // Function calls
    CALL,

    // Temporary assignments
    TEMP
}
