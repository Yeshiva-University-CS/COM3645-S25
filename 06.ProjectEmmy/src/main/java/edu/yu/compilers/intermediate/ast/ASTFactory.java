package edu.yu.compilers.intermediate.ast;

import java.util.ArrayList;
import java.util.List;

import edu.yu.compilers.intermediate.symtable.SymTableEntry;

/**
 * Factory class for constructing Abstract Syntax Tree (AST) nodes.
 * This class simplifies the creation of expression and statement nodes
 * for the Emmy language compiler.
 */
public class ASTFactory {
    
    /**
     * Create a variable expression.
     * @param entry symbol table entry for the variable
     * @return a variable expression node
     */
    public static Expr.Variable createVariable(SymTableEntry entry) {
        return new Expr.Variable(entry);
    }
    
    /**
     * Create a literal expression.
     * @param value the literal value
     * @return a literal expression node
     */
    public static Expr.Literal createLiteral(Object value) {
        return new Expr.Literal(value);
    }
    
    /**
     * Create a unary expression.
     * @param operator the unary operator
     * @param operand the operand expression
     * @return a unary expression node
     */
    public static Expr.Unary createUnary(OpType operator, Expr operand) {
        return new Expr.Unary(operator, operand);
    }
    
    /**
     * Create a binary expression.
     * @param left the left operand expression
     * @param operator the binary operator
     * @param right the right operand expression
     * @return a binary expression node
     */
    public static Expr.Binary createBinary(Expr left, OpType operator, Expr right) {
        return new Expr.Binary(left, operator, right);
    }
    
    /**
     * Create a logical expression.
     * @param left the left operand expression
     * @param operator the logical operator
     * @param right the right operand expression
     * @return a logical expression node
     */
    public static Expr.Logical createLogical(Expr left, OpType operator, Expr right) {
        return new Expr.Logical(left, operator, right);
    }
    
    /**
     * Create an assignment expression.
     * @param entry symbol table entry for the variable
     * @param value the expression to assign
     * @return an assignment expression node
     */
    public static Expr.Assign createAssign(SymTableEntry entry, Expr value) {
        return new Expr.Assign(entry, value);
    }
    
    /**
     * Create a function call expression.
     * @param callee the function to call
     * @param arguments the argument expressions
     * @return a call expression node
     */
    public static Expr.Call createCall(Expr callee, List<Expr> arguments) {
        return new Expr.Call(callee, arguments);
    }
    
    /**
     * Create a function call expression with no arguments.
     * @param callee the function to call
     * @return a call expression node
     */
    public static Expr.Call createCall(Expr callee) {
        return new Expr.Call(callee, new ArrayList<>());
    }
    
    /**
     * Create an expression statement.
     * @param expression the expression
     * @return an expression statement node
     */
    public static Stmt.Expression createExpressionStmt(Expr expression) {
        return new Stmt.Expression(expression);
    }
    
    /**
     * Create a print statement.
     * @param expression the expression to print
     * @return a print statement node
     */
    public static Stmt.Print createPrintStmt(Expr expression) {
        return new Stmt.Print(expression);
    }
    
    /**
     * Create a variable declaration statement.
     * @param entry symbol table entry for the variable
     * @param initializer the initializer expression (may be null)
     * @return a variable declaration statement node
     */
    public static Stmt.Var createVarStmt(SymTableEntry entry, Expr initializer) {
        return new Stmt.Var(entry, initializer);
    }
    
    /**
     * Create a block statement.
     * @param statements the list of statements in the block
     * @return a block statement node
     */
    public static Stmt.Block createBlockStmt(List<Stmt> statements) {
        return new Stmt.Block(statements);
    }
    
    /**
     * Create an if statement.
     * @param condition the condition expression
     * @param thenBranch the then branch statement
     * @param elseBranch the else branch statement (may be null)
     * @return an if statement node
     */
    public static Stmt.If createIfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    
    /**
     * Create a loop statement.
     * @param initializer the initializer variable declaration
     * @param body the loop body statements
     * @return a loop statement node
     */
    public static Stmt.Loop createLoopStmt(Stmt.Var initializer, List<Stmt> body) {
        return new Stmt.Loop(initializer, body);
    }
    
    /**
     * Create a loop break test statement.
     * @param condition the break condition expression
     * @return a loop break test statement node
     */
    public static Stmt.Loop.BreakTest createLoopBreakTestStmt(Expr condition) {
        return new Stmt.Loop.BreakTest(condition);
    }
    
    /**
     * Create a return statement.
     * @param value the return value expression (may be null)
     * @return a return statement node
     */
    public static Stmt.Return createReturnStmt(Expr value) {
        return new Stmt.Return(value);
    }
    
    /**
     * Create a function declaration statement.
     * @param entry symbol table entry for the function
     * @param body the function body statements
     * @return a function declaration statement node
     */
    public static Stmt.Function createFunctionStmt(SymTableEntry entry, List<Stmt> body) {
        return new Stmt.Function(entry, body);
    }
    
