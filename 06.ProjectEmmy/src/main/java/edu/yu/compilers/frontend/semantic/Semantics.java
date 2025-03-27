/**
 * Semantic operations.
 * Perform type checking and create symbol tables.
 */

package edu.yu.compilers.frontend.semantic;

import java.util.Stack;

import antlr4.EmmyBaseVisitor;
import antlr4.EmmyParser;
import antlr4.EmmyParser.ArgumentListContext;
import antlr4.EmmyParser.FunctionDeclarationContext;
import antlr4.EmmyParser.NoargsContext;
import antlr4.EmmyParser.PrimaryContext;
import antlr4.EmmyParser.ProgramStartContext;
import edu.yu.compilers.frontend.semantic.SemanticErrorHandler.Code;
import edu.yu.compilers.intermediate.symbols.CrossReferencer;
import edu.yu.compilers.intermediate.symbols.Predefined;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.symbols.SymTableEntry.Kind;
import edu.yu.compilers.intermediate.symbols.SymTableStack;
import edu.yu.compilers.intermediate.types.Typespec;

public class Semantics extends EmmyBaseVisitor<Void> {
    private final SymTableStack symTableStack;
    private final SemanticErrorHandler error;

    // Track function scope with a stack to handle nested functions
    private final Stack<SymTableEntry> functionStack = new Stack<>();

    // Track whether we're in a loop for break/continue statements
    private boolean insideLoop = false;

    public Semantics() {
        this.symTableStack = new SymTableStack();
        Predefined.initialize(symTableStack);
        this.error = new SemanticErrorHandler();
    }


    public SymTableStack getSymTableStack() {
        return symTableStack;
    }

    public int getErrorCount() {
        return error.getCount();
    }

    public void printSymbolTableStack() {
        CrossReferencer crossReferencer = new CrossReferencer();
        crossReferencer.print(symTableStack);
    }

    // *************************
    // Visitor methods
    // *************************

    @Override
    public Void visitProgramStart(ProgramStartContext ctx) {
        SymTableEntry entry = symTableStack.enterLocal( "__program__", Kind.PROGRAM);
        entry.setRoutineSymTable(symTableStack.push());
        entry.appendLineNumber(ctx.start.getLine());
        symTableStack.setProgramId(entry);
        symTableStack.getLocalSymTable().setOwner(entry);

        // Visit all declarations
        for (EmmyParser.DeclarationContext decl : ctx.declaration()) {
            visit(decl);
        }

        // Exit the global scope
        symTableStack.pop();

        return null;
    }

