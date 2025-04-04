package edu.yu.compilers.frontend.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTree;

import antlr4.EmmyBaseVisitor;
import antlr4.EmmyParser.*;
import edu.yu.compilers.intermediate.ast.Expr;
import edu.yu.compilers.intermediate.ast.Oper;
import edu.yu.compilers.intermediate.ast.Program;
import edu.yu.compilers.intermediate.ast.Stmt;
import edu.yu.compilers.intermediate.symbols.Predefined;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.types.TypeChecker;

/**
 * ASTBuilder class that visits the ANTLR4 parse tree and builds an AST.
 * It transforms the concrete syntax tree into an abstract syntax tree
 * that is used for further processing. This class assumes that semantic
 * analysis has already been performed.
 */
public class ASTBuilder extends EmmyBaseVisitor<Object> {

    public static Program build(ParseTree tree) {
        ASTBuilder builder = new ASTBuilder();
        builder.visit(tree);
        return builder.getProgam();
    }

    // A program is a just a list of statements
    private final List<Stmt> programStatements = new ArrayList<>();

    // A map from a function to the AST representing its body
    // This is used to store the body of a function while visiting its declaration
    private final HashMap<SymTableEntry, Stmt.Block> funcBodyMap = new HashMap<>();

    private ASTBuilder() {

    }

    private Program getProgam() {
        return ASTFactory.createProgram(programStatements);
    }

    // =============================
    // Program and declarations
    // =============================

    @Override
    public Void visitProgramStart(ProgramStartContext ctx) {
        for (DeclarationContext declCtx : ctx.declaration()) {
            Object result = visit(declCtx);
            if (result != null && result instanceof Stmt) {
                programStatements.add((Stmt) result);
            }
        }
        return null;
    }

    @Override
    public Void visitFunctionDeclaration(FunctionDeclarationContext ctx) {
        Stmt body = (Stmt) visit(ctx.functionBody());

        if (!(body instanceof Stmt.Block)) {
            // If the body is not a block, then it's a single statement
            // Wrap it in a block
            body = ASTFactory.createBlockStmt(List.of(body));
        }

        funcBodyMap.put(ctx.entry, (Stmt.Block) body);

        return null;
    }

    @Override
    public Stmt.Expression visitExpressionBody(ExpressionBodyContext ctx) {
        var expr = (Expr) visit(ctx.expression());
        return ASTFactory.createExpressionStmt(expr);
    }

    @Override
    public Stmt.Block visitBlockBody(BlockBodyContext ctx) {
        return (Stmt.Block) visit(ctx.block());
    }

    @Override
    public Stmt visitVariableDeclaration(VariableDeclarationContext ctx) {
        // If there is an initializer, create an assignment statement
        if (ctx.init != null) {
            Expr initializer = (Expr) visit(ctx.init);
            var assignExpr = ASTFactory.createAssign(ctx.entry, initializer);
            assignExpr.setType(initializer.getType());
            return ASTFactory.createExpressionStmt(assignExpr);
        }
        return null;
    }

    // =============================
    // Statements
    // =============================

    @Override
    public Stmt.Expression visitExpressionStatement(ExpressionStatementContext ctx) {
        Expr expr = (Expr) visit(ctx.expression());
        return ASTFactory.createExpressionStmt(expr);
    }

    @Override
    public Stmt.Print visitPrintStatement(PrintStatementContext ctx) {
        Expr value = (Expr) visit(ctx.value);
        return ASTFactory.createPrintStmt(value);
    }

    @Override
    public Stmt.If visitIfStatement(IfStatementContext ctx) {
        Expr condition = (Expr) visit(ctx.condition);
        Stmt thenBranch = (Stmt) visit(ctx.thenBranch);
        Stmt elseBranch = null;

        if (ctx.elseBranch != null) {
            elseBranch = (Stmt) visit(ctx.elseBranch);
        }

        return ASTFactory.createIfStmt(condition, thenBranch, elseBranch);
    }

    @Override
    public Stmt.Return visitReturnStatement(ReturnStatementContext ctx) {
        Expr value = null;
        if (ctx.value != null) {
            value = (Expr) visit(ctx.value);
        }

        return ASTFactory.createReturnStmt(value);
    }

    @Override
    public Stmt.Loop visitWhileStatement(WhileStatementContext ctx) {
        Expr condition = (Expr) visit(ctx.condition);
        Stmt body = (Stmt) visit(ctx.body);

        return ASTFactory.createWhileStmt(condition, body);
    }

