package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;
import edu.yu.compilers.intermediate.ir.Operand.Label;

import java.util.List;
import java.util.Map;

public class Compiler {

    private final CodeGenerator codeGenerator;

    public Compiler(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    /**
     * Compiles the given IR using the associated code generator.
     */
    public String compile(TupleIR ir) {

        // Generate code for each function in the IR

        for (FunctionInfo functionInfo : ir.getFunctionList()) {
            String functionName = functionInfo.getName();
            List<Tuple> tuples = functionInfo.getTuples();
        }

        return "Compilation completed successfully.";

    }
}
