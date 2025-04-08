package edu.yu.compilers.intermediate.ast;

import java.util.Collections;
import java.util.List;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;

public class Program {
    private final SymTableEntry entry;
    private final List<Stmt> statements;

    public Program(SymTableEntry entry, List<Stmt> statements) {
        this.entry = entry;
        this.statements = statements;
    }

    public SymTableEntry getEntry() {
        return entry;
    }

    public List<Stmt> getStatements() {
        return Collections.unmodifiableList(statements);
    }
}
