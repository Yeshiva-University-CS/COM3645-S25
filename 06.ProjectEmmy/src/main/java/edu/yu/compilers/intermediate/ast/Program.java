package edu.yu.compilers.intermediate.ast;

import java.util.Collections;
import java.util.List;

public class Program {
    private final List<Stmt> statements;

    public Program(List<Stmt> statements) {
        this.statements = statements;
    }

    public List<Stmt> getStatements() {
        return Collections.unmodifiableList(statements);
    }
}
