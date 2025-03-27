package edu.yu.compilers;

import edu.yu.compilers.frontend.semantic.Semantics;
import edu.yu.compilers.intermediate.symbols.SymTable;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.symbols.SymTableStack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticSuccessTest extends OfficialTest {

    private static final Logger logger = LogManager.getLogger(SemanticSuccessTest.class);
    
    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        logger.info("Starting semantic success test");
    }
    
    @AfterEach
    @Override
    void tearDown() {
        super.tearDown();
        logger.info("Completing semantic success test");
    }

    @Test
    @DisplayName("Test variable declaration and initialization")
    void testVariableDeclaration() {
        logger.info("RUNNING TEST: Variable declaration and initialization");
        String code = "var x = 5; var y = \"hello\"; var z = true;";
        
        logger.info("Testing variable declarations with different types");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors");
        
        SymTableStack symTableStack = semantics.getSymTableStack();
        SymTableEntry programId = symTableStack.getProgramId();
        SymTable symbolTable = programId.getRoutineSymTable();

        SymTableEntry xEntry = symbolTable.lookup("x");
        SymTableEntry yEntry = symbolTable.lookup("y");
        SymTableEntry zEntry = symbolTable.lookup("z");
        
        assertNotNull(xEntry, "Variable x should be in symbol table");
        assertNotNull(yEntry, "Variable y should be in symbol table");
        assertNotNull(zEntry, "Variable z should be in symbol table");
        logger.info("Verified all variables are in symbol table");
        
        assertEquals("integer", xEntry.getType().getIdentifier().getName());
        assertEquals("string", yEntry.getType().getIdentifier().getName());
        assertEquals("boolean", zEntry.getType().getIdentifier().getName());
        logger.info("Verified correct type assignment for variables: x={}, y={}, z={}", 
                xEntry.getType().getIdentifier().getName(),
                yEntry.getType().getIdentifier().getName(),
                zEntry.getType().getIdentifier().getName());
    }

    @Test
    @DisplayName("Test function declaration with parameters")
    void testFunctionDeclaration() {
        logger.info("RUNNING TEST: Function declaration with parameters");
        String code = "let add a b = a + b;";
        
        logger.info("Testing function declaration with multiple parameters");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors");
        
        SymTableStack symTableStack = semantics.getSymTableStack();
        SymTableEntry programId = symTableStack.getProgramId();
        SymTableEntry addEntry = programId.getRoutineSymTable().lookup("add");
        
        assertNotNull(addEntry, "Function add should be in symbol table");
        assertEquals(SymTableEntry.Kind.FUNCTION, addEntry.getKind());
        assertEquals(2, addEntry.getRoutineParameters().size(), "Should have 2 parameters");
        logger.info("Verified function entry in symbol table with {} parameters", 
                addEntry.getRoutineParameters().size());
    }

    @Test
    @DisplayName("Test function with block body")
    void testFunctionBlockBody() {
        logger.info("RUNNING TEST: Function with block body");
        String code = "let greet name = { var message = \"Hello, \" + name; return message; }";
        
        logger.info("Testing function with block body containing variable and return statement");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors");
        
        SymTableStack symTableStack = semantics.getSymTableStack();
        SymTableEntry programId = symTableStack.getProgramId();
        SymTableEntry greetEntry = programId.getRoutineSymTable().lookup("greet");
        
        assertNotNull(greetEntry, "Function greet should be in symbol table");
        assertEquals(SymTableEntry.Kind.FUNCTION, greetEntry.getKind());
        logger.info("Verified function entry in symbol table with return type: {}", 
                greetEntry.getType().getIdentifier().getName());
    }

    @Test
    @DisplayName("Test if statement")
    void testIfStatement() {
        logger.info("RUNNING TEST: If statement");
        String code = "var x = 10; if (x > 5) { print x; } else { print \"x is small\"; }";
        
        logger.info("Testing if-else statement with boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in if statement");
    }

    @Test
    @DisplayName("Test while loop")
    void testWhileLoop() {
        logger.info("RUNNING TEST: While loop");
        String code = "var i = 0; while (i < 10) { print i; i = i + 1; }";
        
        logger.info("Testing while loop with boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in while loop");
    }

    @Test
    @DisplayName("Test until loop")
    void testUntilLoop() {
        logger.info("RUNNING TEST: Until loop");
        String code = "var i = 0; loop { print i; i = i + 1; } until (i >= 10);";
        
        logger.info("Testing until loop with boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in until loop");
    }

    @Test
    @DisplayName("Test repeat loop")
    void testRepeatLoop() {
        logger.info("RUNNING TEST: Repeat loop");
        String code = "repeat 5 times { print \"Hello\"; }";
        
        logger.info("Testing repeat loop with integer count");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in repeat loop");
    }

    @Test
    @DisplayName("Test arithmetic expressions")
    void testArithmeticExpressions() {
        logger.info("RUNNING TEST: Arithmetic expressions");
        String code = "var a = 5; var b = 10; var c = a + b; var d = a * b; var e = a / b; var f = -a;";
        
        logger.info("Testing various arithmetic expressions (+, *, /, -)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in arithmetic expressions");
    }

    @Test
    @DisplayName("Test logical expressions")
    void testLogicalExpressions() {
        logger.info("RUNNING TEST: Logical expressions");
        String code = "var a = true; var b = false; var c = a and b; var d = a or b; var e = !a;";
        
        logger.info("Testing various logical expressions (and, or, !)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in logical expressions");
    }

    @Test
    @DisplayName("Test comparison expressions")
    void testComparisonExpressions() {
        logger.info("RUNNING TEST: Comparison expressions");
        String code = "var a = 5; var b = 10; var c = a > b; var d = a >= b; var e = a < b; var f = a <= b;";
        
        logger.info("Testing various comparison expressions (>, >=, <, <=)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in comparison expressions");
    }

    @Test
    @DisplayName("Test equality expressions")
    void testEqualityExpressions() {
        logger.info("RUNNING TEST: Equality expressions");
        String code = "var a = 5; var b = 10; var c = true; var d = a == b; var e = a != b; var f = c == true;";
        
        logger.info("Testing various equality expressions (==, !=)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in equality expressions");
    }

    @Test
    @DisplayName("Test function calls")
    void testFunctionCalls() {
        logger.info("RUNNING TEST: Function calls");
        String code = "let add a b = a + b; var result = add(5, 10);";
        
        logger.info("Testing function call with correct arguments");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in function call");
    }

    @Test
    @DisplayName("Test nested scopes")
    void testNestedScopes() {
        logger.info("RUNNING TEST: Nested scopes");
        String code = "var x = 5; { var y = 10; { var z = 15; print x + y + z; } }";
        
        logger.info("Testing nested block scopes and variable access");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in nested scopes");
    }

    @Test
    @DisplayName("Test string concatenation")
    void testStringConcatenation() {
        logger.info("RUNNING TEST: String concatenation");
        String code = "var a = \"Hello\"; var b = \"World\"; var c = a + b;";
        
        logger.info("Testing string concatenation with + operator");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in string concatenation");
    }

    @Test
    @DisplayName("Test chained comparison operators")
    void testChainedComparisons() {
        logger.info("RUNNING TEST: Chained comparison operators");
        String code = "var a = 5; var b = 10; var c = 15; var d = a < b < c;";
        
        logger.info("Testing chained comparison operators");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in chained comparisons");
    }
    
    @Test
    @DisplayName("Test complex nested function definitions")
    void testNestedFunctions() {
        logger.info("RUNNING TEST: Complex nested function definitions");
        String code = "let outer x = { let inner y = x + y; var result = inner(10); return result; }";
        
        logger.info("Testing nested function definitions and variable scope");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in nested functions");
    }
    
    @Test
    @DisplayName("Test numeric type coercion in expressions")
    void testNumericCoercion() {
        logger.info("RUNNING TEST: Numeric type coercion");
        String code = "var a = 5; var b = 2.5; var c = a + b;";
        
        logger.info("Testing numeric type coercion between integer and real");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Should have no semantic errors");
        logger.info("Verified no semantic errors in numeric coercion");
    }
}