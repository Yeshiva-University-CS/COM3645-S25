package edu.yu.compilers.backend.interpreter;

import java.util.Scanner;

import antlr4.EmmyBaseVisitor;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;

/**
 * Execute Emmy programs.
 */
public class Executor extends EmmyBaseVisitor<Object>
{
    private int executionCount = 0;     // count of executed statements
    private long elapsedTime = 0L;          // elapsed time in ms
    private final SymTableEntry programId;      // program identifier's symbol table entry
    private final RuntimeStack runtimeStack;  // runtime stack
    private final Scanner scanner;              // runtime input
    private final RuntimeErrorHandler error;  // runtime error handler
    
    public Executor(SymTableEntry programId)
    {
        this.programId = programId;
        runtimeStack = new RuntimeStack();
        scanner = new Scanner(System.in);
        error = new RuntimeErrorHandler();
    }
    
    public int getExecutionCount() {
        return executionCount;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
