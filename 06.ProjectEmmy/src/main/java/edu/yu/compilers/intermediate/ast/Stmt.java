package edu.yu.compilers.intermediate.ast;

import java.util.List;

import edu.yu.compilers.intermediate.symtable.SymTableEntry;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitFunctionStmt(Function stmt);

        R visitIfStmt(If stmt);

        R visitLoopStmt(Loop stmt);

        R visitLoopBreakTestStmt(Loop.BreakTest stmt);

        R visitPrintStmt(Print stmt);

        R visitReturnStmt(Return stmt);

        R visitVarStmt(Var stmt);
    }

    /**
     * Implementations of Stmt below
     **/

    
    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Function extends Stmt {
        final SymTableEntry entry;
        final List<SymTableEntry> params;
        final List<Stmt> body;

        Function(SymTableEntry entry, List<Stmt> body) {
            this.entry = entry;
            this.params = entry.getRoutineParameters();
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class Loop extends Stmt {
        final Stmt initializer;
        final List<Stmt> body;

        Loop(Stmt initializer, List<Stmt> body) {
            this.initializer = initializer;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopStmt(this);
        }

        static class BreakTest extends Stmt {
            final Expr condition;

            BreakTest(Expr condition) {
                this.condition = condition;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitLoopBreakTestStmt(this);
            }
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Return extends Stmt {
        final Expr value;

        Return(Expr value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    static class Var extends Stmt {
        final SymTableEntry entry;
        final Expr initializer;

        Var(SymTableEntry entry, Expr initializer) {
            this.entry = entry;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
// < Appendix II stmt