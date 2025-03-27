package edu.yu.compilers.frontend.semantic;

public class Operator {

    public static Boolean applyOr(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
       return (Boolean) leftValue ||  (Boolean) rightValue;
    }

    public static Boolean applyAnd(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
       return (Boolean) leftValue &&  (Boolean) rightValue;
    }

    public static Boolean applyEQ(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
  
        return leftValue.equals(rightValue);
    }

    public static Boolean applyNE(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        return !leftValue.equals(rightValue);
    }

    public static Boolean applyGT(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        
        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble > rightDouble;
        }
        
        if (leftValue instanceof String && rightValue instanceof String) {
            return ((String)leftValue).compareTo((String)rightValue) > 0;
        }
        
        // If types are incomparable, return null
        return null;
    }
    
    public static Boolean applyGE(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        
        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble >= rightDouble;
        }
        
        if (leftValue instanceof String && rightValue instanceof String) {
            return ((String)leftValue).compareTo((String)rightValue) >= 0;
        }
        
        // If types are incomparable, return null
        return null;
    }
    
    public static Boolean applyLT(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        
        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble < rightDouble;
        }
        
        if (leftValue instanceof String && rightValue instanceof String) {
            return ((String)leftValue).compareTo((String)rightValue) < 0;
        }
        
        // If types are incomparable, return null
        return null;
    }
    
    public static Boolean applyLE(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        
        if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble <= rightDouble;
        }
        
        if (leftValue instanceof String && rightValue instanceof String) {
            return ((String)leftValue).compareTo((String)rightValue) <= 0;
        }
        
        // If types are incomparable, return null
        return null;
    }

    public static Object applyADD(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        else if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int leftInt = ((Number)leftValue).intValue();
            int rightInt = ((Number)rightValue).intValue();
            return leftInt + rightInt;
        }
        else if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble + rightDouble;
        }
        else if (leftValue instanceof String && rightValue instanceof String) {
            return (String)leftValue + (String)rightValue;
        }
        else {
            return null;
        }   
    }

    public static Object applySUB(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        else if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int leftInt = ((Number)leftValue).intValue();
            int rightInt = ((Number)rightValue).intValue();
            return leftInt - rightInt;
        }
        else if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble - rightDouble;
        }
        else {
            return null;
        }   
    }

    public static Object applyMUL(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        else if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int leftInt = ((Number)leftValue).intValue();
            int rightInt = ((Number)rightValue).intValue();
            return leftInt * rightInt;
        }
        else if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble * rightDouble;
        }
        else {
            return null;
        } 
    }

    public static Object applyDIV(Object leftValue, Object rightValue) {
        if (leftValue == null || rightValue == null) {
            return null;
        }
        else if (leftValue instanceof Integer && rightValue instanceof Integer) {
            int leftInt = ((Number)leftValue).intValue();
            int rightInt = ((Number)rightValue).intValue();
            return leftInt / rightInt;
        }
        else if (leftValue instanceof Number && rightValue instanceof Number) {
            double leftDouble = ((Number)leftValue).doubleValue();
            double rightDouble = ((Number)rightValue).doubleValue();
            return leftDouble / rightDouble;
        }
        else {
            return null;
        }
    }

    public static Object applyNOT(Object value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof Boolean) {
            return !((Boolean)value);
        }
        else {
            return null;
        }
    }

    public static Object applyNEG(Object value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof Integer) {
            return -((Number)value).intValue();
        }
        else if (value instanceof Double) {
            return -((Number)value).doubleValue();
        }
        else {
            return null;
        }
    }

}
