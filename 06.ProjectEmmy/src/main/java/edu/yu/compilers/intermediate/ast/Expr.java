package edu.yu.compilers.intermediate.ast;

import java.util.List;

import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.type.Typespec;

abstract class Expr {

    interface Visitor<R> {
        R visitAssignExpr(Assign expr);

        R visitBinaryExpr(Binary expr);

        R visitCallExpr(Call expr);

        R visitLiteralExpr(Literal expr);

        R visitLogicalExpr(Logical expr);

        R visitUnaryExpr(Unary expr);

        R visitVariableExpr(Variable expr);
    }

    private Typespec type;

    protected void setType(Typespec type) {
        this.type = type;
    }

    public Typespec getType() {
        return type;
    }

    abstract <R> R accept(Visitor<R> visitor);

    /**
     * Implementations of Expr below
     **/

    static class Assign extends Expr {
        final SymTableEntry entry;
        final Expr value;

        Assign(SymTableEntry entry, Expr value) {
            this.entry = entry;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Binary extends Expr {
        final Expr left;
        final OpType operator;
        final Expr right;

        Binary(Expr left, OpType operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Call extends Expr {
        final Expr callee;
        final List<Expr> arguments;

        Call(Expr callee, List<Expr> arguments) {
            this.callee = callee;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final OpType operator;
        final Expr right;

        Logical(Expr left, OpType operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Unary extends Expr {
        public final Expr operand;
        public final OpType operator;

        public Unary(OpType operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Variable extends Expr {
        final SymTableEntry entry;

        Variable(SymTableEntry entry) {
            this.entry = entry;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

}
