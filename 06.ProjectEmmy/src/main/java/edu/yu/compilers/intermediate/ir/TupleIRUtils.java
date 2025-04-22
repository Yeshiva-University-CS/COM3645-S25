package edu.yu.compilers.intermediate.ir;

import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;

public class TupleIRUtils {

    static public String printIR(TupleIR ir) {
        StringBuilder sb = new StringBuilder();

        FunctionInfo programScope = ir.globalFunctionScope();

        // --- Global Variables ---
        sb.append("// --- Global Variables ---\n");
        for (TupleIR.VariableInfo var : programScope.getVariables()) {
            sb.append("global ").append(var.getName()).append(" : ").append(var.getType()).append("\n");
        }
        sb.append("\n");

        // --- String Constants ---
        sb.append("\n// --- String Constants ---\n");
        for (String str : ir.getStringConstants()) {
            int id = ir.getStringConstant(str);
            sb.append("const str_").append(id).append(" = ").append("\"").append(str).append("\"\n");
        }
        sb.append("\n");

        // --- Program Tuples ---
        sb.append("\n// --- Code ---\n");
        for (Tuple tuple : programScope.getTuples()) {
            sb.append(tuple).append("\n");
        }
        sb.append("\n");

        // --- Functions ---
        for (FunctionInfo func : ir.getFunctionList().stream().skip(1).toList()) {
            sb.append("// --- Function: ").append(func.getName()).append(" ---\n");

            // Print out parameters and variables.
            // Do parameters first, then variables. Sort within each group by name

            func.getParameters().stream()
                    .sorted((v1, v2) -> v1.getParamIndex() - v2.getParamIndex())
                    .forEach(var -> sb.append("param ").append(var.getName()).append(" : ").append(var.getType())
                            .append("\n"));

            func.getLocalVariables().stream()
                    .sorted((v1, v2) -> v1.getName().compareTo(v2.getName()))
                    .forEach(var -> sb.append("var ").append(var.getName()).append(" : ").append(var.getType())
                            .append("\n"));

            // Print tuples
            for (Tuple tuple : func.getTuples()) {
                sb.append(tuple).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
