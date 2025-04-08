package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;

/**
 * Abstract base class for backend code generators.
 */
public abstract class CodeGenerator {

    protected final StringBuilder output = new StringBuilder();

    /**
     * Process a single tuple and emit code.
     * 
     * @param tuple the tuple to process
     */
    public final void emitTuple(Tuple tuple) {
        switch (tuple.getOperator()) {
            case PROGRAM -> emitProgram(tuple);
            case FUNCTION -> emitFunction(tuple);
            case PARAM -> emitParam(tuple);
            case END_FUNCTION -> emitEndFunction(tuple);
            case ASSIGN -> emitAssign(tuple);
            case ADD -> emitAdd(tuple);
            case SUB -> emitSub(tuple);
            case MUL -> emitMul(tuple);
            case DIV -> emitDiv(tuple);
            case AND -> emitAnd(tuple);
            case OR -> emitOr(tuple);
            case NOT -> emitNot(tuple);
            case EQ -> emitEq(tuple);
            case NEQ -> emitNeq(tuple);
            case GT -> emitGt(tuple);
            case GTE -> emitGte(tuple);
            case LT -> emitLt(tuple);
            case LTE -> emitLte(tuple);
            case IF -> emitIf(tuple);
            case GOTO -> emitGoto(tuple);
            case LABEL -> emitLabel(tuple);
            case RETURN -> emitReturn(tuple);
            case PRINT -> emitPrint(tuple);
            case CALL -> emitCall(tuple);
            case TEMP -> emitTemp(tuple);
            default -> emitUnknown(tuple);
        }
    }

    /**
     * Process the start of a function. This is called once per function
     * with detailed function information.
     * 
     * @param functionTuple the FUNCTION tuple
     * @param info          the function information
     */
    public void emitFunctionStart(Tuple functionTuple, FunctionInfo info) {
        emitFunction(functionTuple);
    }

    /**
     * Process the end of a function. This is called once per function
     * with detailed function information.
     * 
     * @param endFunctionTuple the END_FUNCTION tuple
     * @param info             the function information
     */
    public void emitFunctionEnd(Tuple endFunctionTuple, FunctionInfo info) {
        emitEndFunction(endFunctionTuple);
    }

    /**
     * Get the generated output code.
     * 
     * @return the output code
     */
    public StringBuilder getOutput() {
        return output;
    }

    protected void emitProgram(Tuple tuple) {
    }

    protected void emitFunction(Tuple tuple) {
    }

    protected void emitParam(Tuple tuple) {
    }

    protected void emitEndFunction(Tuple tuple) {
    }

    protected void emitDeclare(Tuple tuple) {
    }

    protected void emitAssign(Tuple tuple) {
    }

    protected void emitAdd(Tuple tuple) {
    }

    protected void emitSub(Tuple tuple) {
    }

    protected void emitMul(Tuple tuple) {
    }

    protected void emitDiv(Tuple tuple) {
    }

    protected void emitAnd(Tuple tuple) {
    }

    protected void emitOr(Tuple tuple) {
    }

    protected void emitNot(Tuple tuple) {
    }

    protected void emitEq(Tuple tuple) {
    }

    protected void emitNeq(Tuple tuple) {
    }

    protected void emitGt(Tuple tuple) {
    }

    protected void emitGte(Tuple tuple) {
    }

    protected void emitLt(Tuple tuple) {
    }

    protected void emitLte(Tuple tuple) {
    }

    protected void emitIf(Tuple tuple) {
    }

    protected void emitGoto(Tuple tuple) {
    }

    protected void emitLabel(Tuple tuple) {
    }

    protected void emitReturn(Tuple tuple) {
    }

    protected void emitPrint(Tuple tuple) {
    }

    protected void emitCall(Tuple tuple) {
    }

    protected void emitTemp(Tuple tuple) {
    }

    protected void emitUnknown(Tuple tuple) {
    }
}