    /**
     * Convert an operator token to the corresponding OpType.
     * @param operStr the operator string from the parser
     * @return the corresponding OpType
     */
    public static OpType tokenToOpType(String operStr) {
        // This method would map token types from your parser to OpType values
        // The implementation depends on your token type constants
        switch (operStr) {
            // Arithmetic operators
            case "+":    return OpType.ADD;
            case "-":    return OpType.SUB;
            case "*":    return OpType.MUL;
            case "/":    return OpType.DIV;
            
            // Relational operators
            case "==":   return OpType.EQ;
            case "!=":   return OpType.NE;
            case "<":    return OpType.LT;
            case "<=":   return OpType.LE;
            case ">":    return OpType.GT;
            case ">=":   return OpType.GE;
            
            // Logical operators
            case "and":  return OpType.AND;
            case "or":   return OpType.OR;
            case "!":    return OpType.NOT;
            
            default:
                throw new IllegalArgumentException("Unknown operator: " + operStr);
        }
    }
    
    /**
     * Check if an expression is a valid function callee.
     * This can be used to validate that only appropriate expressions can be called.
     * @param expr the callee expression
     * @return true if the expression can be a callee, false otherwise
     */
    public static boolean isValidCallee(Expr expr) {
        return !(expr instanceof Expr.Literal && 
               (((Expr.Literal)expr).value == Boolean.TRUE || 
                ((Expr.Literal)expr).value == Boolean.FALSE || 
                ((Expr.Literal)expr).value == null));
    }
    
    /**
     * Create a while statement by translating it to the appropriate Loop structure.
     * @param condition the loop condition
     * @param body the loop body statement
     * @return a Loop statement representing the while loop
     */
    public static Stmt.Loop createWhileStmt(Expr condition, Stmt body) {
        // Create empty initializer (while loops don't have initializers)
        Stmt.Var emptyInitializer = null;
        
        // Create break test with negated condition (loops continue while condition is true)
        Stmt.Loop.BreakTest breakTest = createLoopBreakTestStmt(
            createUnary(OpType.NOT, condition));
        
        // Create the loop body statements, including the break test at the beginning
        List<Stmt> loopBody = new ArrayList<>();
        loopBody.add(breakTest);
        
        // If body is a block, add all its statements, otherwise add the single statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).statements);
        } else {
            loopBody.add(body);
        }
        
        // Create and return the loop
        return createLoopStmt(emptyInitializer, loopBody);
    }
    
    /**
     * Create an until statement by translating it to the appropriate Loop structure.
     * The until loop executes the body until the condition becomes true.
     * 
     * @param body the loop body statement
     * @param condition the until condition
     * @return a Loop statement representing the until loop
     */
    public static Stmt.Loop createUntilStmt(Stmt body, Expr condition) {
        // Create empty initializer (until loops don't have initializers)
        Stmt.Var emptyInitializer = null;
        
        // Create the loop body statements
        List<Stmt> loopBody = new ArrayList<>();
        
        // If body is a block, add all its statements, otherwise add the single statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).statements);
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
     * Create a repeat statement by translating it to the appropriate Loop structure.
     * The repeat loop executes the body a specified number of times.
     * 
     * @param count the expression that evaluates to the number of repetitions
     * @param body the loop body statement
     * @return a Loop statement representing the repeat loop
     */
    public static Stmt.Loop createRepeatStmt(Expr count, Stmt body) {
        // We'll need a counter variable
        // This would typically be created through the symbol table,
        // but for this example we'll assume a helper method would be used
        SymTableEntry counterEntry = null; // This would come from the symbol table
        
        // Initialize counter to 0
        Expr counterInit = createLiteral(0);
        Stmt.Var initializer = createVarStmt(counterEntry, counterInit);
        
        // Create the loop body statements
        List<Stmt> loopBody = new ArrayList<>();
        
        // If body is a block, add all its statements, otherwise add the single statement
        if (body instanceof Stmt.Block) {
            loopBody.addAll(((Stmt.Block) body).statements);
        } else {
            loopBody.add(body);
        }
        
        // Increment counter: counter = counter + 1
        Expr varExpr = createVariable(counterEntry);
        Expr plusOne = createBinary(varExpr, OpType.ADD, createLiteral(1));
        Expr.Assign incrementExpr = createAssign(counterEntry, plusOne);
        loopBody.add(createExpressionStmt(incrementExpr));
        
        // Break test: counter >= count
        Expr breakCondition = createBinary(varExpr, OpType.GE, count);
        Stmt.Loop.BreakTest breakTest = createLoopBreakTestStmt(breakCondition);
        loopBody.add(breakTest);
        
        // Create and return the loop
        return createLoopStmt(initializer, loopBody);
    }
}
