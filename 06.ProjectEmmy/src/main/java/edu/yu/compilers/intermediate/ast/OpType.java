package edu.yu.compilers.intermediate.ast;

/**
 * Enumeration of operator types used in expressions.
 */
public enum OpType {

    // Arithmetic operators
    ADD, SUB, MUL, DIV,

    // Relational operators
    EQ, NE, LT, LE, GT, GE,

    // Logical operators
    AND, OR, NOT;

    /**
     * Check if this operator is an arithmetic operator.
     * 
     * @return true if arithmetic operator, false otherwise
     */
    public boolean isArithmetic() {
        return this == ADD || this == SUB ||
                this == MUL || this == DIV;
    }

    /**
     * Check if this operator is a relational operator.
     * 
     * @return true if relational operator, false otherwise
     */
    public boolean isRelational() {
        return this == EQ || this == NE || this == LT ||
                this == LE || this == GT || this == GE;
    }

    /**
     * Check if this operator is a logical operator.
     * 
     * @return true if logical operator, false otherwise
     */
    public boolean isLogical() {
        return this == AND || this == OR || this == NOT;
    }

    /**
     * Check if this operator is a unary operator.
     * 
     * @return true if unary operator, false otherwise
     */
    public boolean isUnary() {
        return this == NOT; // Add SUB here if you want to include unary minus
    }

    /**
     * Check if this operator is a binary operator.
     * 
     * @return true if binary operator, false otherwise
     */
    public boolean isBinary() {
        return !isUnary();
    }

    /**
     * Check if this operator is a multiplicative operator.
     * 
     * @return true if multiplicative operator, false otherwise
     */
    public boolean isMultiplicative() {
        return this == MUL || this == DIV;
    }

    /**
     * Check if this operator is an additive operator.
     * 
     * @return true if additive operator, false otherwise
     */
    public boolean isAdditive() {
        return this == ADD || this == SUB;
    }

    /**
     * Check if this operator is a comparison operator.
     * 
     * @return true if comparison operator, false otherwise
     */
    public boolean isComparison() {
        return this == LT || this == LE || this == GT || this == GE;
    }

    /**
     * Check if this operator is an equality operator.
     * 
     * @return true if equality operator, false otherwise
     */
    public boolean isEquality() {
        return this == EQ || this == NE;
    }
}
