package edu.yu.compilers.intermediate.types;

import edu.yu.compilers.intermediate.symbols.Predefined;

public class TypeChecker {

    public static boolean isFunction(Typespec type) { return type.getForm() == Typespec.Form.FUNCTION; }

    public static boolean isDynamic(Typespec type) {
        return type.equals(Predefined.noneType);
    }

    public static boolean atLeastOneIsDynamic(Typespec type1, Typespec type2) {
        return isDynamic(type1) || isDynamic(type2);
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
                || atLeastOneIsDynamic(lhsType, rhsType)
                || isReal(lhsType) && isInteger(rhsType);
    }

    public static boolean areComparable(Typespec leftType, Typespec rightType) {
        return atLeastOneIsDynamic(leftType, rightType) ||
                areNumeric(leftType, rightType) ||
                areStrings(leftType, rightType);
    }

    public static boolean areEquatable(Typespec leftType, Typespec rightType) {
        return atLeastOneIsDynamic(leftType, rightType) || leftType.equals(rightType);
    }

    public static boolean supportsAdd(Typespec type) {
        return isDynamic(type) ||
                isNumeric(type) ||
                isString(type);
    }

    public static boolean supportsSubtract(Typespec type) {
        return isDynamic(type) || isNumeric(type);
    }

    public static boolean supportsMultDiv(Typespec type) {
        return isDynamic(type) || isNumeric(type);
    }

    public static boolean canAdd(Typespec leftType, Typespec rightType) {
        return atLeastOneIsDynamic(leftType, rightType) ||
                areNumeric(leftType, rightType) ||
                areStrings(leftType, rightType);
    }

    public static boolean canSubtract(Typespec leftType, Typespec rightType) {
        return atLeastOneIsDynamic(leftType, rightType) || areNumeric(leftType, rightType);
    }

    public static boolean canMultDiv(Typespec leftType, Typespec rightType) {
        return atLeastOneIsDynamic(leftType, rightType) || areNumeric(leftType, rightType);
    }

    public static boolean atLeastOneIsReal(Typespec type, Typespec type2) {
        return isReal(type) || isReal(type2);
    }
}
