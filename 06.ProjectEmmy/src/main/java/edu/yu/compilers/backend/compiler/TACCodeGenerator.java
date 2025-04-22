package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.Operand.OperandType;
import edu.yu.compilers.intermediate.ir.Operator;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;

import java.util.List;

public class TACCodeGenerator extends CodeGenerator {
    private static final int INDENT_SPACES = 4;
    private String indent = "";
    private String programLabel = "";

    public TACCodeGenerator(TupleIR ir) {
        super(ir);
    }

    private void indent() {
        indent += " ".repeat(INDENT_SPACES);
    }

    private void emit(String code) {
        output.append(code).append("\n");
    }

    private void emitIndented(String code) {
        output.append(indent).append(code).append("\n");
    }

    @Override
    public void emitProgramStart() {
        // Emit global variable declarations before the program label
        List<TupleIR.VariableInfo> globalVars = ir.globalFunctionScope().getVariables().stream()
                .filter(v -> !v.isParameter())
                .sorted((v1, v2) -> v1.getName().compareTo(v2.getName()))
                .toList();

        // Emit global variables without indentation
        for (TupleIR.VariableInfo var : globalVars) {
            emit("global " + var.getName());
        }

        // Add an empty line before the program label if there are global variables
        if (!globalVars.isEmpty()) {
            emit("");
        }
    }

    @Override
    public void emitProgramEnd() {
        emit(programLabel + "end:");
        indent();
        emitIndented("halt\n");
    }

    @Override
    public void emitProgram(Tuple programTuple) {
        // Save and Emit the program label
        programLabel = programTuple.getOperands().get(0).toString();
        emit(programLabel + ":");
    }

    @Override
    public void emitEndProgram(Tuple endProgramTuple) {
        emitIndented("goto " + programLabel + "end");
        emit("");
    }

    @Override
    public void emitFunctionStart(Tuple functionTuple, FunctionInfo info) {
        // Emit the function label
        String functionLabel = functionTuple.getOperands().get(0).toString();
        emit(functionLabel + ":");

        // Set indentation to a single level (4 spaces)
        indent = " ".repeat(INDENT_SPACES);

        // Emit parameter declarations
        info.getVariables().stream()
                .filter(TupleIR.VariableInfo::isParameter)
                .sorted((v1, v2) -> v1.getName().compareTo(v2.getName()))
                .forEach(var -> emitIndented("param " + var.getName()));

        // Emit local variable declarations
        info.getVariables().stream()
                .filter(v -> !v.isParameter())
                .sorted((v1, v2) -> v1.getName().compareTo(v2.getName()))
                .forEach(var -> emitIndented("local " + var.getName()));
    }

    @Override
    public void emitFunctionEnd(Tuple endFunctionTuple, FunctionInfo info) {
        // Only emit a default return if the last tuple wasn't already a return
        List<Tuple> tuples = info.getTuples();
        if (!tuples.isEmpty() && tuples.get(tuples.size() - 2).getOperator() != Operator.RETURN) {
            emitIndented("return");
        }

        // Reset indentation
        indent = "";
        emit(".end");
        emit("");
    }

    @Override
    protected void emitAssign(Tuple t) {
        emitIndented(t.getOperands().get(0) + " := " + t.getOperands().get(1));
    }

    private void emitBinaryOp(Tuple t, String op) {
        List<Operand> ops = t.getOperands();
        emitIndented(ops.get(0) + " := " + ops.get(1) + " " + op + " " + ops.get(2));
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
        emitBinaryOp(t, "&&");
    }

    @Override
    protected void emitOr(Tuple t) {
        emitBinaryOp(t, "||");
    }

    @Override
    protected void emitNot(Tuple t) {
        emitIndented(t.getOperands().get(0) + " := !" + t.getOperands().get(1));
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

    @Override
    protected void emitIf(Tuple t) {
        emitIndented("if " + t.getOperands().get(0) + " goto " + t.getOperands().get(1));
    }

    @Override
    protected void emitGoto(Tuple t) {
        emitIndented("goto " + t.getOperands().get(0));
    }

    @Override
    protected void emitLabel(Tuple t) {
        // Save current indentation
        String savedIndent = indent;
        indent = "";

        emit(t.getOperands().get(0) + ":");

        // Restore indentation
        indent = savedIndent;
    }

    @Override
    protected void emitReturn(Tuple t) {
        if (t.getOperands().isEmpty()) {
            emitIndented("return");
        } else {
            emitIndented("return " + t.getOperands().get(0));
        }
    }

    @Override
    protected void emitPrint(Tuple t) {
        Operand operand = t.getOperands().get(0);

        if (isStringConstant(operand)) {
            // Special case for string constants - no need for format specifier
            emitIndented("printf " + operand);
        } else {
            String formatStr = getFormatSpecifier(operand);
            emitIndented("printf \"" + formatStr + "\", " + operand);
        }
    }

    /**
     * Checks if the operand is a string constant.
     *
     * @param operand the operand to check
     * @return true if the operand is a string constant
     */
    private boolean isStringConstant(Operand operand) {
        return operand instanceof Operand.Constant &&
                ((Operand.Constant) operand).getType() == Operand.OperandType.STRING;
    }

    /**
     * Helper method to get the appropriate format specifier for an operand.
     * Uses %?" for dynamic types that can't be determined at compile time.
     *
     * @param operand the operand to get a format specifier for
     * @return the format specifier string
     */
    private String getFormatSpecifier(Operand operand) {
        OperandType type = getOperandType(operand);

        switch (type) {
            case INTEGER:
                return "%d";
            case FLOAT:
                return "%f";
            case BOOLEAN:
                return "%s";
            case STRING:
                return "%s";
            case NONE:
                // For dynamic types, use "%?" to indicate the type is unknown at compile time
                return "%?";
            default:
                return "%?";
        }
    }

    /**
     * Gets the type of an operand regardless of its specific class.
     *
     * @param operand the operand to get the type from
     * @return the operand type, or NONE if not available
     */
    private OperandType getOperandType(Operand operand) {
        if (operand instanceof Operand.Constant) {
            return ((Operand.Constant) operand).getType();
        } else if (operand instanceof Operand.Variable) {
            return ((Operand.Variable) operand).getType();
        } else if (operand instanceof Operand.Temporary) {
            return ((Operand.Temporary) operand).getType();
        }

        return OperandType.NONE;
    }

    @Override
    protected void emitCall(Tuple t) {
        StringBuilder sb = new StringBuilder();

        // Handle return value if present
        if (t.getOperands().size() > 1) {
            sb.append(t.getOperands().get(0)).append(" := ");
        }

        // Add call and function name
        sb.append("call ").append(t.getOperands().get(1));

        // Add arguments if present
        if (t.getOperands().size() > 2) {
            sb.append("(");
            for (int i = 2; i < t.getOperands().size(); i++) {
                if (i > 2)
                    sb.append(", ");
                sb.append(t.getOperands().get(i));
            }
            sb.append(")");
        }

        emitIndented(sb.toString());
    }

    @Override
    protected void emitTemp(Tuple t) {
        emitIndented(t.getOperands().get(0) + " := " + t.getOperands().get(1));
    }

    @Override
    protected void emitUnknown(Tuple t) {
        emitIndented("// Unknown operation: " + t.getOperator());
    }
}
