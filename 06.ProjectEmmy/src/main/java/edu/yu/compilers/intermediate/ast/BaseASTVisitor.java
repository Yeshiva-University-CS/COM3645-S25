package edu.yu.compilers.intermediate.ast;

import edu.yu.compilers.intermediate.ast.Expr.Assign;
import edu.yu.compilers.intermediate.ast.Expr.Binary;
import edu.yu.compilers.intermediate.ast.Expr.Call;
import edu.yu.compilers.intermediate.ast.Expr.FuncId;
import edu.yu.compilers.intermediate.ast.Expr.Literal;
import edu.yu.compilers.intermediate.ast.Expr.Logical;
import edu.yu.compilers.intermediate.ast.Expr.Unary;
import edu.yu.compilers.intermediate.ast.Expr.VarId;
import edu.yu.compilers.intermediate.ast.Stmt.Block;
import edu.yu.compilers.intermediate.ast.Stmt.Expression;
import edu.yu.compilers.intermediate.ast.Stmt.If;
import edu.yu.compilers.intermediate.ast.Stmt.Loop;
import edu.yu.compilers.intermediate.ast.Stmt.Loop.BreakTest;
import edu.yu.compilers.intermediate.ast.Stmt.Print;
import edu.yu.compilers.intermediate.ast.Stmt.Return;
import edu.yu.compilers.intermediate.ast.Stmt.Var;

/**
 * Base AST visitor implementation that by default visits all children of a
 * node.
 * Subclasses can override methods to implement specific behavior.
 */
public class BaseASTVisitor<T> implements Expr.Visitor<T>, Stmt.Visitor<T> {

    /**
     * Visit a Program node and its children.
     */
    public T visitProgram(Program program) {
        // Visit all statements in the program
        for (Stmt s : program.getStatements()) {
            s.accept(this);
        }
        return null;
    }

    @Override
    public T visitBlockStmt(Block stmt) {
        // Visit all statements in the block
        for (Stmt s : stmt.getStatements()) {
            s.accept(this);
        }
        return null;
    }

    @Override
    public T visitExpressionStmt(Expression stmt) {
        // Visit the expression
        stmt.getExpression().accept(this);
        return null;
    }

    @Override
    public T visitIfStmt(If stmt) {
        // Visit condition
        stmt.getCondition().accept(this);

        // Visit then branch
        stmt.getThenBranch().accept(this);

        // Visit else branch if it exists
        if (stmt.getElseBranch() != null) {
            stmt.getElseBranch().accept(this);
        }

        return null;
    }

    @Override
    public T visitLoopStmt(Loop stmt) {
        // Visit initializer if it exists
        if (stmt.getInitializer() != null) {
            stmt.getInitializer().accept(this);
        }

        // Visit all statements in the loop body
        for (Stmt s : stmt.getBody()) {
            s.accept(this);
        }

        return null;
    }

    @Override
    public T visitLoopBreakTestStmt(BreakTest stmt) {
        // Visit the condition
        stmt.getCondition().accept(this);
        return null;
    }

    @Override
    public T visitPrintStmt(Print stmt) {
        // Visit the expression
        stmt.getExpression().accept(this);
        return null;
    }

    @Override
    public T visitReturnStmt(Return stmt) {
        // Visit the value if it exists
        if (stmt.getValue() != null) {
            stmt.getValue().accept(this);
        }
        return null;
    }

    @Override
    public T visitVarStmt(Var stmt) {
        // Visit the initializer if it exists
        if (stmt.getInitializer() != null) {
            stmt.getInitializer().accept(this);
        }
        return null;
    }

    @Override
    public T visitAssignExpr(Assign expr) {
        // Visit the value
        expr.getValue().accept(this);
        return null;
    }

    @Override
    public T visitBinaryExpr(Binary expr) {
        // Visit left and right operands
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return null;
    }

    @Override
    public T visitCallExpr(Call expr) {
        // Visit callee
        expr.getCallee().accept(this);

        // Visit all arguments
        for (Expr arg : expr.getArguments()) {
            arg.accept(this);
        }

        expr.getCodeBlock().accept(this);
        return null;
    }

    @Override
    public T visitFuncIdExpr(FuncId expr) {
        // Leaf node, no children to visit
        return null;
    }

    @Override
    public T visitLiteralExpr(Literal expr) {
        // Leaf node, no children to visit
        return null;
    }

    @Override
    public T visitLogicalExpr(Logical expr) {
        // Visit left and right operands
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return null;
    }

    @Override
    public T visitUnaryExpr(Unary expr) {
        // Visit the operand
        expr.getOperand().accept(this);
        return null;
    }

    @Override
    public T visitVarIdExpr(VarId expr) {
        // Leaf node, no children to visit
        return null;
    }
}
