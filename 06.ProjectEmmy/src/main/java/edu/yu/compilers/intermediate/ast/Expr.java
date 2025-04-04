package edu.yu.compilers.intermediate.ast;

import java.util.Collections;
import java.util.List;

import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.types.Typespec;

public abstract class Expr {

    interface Visitor<R> {
        R visitAssignExpr(Assign expr);

        R visitBinaryExpr(Binary expr);

        R visitCallExpr(Call expr);

        R visitFuncIdExpr(FuncId expr);

        R visitLiteralExpr(Literal expr);

        R visitLogicalExpr(Logical expr);

        R visitUnaryExpr(Unary expr);

        R visitVarIdExpr(VarId expr);

        default R visit(Expr expr) {
            return expr.accept(this);
        }
    }

    private Typespec type;

    public void setType(Typespec type) {
        this.type = type;
    }

    public Typespec getType() {
        return type;
    }

    abstract <R> R accept(Visitor<R> visitor);

    /**
     * Implementations of Expr below
     **/

    public static class Assign extends Expr {
        private final SymTableEntry entry;
        private final Expr value;

        public Assign(SymTableEntry entry, Expr value) {
            this.entry = entry;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        public SymTableEntry getEntry() {
            return entry;
        }

        public Expr getValue() {
            return value;
        }
    }

    public static class Binary extends Expr {
        private final Expr left;
        private final Oper operator;
        private final Expr right;

        public Binary(Expr left, Oper operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        public Expr getLeft() {
            return left;
        }

        public Oper getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }
    }

    public static class Call extends Expr {
        private final Expr.FuncId callee;
        private final List<Expr> arguments;
        private final Stmt.Block codeBlock;

        public Call(Expr.FuncId callee, List<Expr> arguments, Stmt.Block codeBlock) {
            this.callee = callee;
            this.arguments = arguments;
            this.codeBlock = codeBlock;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        public Expr.FuncId getCallee() {
            return callee;
        }

        public List<Expr> getArguments() {
            return Collections.unmodifiableList(arguments);
        }

        public Stmt.Block getCodeBlock() {
            return codeBlock;
        }
    }

    public static class Literal extends Expr {
        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        public Object getValue() {
            return value;
        }
    }

    public static class Logical extends Expr {
        private final Expr left;
        private final Oper operator;
        private final Expr right;

        public Logical(Expr left, Oper operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        public Expr getLeft() {
            return left;
        }

        public Oper getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }
    }

    public static class Unary extends Expr {
        private final Expr operand;
        private final Oper operator;

        public Unary(Oper operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        public Expr getOperand() {
            return operand;
        }

        public Oper getOperator() {
            return operator;
        }
    }

    public static class FuncId extends Expr {
        private final SymTableEntry entry;

        public FuncId(SymTableEntry entry) {
            if (entry == null) {
                throw new IllegalArgumentException("entry cannot be null");
            }
            if (!entry.isFunction()) {
                throw new IllegalArgumentException("entry must be a variable");
            }            
            this.entry = entry;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFuncIdExpr(this);
        }

        public SymTableEntry getEntry() {
            return entry;
        }
    }

    public static class VarId extends Expr {
        private final SymTableEntry entry;

        public VarId(SymTableEntry entry) {
            if (entry == null) {
                throw new IllegalArgumentException("entry cannot be null");
            }
            if (!(entry.isVariable() || entry.isValueParameter())) {
                throw new IllegalArgumentException("entry must be a variable");
            }
            this.entry = entry;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarIdExpr(this);
        }

        public SymTableEntry getEntry() {
            return entry;
        }
    }

}
