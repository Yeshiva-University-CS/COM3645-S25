package edu.yu.compilers.backend.irgen;

import java.util.List;

import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.Operand.Label;
import edu.yu.compilers.intermediate.ir.Operand.Temporary;
import edu.yu.compilers.intermediate.ir.Operand.Variable;
import edu.yu.compilers.intermediate.ir.Operator;
import edu.yu.compilers.intermediate.ir.Tuple;

/**
 * The TupleIRFactory class creates IR tuples.
 * This factory is used by TupleIRBuilder to construct the intermediate
 * representation.
 */
public class TupleFactory {
    private static final Label PROGRAM_LABEL = new Label("__program__");

    /**
     * Create a tuple for the program start.
     * 
     * @return the created tuple
     */
    public static Tuple createProgram() {
        return new Tuple(Operator.PROGRAM, PROGRAM_LABEL);
    }

    /**
     * Create a tuple for the end of the program.
     * 
     * @return the created tuple
     */
    public static Tuple createEndProgram() {
        return new Tuple(Operator.END_PROGRAM, PROGRAM_LABEL);
    }

    /**
     * Create a tuple for an assignment.
     * 
     * @param target the target operand
     * @param source the source operand
     * @return the created tuple
     */
    public static Tuple createAssign(Operand target, Operand source) {
        return new Tuple(Operator.ASSIGN, target, source);
    }

    /**
     * Create a tuple for a function declaration with name.
     * 
     * @param funcLabel the label of the function
     * @return the created tuple
     */
    public static Tuple createFunction(Label funcLabel) {
        return new Tuple(Operator.FUNCTION, funcLabel);
    }

    /**
     * Create a tuple for a function parameter.
     * 
     * @param param the parameter variable
     * @return the created tuple
     */
    public static Tuple createParam(Variable param) {
        return new Tuple(Operator.PARAM, param);
    }

    /**
     * Create a tuple for end of function with a label.
     * This ensures we use the same label for FUNCTION and END_FUNCTION.
     * 
     * @param funcLabel the label of the function to end
     * @return the created tuple
     */
    public static Tuple createEndFunction(Label funcLabel) {
        return new Tuple(Operator.END_FUNCTION, funcLabel);
    }

    /**
     * Create a binary operation.
     * 
     * @param op     the operator
     * @param result the result operand
     * @param left   the left operand
     * @param right  the right operand
     * @return the created tuple
     */
    public static Tuple createBinaryOp(Operator op, Temporary result, Operand left, Operand right) {
        return new Tuple(op, result, left, right);
    }

    /**
     * Create a unary operation.
     * 
     * @param op     the operator
     * @param result the result operand
     * @param expr   the expression operand
     * @return the created tuple
     */
    public static Tuple createUnaryOp(Operator op, Temporary result, Operand expr) {
        return new Tuple(op, result, expr);
    }

    /**
     * Create a function call.
     * 
     * @param result the result operand
     * @param func   the function operand
     * @param args   the argument operands
     * @return the created tuple
     */
    public static Tuple createCall(Temporary result, Operand func, List<Operand> args) {
        Tuple tuple = new Tuple(Operator.CALL, result, func);
        for (Operand arg : args) {
            tuple.addOperand(arg);
        }
        return tuple;
    }

    /**
     * Create a print statement.
     * 
     * @param expr the expression to print
     * @return the created tuple
     */
    public static Tuple createPrint(Operand expr) {
        return new Tuple(Operator.PRINT, expr);
    }

    /**
     * Create an if statement.
     * 
     * @param condition the condition expression
     * @param label     the target label if condition is false
     * @return the created tuple
     */
    public static Tuple createIf(Operand condition, Label label) {
        return new Tuple(Operator.IF, condition, label);
    }

    /**
     * Create a label.
     * 
     * @param label the label operand
     * @return the created tuple
     */
    public static Tuple createLabel(Label label) {
        return new Tuple(Operator.LABEL, label);
    }

    /**
     * Create an unconditional jump.
     * 
     * @param label the destination label
     * @return the created tuple
     */
    public static Tuple createGoto(Label label) {
        return new Tuple(Operator.GOTO, label);
    }

    /**
     * Create a return statement.
     * 
     * @param expr the return expression (or null)
     * @return the created tuple
     */
    public static Tuple createReturn(Operand expr) {
        if (expr != null) {
            return new Tuple(Operator.RETURN, expr);
        } else {
            return new Tuple(Operator.RETURN);
        }
    }

}