    @Override
    public Stmt.Loop visitUntilStatement(UntilStatementContext ctx) {
        Expr condition = (Expr) visit(ctx.condition);
        Stmt body = (Stmt) visit(ctx.body);

        return ASTFactory.createUntilStmt(body, condition);
    }

    @Override
    public Stmt.Loop visitRepeatStatement(RepeatStatementContext ctx) {
        Expr count = (Expr) visit(ctx.count);
        Stmt body = (Stmt) visit(ctx.body);

        return ASTFactory.createRepeatStmt(count, body);
    }

    @Override
    public Stmt.Block visitBlockStatement(BlockStatementContext ctx) {
        List<Stmt> blokStmts = new ArrayList<>();

        // Visit all declarations in the block
        for (DeclarationContext declCtx : ctx.declaration()) {
            Object result = visit(declCtx);
            if (result instanceof Stmt) {
                blokStmts.add((Stmt) result);
            }
        }

        return ASTFactory.createBlockStmt(blokStmts);
    }

    // =============================
    // Expressions
    // =============================

    @Override
    public Expr visitAssignmentExpr(AssignmentExprContext ctx) {
        SymTableEntry entry = ctx.entry;
        Expr rhs = (Expr) visit(ctx.rhs);
        var expr = ASTFactory.createAssign(entry, rhs);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr visitLogicalOr(LogicalOrContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle chain of OR expressions
        for (int i = 0; i < ctx.right.size(); i++) {
            Expr right = (Expr) visit(ctx.right.get(i));
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            left = ASTFactory.createLogical(left, operator, right);
            left.setType(Predefined.booleanType);
        }

        return left;
    }

    @Override
    public Expr visitLogicalAnd(LogicalAndContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle chain of AND expressions
        for (int i = 0; i < ctx.right.size(); i++) {
            Expr right = (Expr) visit(ctx.right.get(i));
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            left = ASTFactory.createLogical(left, operator, right);
            left.setType(Predefined.booleanType);
        }

        return left;
    }

    @Override
    public Expr visitEqualityExpr(EqualityExprContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle the case where there are no operators (single term)
        if (ctx.right.isEmpty()) {
            return left;
        }

        // For chained equality like "a == b == c", we need to check (a == b) && (b ==
        // c)
        List<Expr> operands = new ArrayList<>();
        operands.add(left);

        // Add all the right operands
        for (ComparisonContext rightCtx : ctx.right) {
            operands.add((Expr) visit(rightCtx));
        }

        // Create a logical AND of all adjacent equality checks
        List<Expr> equalityChecks = new ArrayList<>();
        for (int i = 0; i < ctx.op.size(); i++) {
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            Expr leftOperand = operands.get(i);
            Expr rightOperand = operands.get(i + 1);
            var expr = ASTFactory.createBinary(leftOperand, operator, rightOperand);
            expr.setType(Predefined.booleanType);
            equalityChecks.add(expr);
        }

        // Start with the first equality check
        Expr result = equalityChecks.get(0);

        // Combine all remaining equality checks with logical AND
        for (int i = 1; i < equalityChecks.size(); i++) {
            result = ASTFactory.createLogical(result, Oper.AND, equalityChecks.get(i));
            result.setType(Predefined.booleanType);
        }

        return result;
    }

    @Override
    public Expr visitComparisonExpr(ComparisonExprContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle the case where there are no operators (single term)
        if (ctx.right.isEmpty()) {
            return left;
        }

        // For chained comparison like "a < b < c", we need to check (a < b) && (b < c)
        List<Expr> operands = new ArrayList<>();
        operands.add(left);

        // Add all the right operands
        for (TermContext rightCtx : ctx.right) {
            operands.add((Expr) visit(rightCtx));
        }

        // Create a logical AND of all adjacent comparison checks
        List<Expr> comparisonChecks = new ArrayList<>();
        for (int i = 0; i < ctx.op.size(); i++) {
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            Expr leftOperand = operands.get(i);
            Expr rightOperand = operands.get(i + 1);
            var expr = ASTFactory.createBinary(leftOperand, operator, rightOperand);
            expr.setType(Predefined.booleanType);
            comparisonChecks.add(expr);
        }

        // Start with the first comparison check
        Expr result = comparisonChecks.get(0);

        // Combine all remaining comparison checks with logical AND
        for (int i = 1; i < comparisonChecks.size(); i++) {
            result = ASTFactory.createLogical(result, Oper.AND, comparisonChecks.get(i));
            result.setType(Predefined.booleanType);
        }

        return result;
    }

    @Override
    public Expr visitTermExpr(TermExprContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle chain of term expressions
        for (int i = 0; i < ctx.right.size(); i++) {
            Expr right = (Expr) visit(ctx.right.get(i));
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            left = ASTFactory.createBinary(left, operator, right);
            if (TypeChecker.atLeastOneIsReal(ctx.left.type, ctx.right.get(i).type)) {
                left.setType(Predefined.realType);
            } else {
                left.setType(ctx.type);
            }
        }

        return left;
    }

    @Override
    public Expr visitFactorExpr(FactorExprContext ctx) {
        Expr left = (Expr) visit(ctx.left);

        // Handle chain of factor expressions
        for (int i = 0; i < ctx.right.size(); i++) {
            Expr right = (Expr) visit(ctx.right.get(i));
            Oper operator = ASTFactory.tokenToOpType(ctx.op.get(i).getText());
            left = ASTFactory.createBinary(left, operator, right);
            if (TypeChecker.atLeastOneIsReal(ctx.left.type, ctx.right.get(i).type)) {
                left.setType(Predefined.realType);
            } else {
                left.setType(ctx.type);
            }
        }

        return left;
    }

    @Override
    public Expr visitUnaryExpr(UnaryExprContext ctx) {
        Expr right = (Expr) visit(ctx.right);
        Oper operator = ASTFactory.tokenToOpType(ctx.op.getText());
        var expr = ASTFactory.createUnary(operator, right);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr visitCallExpr(CallExprContext ctx) {
        Expr primary = (Expr) visit(ctx.primary());

        // If it's not a function call, return it as is
        if (!TypeChecker.isFunction(ctx.primary().type)) {
            return primary;
        }

        // Handle function arguments
        // If there are no arguments, then it's not a function call
        // but rather returning the function itself

        // If arg size > 1 then it means that it's a chain of function calls
        // e.g. foo()()()()
        // In this case, we need to chain the function calls

        for (int i = 0; i < ctx.args.size(); i++) {
            Expr.FuncId funcIdExpr = (Expr.FuncId) primary;
            List<Expr> arguments = getArguments(ctx.args.get(i));
            Stmt.Block body = funcBodyMap.get(funcIdExpr.getEntry());
            primary = ASTFactory.createCall(funcIdExpr, arguments, body);
            primary.setType(funcIdExpr.getEntry().getReturnType());
        }

        return primary;
    }

    private List<Expr> getArguments(ArgumentsContext ctx) {
        List<Expr> arguments = new ArrayList<>();

        if (ctx instanceof ArgumentListContext arglistCtx) {
            arguments.add((Expr) visit(arglistCtx.first));

            for (ExpressionContext exprCtx : arglistCtx.rest) {
                arguments.add((Expr) visit(exprCtx));
            }
        }

        return arguments;
    }

    @Override
    public Expr.Literal visitPrimaryTrue(PrimaryTrueContext ctx) {
        var expr = ASTFactory.createLiteral(Boolean.TRUE);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr.Literal visitPrimaryFalse(PrimaryFalseContext ctx) {
        var expr = ASTFactory.createLiteral(Boolean.FALSE);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr.Literal visitPrimaryNone(PrimaryNoneContext ctx) {
        var expr = ASTFactory.createLiteral(null);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr.Literal visitPrimaryNumber(PrimaryNumberContext ctx) {
        String numberText = ctx.num.getText();
        Object value;

        if (numberText.contains(".")) {
            value = Double.parseDouble(numberText);
        } else {
            value = Integer.parseInt(numberText);
        }

        var expr = ASTFactory.createLiteral(value);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr.Literal visitPrimaryString(PrimaryStringContext ctx) {
        // Remove the quotes from the string literal
        String text = ctx.str.getText();
        String value = text.substring(1, text.length() - 1);
        var expr = ASTFactory.createLiteral(value);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr visitPrimaryIdentifier(PrimaryIdentifierContext ctx) {
        var expr = (ctx.entry.isVariable() || ctx.entry.isValueParameter()) ? ASTFactory.createVarId(ctx.entry)
                : ASTFactory.createFuncId(ctx.entry);
        expr.setType(ctx.type);
        return expr;
    }

    @Override
    public Expr visitPrimaryParenthesis(PrimaryParenthesisContext ctx) {
        return (Expr) visit(ctx.inner);
    }

}
