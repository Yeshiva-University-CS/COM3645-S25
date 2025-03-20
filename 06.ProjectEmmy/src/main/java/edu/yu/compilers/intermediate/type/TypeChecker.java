package edu.yu.compilers.intermediate.type;

import edu.yu.compilers.intermediate.symtable.Predefined;

public class TypeChecker {

    public static boolean isNone(Typespec type) {
        return type.equals(Predefined.noneType);
    }

    public static boolean atLeastOneIsNone(Typespec type1, Typespec type2) {
        return isNone(type1) || isNone(type2);
    }

    public static boolean isInteger(Typespec type) {
        return type.equals(Predefined.integerType);
    }

    public static boolean areIntegers(Typespec type1, Typespec type2) {
        return isInteger(type1) && isInteger(type2);
    }

    public static boolean isReal(Typespec type) {
        return type.equals(Predefined.realType);
    }

    public static boolean areReals(Typespec type1, Typespec type2) {
        return isReal(type1) && isReal(type2);
    }

    public static boolean isString(Typespec type) {
        return type.equals(Predefined.stringType);
    }

    public static boolean areStrings(Typespec type1, Typespec type2) {
        return isString(type1) && isString(type2);
    }

    public static boolean isBoolean(Typespec type) {
        return type.equals(Predefined.booleanType);
    }

    public static boolean areBooleans(Typespec type1, Typespec type2) {
        return isBoolean(type1) && isBoolean(type2);
    }

    public static boolean isNumeric(Typespec type) {
        return isInteger(type) || isReal(type);
    }

    public static boolean areNumeric(Typespec type1, Typespec type2) {
        return isNumeric(type1) && isNumeric(type2);
    }

    public static boolean areAssignmentCompatible(Typespec lhsType, Typespec rhsType) {
        return lhsType.equals(rhsType)
                || atLeastOneIsNone(lhsType, rhsType)
                || isReal(lhsType) && isInteger(rhsType);
    }

    public static boolean areComparable(Typespec leftType, Typespec rightType) {
        return areNumeric(leftType, rightType) || areStrings(leftType, rightType);
    }

    public static boolean areEquatable(Typespec leftType, Typespec rightType) {
        return leftType.equals(rightType);
    }

    public static boolean supportsAdd(Typespec type) {
        return isNumeric(type) || isString(type);
    }

    public static boolean supportsSubract(Typespec type) {
        return isNumeric(type);
    }

    public static boolean supportsMultDiv(Typespec type) {
        return isNumeric(type);
    }

    public static boolean canAdd(Typespec leftType, Typespec rightType) {
        return areNumeric(leftType, rightType) || areStrings(leftType, rightType);
    }

    public static boolean canSubract(Typespec leftType, Typespec rightType) {
        return areNumeric(leftType, rightType);
    }

    public static boolean canMultDiv(Typespec leftType, Typespec rightType) {
        return areNumeric(leftType, rightType);
    }
}
