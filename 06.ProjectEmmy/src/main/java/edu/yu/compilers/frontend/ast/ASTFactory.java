package edu.yu.compilers.frontend.ast;

import java.util.ArrayList;
import java.util.List;
import edu.yu.compilers.intermediate.ast.Expr;
import edu.yu.compilers.intermediate.ast.Oper;
import edu.yu.compilers.intermediate.ast.Program;
import edu.yu.compilers.intermediate.ast.Stmt;
import edu.yu.compilers.intermediate.symbols.Predefined;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.symbols.SymTableEntry.Kind;
import edu.yu.compilers.intermediate.symbols.SymTableStack;

/**
 * Factory class for constructing Abstract Syntax Tree (AST) nodes.
 * This class simplifies the creation of expression and statement nodes
 * for the Emmy language compiler.
 */
public class ASTFactory {

    // Symbol table stack for creating temporary variables
    private static SymTableStack symTableStack;

    /**
     * Set the symbol table stack for the factory to use.
     * 
     * @param stack the symbol table stack
     */
    public static void setSymbolTableStack(SymTableStack stack) {
        symTableStack = stack;
    }

    /**
     * Create a temporary variable for internal use in AST transformations.
     * 
     * @param name base name for the variable
     * @return symbol table entry for the temporary variable
     */
    private static SymTableEntry createTempVariable(String name) {
        if (symTableStack == null) {
            throw new IllegalStateException("Symbol table stack not initialized");
        }

        // Create a unique name for the temporary variable
        String tempName = "_temp_" + name + "_" + System.nanoTime();

        // Create a symbol table entry for the temporary variable
        SymTableEntry entry = symTableStack.enterLocal(tempName, Kind.VARIABLE);

        // Set its type to undefined - will be determined during execution
        entry.setType(Predefined.noneType);

        return entry;
    }

    /**
     * Create a block statement.
     * 
     * @param entry      symbol table entry for the program
     * @param statements the list of statements in the program
     * @return a Program node
     */
    public static Program createProgram(SymTableEntry entry, List<Stmt> statements) {
        return new Program(entry, statements);
    }

    /**
     * Create a variable identifier expression.
     * 
     * @param entry symbol table entry for the variable identifier
     * @return an variable identifier expression node
     */
    public static Expr.VarId createVarId(SymTableEntry entry) {
        return new Expr.VarId(entry);
    }

    /**
     * Create a function identifier expression.
     * 
     * @param entry     symbol table entry for the function identifier
     * @param codeBlock the function's code block
     * @return an function identifier expression node
     */
    public static Expr.FuncId createFuncId(SymTableEntry entry, Stmt.Block codeBlock) {
        return new Expr.FuncId(entry, codeBlock);
    }

    /**
     * Create a literal expression.
     * 
     * @param value the literal value
     * @return a literal expression node
     */
    public static Expr.Literal createLiteral(Object value) {
        return new Expr.Literal(value);
    }

    /**
     * Create a unary expression.
     * 
     * @param operator the unary operator
     * @param operand  the operand expression
     * @return a unary expression node
     */
    public static Expr.Unary createUnary(Oper operator, Expr operand) {
        return new Expr.Unary(operator, operand);
    }

    /**
     * Create a binary expression.
     * 
     * @param left     the left operand expression
     * @param operator the binary operator
     * @param right    the right operand expression
     * @return a binary expression node
     */
    public static Expr.Binary createBinary(Expr left, Oper operator, Expr right) {
        return new Expr.Binary(left, operator, right);
    }

    /**
     * Create a logical expression.
     * 
     * @param left     the left operand expression
     * @param operator the logical operator
     * @param right    the right operand expression
     * @return a logical expression node
     */
    public static Expr.Logical createLogical(Expr left, Oper operator, Expr right) {
        return new Expr.Logical(left, operator, right);
    }

    /**
     * Create an assignment expression.
     * 
     * @param entry symbol table entry for the variable
     * @param value the expression to assign
     * @return an assignment expression node
     */
    public static Expr.Assign createAssign(SymTableEntry entry, Expr value) {
        return new Expr.Assign(entry, value);
    }

    /**
     * Create a function call expression.
     * 
     * @param Expr      of the function to call
     * @param arguments the argument expressions
     * @return a call expression node
     */
    public static Expr.Call createCall(Expr.FuncId callee, List<Expr> arguments) {
        return new Expr.Call(callee, arguments);
    }

    /**
     * Create a function call expression with no arguments.
     * 
     * @param Expr of the function to call
     * @return a call expression node
     */
    public static Expr.Call createCall(Expr.FuncId callee) {
        return new Expr.Call(callee, new ArrayList<>());
    }

    /**
     * Create an expression statement.
     * 
     * @param expression the expression
     * @return an expression statement node
     */
    public static Stmt.Expression createExpressionStmt(Expr expression) {
        return new Stmt.Expression(expression);
    }

    /**
     * Create a print statement.
     * 
     * @param expression the expression to print
     * @return a print statement node
     */
    public static Stmt.Print createPrintStmt(Expr expression) {
        return new Stmt.Print(expression);
    }

    /**
     * Create a block statement.
     * 
     * @param statements the list of statements in the block
     * @return a block statement node
     */
    public static Stmt.Block createBlockStmt(List<Stmt> statements) {
        return new Stmt.Block(statements);
    }

    /**
     * Create an empty statement.
     * 
     * @return an empty statement node
     */
    public static Stmt.Empty createEmptyStmt() {
        return new Stmt.Empty();
    }

