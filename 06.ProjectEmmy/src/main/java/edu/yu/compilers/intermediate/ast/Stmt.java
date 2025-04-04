package edu.yu.compilers.intermediate.ast;

import java.util.Collections;
import java.util.List;

import edu.yu.compilers.intermediate.symbols.SymTableEntry;

public abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitIfStmt(If stmt);

        R visitLoopStmt(Loop stmt);

        R visitLoopBreakTestStmt(Loop.BreakTest stmt);

        R visitPrintStmt(Print stmt);

        R visitReturnStmt(Return stmt);

        R visitVarStmt(Var stmt);
        
        default R visit(Stmt stmt) {
            return stmt.accept(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
    
    /**
     * Implementations of Stmt below
     **/

    public static class Block extends Stmt {
        private final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public List<Stmt> getStatements() {
            return Collections.unmodifiableList(statements);
        }
    }

    public static class Expression extends Stmt {
        private final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        public Expr getExpression() {
            return expression;
        }
    }

    public static class If extends Stmt {
        private final Expr condition;
        private final Stmt thenBranch;
        private final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getThenBranch() {
            return thenBranch;
        }

        public Stmt getElseBranch() {
            return elseBranch;
        }
    }

    public static class Loop extends Stmt {
        private final Stmt initializer;
        private final List<Stmt> body;

        public Loop(Stmt initializer, List<Stmt> body) {
            this.initializer = initializer;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopStmt(this);
        }

        public static class BreakTest extends Stmt {
            private final Expr condition;

            public BreakTest(Expr condition) {
                this.condition = condition;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitLoopBreakTestStmt(this);
            }

            public Expr getCondition() {
                return condition;
            }
        }

        public Stmt getInitializer() {
            return initializer;
        }

        public List<Stmt> getBody() {
            return Collections.unmodifiableList(body);
        }
    }

    public static class Print extends Stmt {
        private final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        public Expr getExpression() {
            return expression;
        }
    }

    public static class Return extends Stmt {
        private final Expr value;

        public Return(Expr value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        public Expr getValue() {
            return value;
        }
    }

    public static class Var extends Stmt {
        private final SymTableEntry entry;
        private final Expr initializer;

        public Var(SymTableEntry entry, Expr initializer) {
            this.entry = entry;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        public SymTableEntry getEntry() {
            return entry;
        }

        public Expr getInitializer() {
            return initializer;
        }
    }
}
// < Appendix II stmt