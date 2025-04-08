package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;

import java.util.List;

public class TACCodeGenerator extends CodeGenerator {

    private void emit(String code) {
        output.append(code).append("\n");
    }

    @Override
    public void emitFunctionStart(Tuple functionTuple, FunctionInfo info) {
        // emit("// Function: " + info.getName());

        // Emit the function label
        emit(functionTuple.getOperands().get(0) + ":");

        // // Emit function prologue
        // emit("// Function prologue");

        // // Emit parameter declarations
        // if (!info.getParameterNames().isEmpty()) {
        //     emit("// Parameters");
        //     for (String paramName : info.getParameterNames()) {
        //         emit("param " + paramName);
        //     }
        // }

        // // Emit local variable declarations
        // if (info.getLocalVariables() != null && !info.getLocalVariables().isEmpty()) {
        //     emit("// Local variables");
        //     for (Operand.Variable var : info.getLocalVariables()) {
        //         emit("declare " + var.getName());
        //     }
        // }

        // emit("// Begin function body");
    }

    @Override
    public void emitFunctionEnd(Tuple endFunctionTuple, FunctionInfo info) {
        emit("// Function epilogue");

        // If there's no explicit return, add a default return
        emit("return");

        // End the function
        emit(endFunctionTuple.getOperands().get(0) + "_end:");

        emit("// End of function: " + info.getName());
    }

    @Override
    protected void emitProgram(Tuple t) {
        emit("// Program start");
    }

    @Override
    protected void emitFunction(Tuple t) {
        emit(t.getOperands().get(0) + ":");
    }

    @Override
    protected void emitParam(Tuple t) {
        emit("param " + t.getOperands().get(0));
    }

    @Override
    protected void emitEndFunction(Tuple t) {
        emit("// End function");
    }

    @Override
    protected void emitDeclare(Tuple t) {
        emit("declare " + t.getOperands().get(0));
    }

    @Override
    protected void emitAssign(Tuple t) {
        emit(t.getOperands().get(0) + " := " + t.getOperands().get(1));
    }

    @Override
    protected void emitAdd(Tuple t) {
        emitBinaryOp(t, "+");
    }

    @Override
    protected void emitSub(Tuple t) {
        emitBinaryOp(t, "-");
    }

    @Override
    protected void emitMul(Tuple t) {
        emitBinaryOp(t, "*");
    }

    @Override
    protected void emitDiv(Tuple t) {
        emitBinaryOp(t, "/");
    }

    @Override
    protected void emitAnd(Tuple t) {
        emitBinaryOp(t, "and");
    }

    @Override
    protected void emitOr(Tuple t) {
        emitBinaryOp(t, "or");
    }

    @Override
    protected void emitEq(Tuple t) {
        emitBinaryOp(t, "==");
    }

    @Override
    protected void emitNeq(Tuple t) {
        emitBinaryOp(t, "!=");
    }

    @Override
    protected void emitGt(Tuple t) {
        emitBinaryOp(t, ">");
    }

    @Override
    protected void emitGte(Tuple t) {
        emitBinaryOp(t, ">=");
    }

    @Override
    protected void emitLt(Tuple t) {
        emitBinaryOp(t, "<");
    }

    @Override
    protected void emitLte(Tuple t) {
        emitBinaryOp(t, "<=");
    }

    private void emitBinaryOp(Tuple t, String op) {
        List<Operand> ops = t.getOperands();
        emit(ops.get(0) + " := " + ops.get(1) + " " + op + " " + ops.get(2));
    }

    @Override
    protected void emitNot(Tuple t) {
        emit(t.getOperands().get(0) + " := not " + t.getOperands().get(1));
    }

    @Override
    protected void emitTemp(Tuple t) {
        emit(t.getOperands().get(0) + " := temp " + t.getOperands().get(1));
    }

    @Override
    protected void emitIf(Tuple t) {
        emit("if " + t.getOperands().get(0) + " == 0 goto " + t.getOperands().get(1));
    }

    @Override
    protected void emitGoto(Tuple t) {
        emit("goto " + t.getOperands().get(0));
    }

    @Override
    protected void emitLabel(Tuple t) {
        emit(t.getOperands().get(0) + ":");
    }

    @Override
    protected void emitReturn(Tuple t) {
        if (t.getOperands().isEmpty())
            emit("return");
        else
            emit("return " + t.getOperands().get(0));
    }

    @Override
    protected void emitPrint(Tuple t) {
        emit("print " + t.getOperands().get(0));
    }

    @Override
    protected void emitCall(Tuple t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getOperands().get(0)).append(" := call ").append(t.getOperands().get(1));
        if (t.getOperands().size() > 2) {
            sb.append(" with args ");
            for (int i = 2; i < t.getOperands().size(); i++) {
                sb.append(t.getOperands().get(i));
                if (i < t.getOperands().size() - 1)
                    sb.append(", ");
            }
        }
        emit(sb.toString());
    }
}