    @Override
    public Void visitFunctionDeclaration(FunctionDeclarationContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitExpressionBody(EmmyParser.ExpressionBodyContext ctx) {
        visit(ctx.expression());
        Typespec exprType = ctx.expression().type;

        // Check if we're in a function
        // set the return type of the function we are in

        if (!functionStack.isEmpty()) {
            SymTableEntry functionId = functionStack.peek();
            functionId.setReturnType(exprType);
        }

        return null;
    }

    @Override
    public Void visitVariableDeclaration(EmmyParser.VariableDeclarationContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitBlockStatement(EmmyParser.BlockStatementContext ctx) {
        // Enter a new scope for the block
        symTableStack.push();

        // Visit all declarations in the block
        for (EmmyParser.DeclarationContext decl : ctx.declarations) {
            visit(decl);
        }

        // Exit the block's scope
        symTableStack.pop();

        return null;
    }

    @Override
    public Void visitIfStatement(EmmyParser.IfStatementContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitReturnStatement(EmmyParser.ReturnStatementContext ctx) {
        // TODO: Implementation needed
        // Sets the return value of the function, if not yet set
        return null;
    }

    @Override
    public Void visitWhileStatement(EmmyParser.WhileStatementContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitUntilStatement(EmmyParser.UntilStatementContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitRepeatStatement(EmmyParser.RepeatStatementContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    // Expression handling methods

    @Override
    public Void visitExpr(EmmyParser.ExprContext ctx) {
        visit(ctx.assignment());
        ctx.type = ctx.assignment().type;
        ctx.value = ctx.assignment().value;
        return null;
    }

    @Override
    public Void visitAssignmentExpr(EmmyParser.AssignmentExprContext ctx) {
        // TODO: Implementation needed
        return null;
    }

    @Override
    public Void visitAssignmentOr(EmmyParser.AssignmentOrContext ctx) {
        var exprCtx = ctx.logic_or();
        visit(exprCtx);
        ctx.type = exprCtx.type;
        ctx.value = exprCtx.value;
        return null;
    }

    @Override
    public Void visitLogicalOr(EmmyParser.LogicalOrContext ctx) {
        // TODO: Implementation of various type checking
        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            leftValue = null;
            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitLogicalAnd(EmmyParser.LogicalAndContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            leftValue = null;
            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitEqualityExpr(EmmyParser.EqualityExprContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            Boolean chainedEquality = Boolean.TRUE;

            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }

            leftType = Predefined.booleanType;
            leftValue = chainedEquality;
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitComparisonExpr(EmmyParser.ComparisonExprContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            Boolean chainedComparison = Boolean.TRUE;

            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }

            leftType = Predefined.booleanType;
            leftValue = chainedComparison;
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitTermExpr(EmmyParser.TermExprContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            leftValue = null;
            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitFactorExpr(EmmyParser.FactorExprContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.left);
        Typespec leftType = ctx.left.type;
        Object leftValue = ctx.left.value;

        if (!ctx.right.isEmpty()) {
            leftValue = null;
            for (int i = 0; i < ctx.right.size(); i++) {
                visit(ctx.right.get(i));
            }
        }

        ctx.type = leftType;
        ctx.value = leftValue;
        return null;
    }

    @Override
    public Void visitUnaryExpr(EmmyParser.UnaryExprContext ctx) {
        // TODO: Implementation of various type checking

        visit(ctx.unary());
        Typespec type = ctx.unary().type;
        Object value = ctx.unary().value;

        ctx.type = type;
        ctx.value = value;
        return null;

    }

    @Override
    public Void visitUnaryCall(EmmyParser.UnaryCallContext ctx) {
        visit(ctx.call());
        ctx.type = ctx.call().type;
        ctx.value = ctx.call().value;
        return null;
    }

    @Override
    public Void visitCallExpr(EmmyParser.CallExprContext ctx) {
        // TODO: Implementation of various type checking
    
        PrimaryContext primaryCtx = ctx.primary();

        visit(primaryCtx);
        ctx.type = primaryCtx.type;
        ctx.value = primaryCtx.value;
        ctx.entry = primaryCtx.entry;

        if (ctx.getChildCount() > 1) {
            // This is a function call

            if (primaryCtx.entry == null) {
                // The error will already have been reported
                return null;
            }

            for (int i = 0; i < ctx.args.size(); i++) {
                visit(ctx.args.get(i));
            }
        }

        return null;
    }

    private int getArgumentCount(EmmyParser.ArgumentsContext ctx) {
        if (ctx == null) {
            return 0;
        } else if (ctx instanceof NoargsContext) {
            return 0;
        } else  {
            ArgumentListContext listCtx = (ArgumentListContext) ctx;
            return listCtx.rest.size() + 1; // +1 for the first argument
        }
    }

    @Override
    public Void visitPrimaryTrue(EmmyParser.PrimaryTrueContext ctx) {
        ctx.type = Predefined.booleanType;
        ctx.value = Boolean.TRUE;
        return null;
    }

    @Override
    public Void visitPrimaryFalse(EmmyParser.PrimaryFalseContext ctx) {
        ctx.type = Predefined.booleanType;
        ctx.value = Boolean.FALSE;
        return null;
    }

    @Override
    public Void visitPrimaryNone(EmmyParser.PrimaryNoneContext ctx) {
        ctx.type = Predefined.noneType;
        ctx.value = null;
        return null;
    }

    @Override
    public Void visitPrimaryNumber(EmmyParser.PrimaryNumberContext ctx) {
        String numberText = ctx.num.getText();

        if (numberText.contains(".")) {
            ctx.type = Predefined.realType;
            ctx.value = Double.parseDouble(numberText);
        } else {
            ctx.type = Predefined.integerType;
            ctx.value = Integer.parseInt(numberText);
        }
        return null;
    }

    @Override
    public Void visitPrimaryString(EmmyParser.PrimaryStringContext ctx) {
        ctx.type = Predefined.stringType;
        ctx.value = ctx.str.getText();
        return null;
    }

    @Override
    public Void visitPrimaryIdentifier(EmmyParser.PrimaryIdentifierContext ctx) {
        String identifierName = ctx.id.getText();
        SymTableEntry identifierId = symTableStack.lookup(identifierName);

        if (identifierId == null) {
            error.flag(Code.UNDECLARED_IDENTIFIER, ctx, "Identifier not found: " + identifierName);
            ctx.type = Predefined.noneType;
            ctx.value = null;
        } else {
            ctx.type = identifierId.getType();
            ctx.value = null;
            ctx.entry = identifierId;
        }
        return null;
    }

    @Override
    public Void visitPrimaryParenthesis(EmmyParser.PrimaryParenthesisContext ctx) {
        visit(ctx.inner);
        ctx.type = ctx.inner.type;
        ctx.value = ctx.inner.value;
        return null;
    }
}