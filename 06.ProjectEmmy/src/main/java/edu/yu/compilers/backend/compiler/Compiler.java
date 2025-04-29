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

        // Process all function scopes after the program end
        List<FunctionInfo> functionList = ir.getFunctionList();
        for (FunctionInfo functionInfo : functionList) {
            List<Tuple> functionTuples = functionInfo.getTuples();

            // Emit function start
            codeGenerator.emitFunctionStart(functionTuples.get(0), functionInfo);

            // Process function body tuples
            for (Tuple tuple : functionTuples) {
                codeGenerator.emitTuple(tuple);
            }
        }

        // Emit program end
        if (! programScope.getTuples().isEmpty()) {
            codeGenerator.emitProgramEnd();
        }

        // Return the generated code
        return codeGenerator.getOutput();
    }
}