    /**
     * Create an if statement.
     * 
     * @param condition  the condition expression
     * @param thenBranch the then branch statement
     * @param elseBranch the else branch statement (may be null)
     * @return an if statement node
     */
    public static Stmt.If createIfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * Create a loop statement.
     * 
     * @param initializer the initializer variable declaration
     * @param body        the loop body statements
     * @return a loop statement node
     */
    public static Stmt.Loop createLoopStmt(Stmt initializer, List<Stmt> body) {
        return new Stmt.Loop(initializer, body);
    }

    /**
     * Create a loop break test statement.
     * 
     * @param condition the break condition expression
     * @return a loop break test statement node
     */
    public static Stmt.Loop.BreakTest createLoopBreakTestStmt(Expr condition) {
        return new Stmt.Loop.BreakTest(condition);
    }

    /**
     * Create a return statement.
     * 
     * @param value the return value expression (may be null)
     * @return a return statement node
     */
    public static Stmt.Return createReturnStmt(Expr value) {
        return new Stmt.Return(value);
    }

    /**
     * Convert an operator token to the corresponding OpType.
     * 
     * @param operStr the operator string from the parser
     * @return the corresponding OpType
     */
    public static Oper tokenToOpType(String operStr) {
        switch (operStr) {
            // Arithmetic operators
            case "+":
                return Oper.ADD;
            case "-":
                return Oper.SUB;
            case "*":
                return Oper.MUL;
            case "/":
                return Oper.DIV;

            // Relational operators
            case "==":
                return Oper.EQ;
            case "!=":
                return Oper.NE;
            case "<":
                return Oper.LT;
            case "<=":
                return Oper.LE;
            case ">":
                return Oper.GT;
            case ">=":
                return Oper.GE;

            // Logical operators
            case "and":
                return Oper.AND;
            case "or":
                return Oper.OR;
            case "!":
                return Oper.NOT;

            default:
                throw new IllegalArgumentException("Unknown operator: " + operStr);
        }
    }

    /**
     * Create a while statement by translating it to the appropriate Loop structure.
     * 
     * @param condition the loop condition
     * @param body      the loop body statement
     * @return a Loop statement representing the while loop
     */
    public static Stmt.Loop createWhileStmt(Expr condition, Stmt body) {
        // Create empty initializer (while loops don't have initializers)
        Stmt.Empty emptyInitializer = null;

        // Create break test with negated condition (loops continue while condition is
        // true)
        Stmt.Loop.BreakTest breakTest = createLoopBreakTestStmt(
                createUnary(Oper.NOT, condition));

        // Create the loop body statements, including the break test at the beginning
        List<Stmt> loopBody = new ArrayList<>();
        loopBody.add(breakTest);

        // If body is a block, add all its statements, otherwise add the single
        // statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).getStatements());
        } else {
            loopBody.add(body);
        }

        // Create and return the loop
        return createLoopStmt(emptyInitializer, loopBody);
    }

    /**
     * Create an until statement by translating it to the appropriate Loop
     * structure.
     * The until loop executes the body until the condition becomes true.
     * 
     * @param body      the loop body statement
     * @param condition the until condition
     * @return a Loop statement representing the until loop
     */
    public static Stmt.Loop createUntilStmt(Stmt body, Expr condition) {
        // Create empty initializer (until loops don't have initializers)
        Stmt.Empty emptyInitializer = null;

        // Create the loop body statements
        List<Stmt> loopBody = new ArrayList<>();

        // If body is a block, add all its statements, otherwise add the single
        // statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).getStatements());
        } else {
            loopBody.add(body);
        }

        // Add break test at the end (breaks when condition is true)
        Stmt.Loop.BreakTest breakTest = createLoopBreakTestStmt(condition);
        loopBody.add(breakTest);

        // Create and return the loop
        return createLoopStmt(emptyInitializer, loopBody);
    }

    /**
     * Create a repeat statement by translating it to the appropriate Loop
     * structure.
     * The repeat loop executes the body a specified number of times.
     * 
     * @param count the expression that evaluates to the number of repetitions
     * @param body  the loop body statement
     * @return a Loop statement representing the repeat loop
     */
    public static Stmt.Loop createRepeatStmt(Expr count, Stmt body) {
        // Create a counter variable
        SymTableEntry counterEntry = createTempVariable("repeat_counter");

        // Initialize counter to 0
        Expr counterInit = createLiteral(0);
        Expr.Assign assignExpr = createAssign(counterEntry, counterInit);
        Stmt.Expression initializer = createExpressionStmt(assignExpr);

        // Create the loop body statements
        List<Stmt> loopBody = new ArrayList<>();

        // If body is a block, add all its statements, otherwise add the single
        // statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).getStatements());
        } else {
            loopBody.add(body);
        }

        // Increment counter: counter = counter + 1
        Expr varExpr = createVarId(counterEntry);
        Expr plusOne = createBinary(varExpr, Oper.ADD, createLiteral(1));
        Expr.Assign incrementExpr = createAssign(counterEntry, plusOne);
        loopBody.add(createExpressionStmt(incrementExpr));

        // Break test: counter >= count
        Expr breakCondition = createBinary(createVarId(counterEntry), Oper.GE, count);
        Stmt.Loop.BreakTest breakTest = createLoopBreakTestStmt(breakCondition);
        loopBody.add(breakTest);

        // Create and return the loop
        return createLoopStmt(initializer, loopBody);
    }

}
