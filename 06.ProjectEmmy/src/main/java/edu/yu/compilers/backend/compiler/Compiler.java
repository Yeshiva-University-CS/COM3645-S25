package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;

import java.util.List;

public class Compiler {

    private final CodeGenerator codeGenerator;

    public Compiler(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    /**
     * Compiles the given IR using the associated code generator.
     *
     * @param ir the intermediate representation to compile
     * @return the generated code as a string
     */
    public String compile(TupleIR ir) {
        // Process the global program scope first
        FunctionInfo programScope = ir.globalFunctionScope();

        // Emit program start
        if (!programScope.getTuples().isEmpty()) {
            codeGenerator.emitProgramStart();
        }

        // Process program body tuples
        for (Tuple tuple : programScope.getTuples()) {
            codeGenerator.emitTuple(tuple);
        }

        // Process all function scopes after the program end
        List<FunctionInfo> functionList = ir.getFunctionList();
        for (FunctionInfo functionInfo : functionList) {
            // Skip the global scope as we've already processed it
            if (functionInfo == programScope) {
                continue;
            }

            List<Tuple> functionTuples = functionInfo.getTuples();
            if (functionTuples.isEmpty()) {
                continue;
            }

            // Emit function start
            codeGenerator.emitFunctionStart(functionTuples.get(0), functionInfo);

            // Process function body tuples (skip first and last tuples which are FUNCTION and END_FUNCTION)
            for (int i = 1; i < functionTuples.size() - 1; i++) {
                Tuple tuple = functionTuples.get(i);
                codeGenerator.emitTuple(tuple);
            }

            // Emit function end
            codeGenerator.emitFunctionEnd(functionTuples.get(functionTuples.size() - 1), functionInfo);
        }

        // Emit program end
        if (! programScope.getTuples().isEmpty()) {
            codeGenerator.emitProgramEnd();
        }


        // Return the generated code
        return codeGenerator.getOutput().toString();
    }
}
