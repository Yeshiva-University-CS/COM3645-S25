package edu.yu.compilers;

import edu.yu.compilers.frontend.Semantics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticErrorTest extends OfficialTest {

    private static final Logger logger = LogManager.getLogger(SemanticErrorTest.class);
    
    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        logger.info("Starting semantic error test");
    }
    
    @AfterEach
    @Override
    void tearDown() {
        super.tearDown();
        logger.info("Completing semantic error test");
    }

    @Test
    @DisplayName("Test error: redeclared identifier")
    void testRedeclaredIdentifier() {
        logger.info("RUNNING TEST: Redeclared identifier");
        String code = "var x = 5; var x = 10;";
        
        logger.info("Testing redeclaration of variable 'x'");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: redeclared function")
    void testRedeclaredFunction() {
        logger.info("RUNNING TEST: Redeclared function");
        String code = "let func a = a + 1; let func b = b * 2;";
        
        logger.info("Testing redeclaration of function 'func'");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: redeclared parameter")
    void testRedeclaredParameter() {
        logger.info("RUNNING TEST: Redeclared parameter");
        String code = "let duplicate a a = a + a;";
        
        logger.info("Testing function with duplicate parameter name 'a'");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: undeclared identifier")
    void testUndeclaredIdentifier() {
        logger.info("RUNNING TEST: Undeclared identifier");
        String code = "print y;";
        
        logger.info("Testing use of undeclared variable 'y'");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in assignment")
    void testTypeMismatchInAssignment() {
        logger.info("RUNNING TEST: Type mismatch in assignment");
        String code = "var x = 5; var y = \"hello\"; x = y;";
        
        logger.info("Testing assignment of string to integer variable");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in if condition")
    void testTypeMismatchInIfCondition() {
        logger.info("RUNNING TEST: Type mismatch in if condition");
        String code = "var x = 5; if (x) { print x; }";
        
        logger.info("Testing if statement with non-boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in while condition")
    void testTypeMismatchInWhileCondition() {
        logger.info("RUNNING TEST: Type mismatch in while condition");
        String code = "var x = 5; while (x) { print x; }";
        
        logger.info("Testing while loop with non-boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in until condition")
    void testTypeMismatchInUntilCondition() {
        logger.info("RUNNING TEST: Type mismatch in until condition");
        String code = "var x = 5; loop { print x; } until (x);";
        
        logger.info("Testing until loop with non-boolean condition");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in repeat count")
    void testTypeMismatchInRepeatCount() {
        logger.info("RUNNING TEST: Type mismatch in repeat count");
        String code = "var x = true; repeat x times { print \"Hello\"; }";
        
        logger.info("Testing repeat loop with non-integer count");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in logical expression")
    void testTypeMismatchInLogicalExpression() {
        logger.info("RUNNING TEST: Type mismatch in logical expression");
        String code = "var x = 5; var y = true; var z = x and y;";
        
        logger.info("Testing logical AND with non-boolean operand");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in equality expression")
    void testTypeMismatchInEqualityExpression() {
        logger.info("RUNNING TEST: Type mismatch in equality expression");
        // Note: Your implementation might allow comparing any types with == and !=
        // Update test accordingly if your design allows this
        String code = "var x = 5; var y = true; var z = (x < y);";
        
        logger.info("Testing comparison between incompatible types (integer and boolean)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in arithmetic expression")
    void testTypeMismatchInArithmeticExpression() {
        logger.info("RUNNING TEST: Type mismatch in arithmetic expression");
        String code = "var x = 5; var y = true; var z = x + y;";
        
        logger.info("Testing addition with incompatible types (integer and boolean)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: type mismatch in unary expression")
    void testTypeMismatchInUnaryExpression() {
        logger.info("RUNNING TEST: Type mismatch in unary expression");
        String code = "var x = 5; var y = !x;";
        
        logger.info("Testing logical NOT on non-boolean value");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: invalid assignment")
    void testInvalidAssignment() {
        logger.info("RUNNING TEST: Invalid assignment");
        String code = "let add a b = a + b; add = 5;";
        
        logger.info("Testing assignment to function identifier");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: invalid function")
    void testInvalidFunction() {
        logger.info("RUNNING TEST: Invalid function");
        String code = "var x = 5; x(10);";
        
        logger.info("Testing calling a variable as if it were a function");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: argument count mismatch")
    void testArgumentCountMismatch() {
        logger.info("RUNNING TEST: Argument count mismatch");
        String code = "let add a b = a + b; var result = add(5);";
        
        logger.info("Testing function call with too few arguments");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: too many arguments")
    void testTooManyArguments() {
        logger.info("RUNNING TEST: Too many arguments");
        String code = "let add a b = a + b; var result = add(5, 10, 15);";
        
        logger.info("Testing function call with too many arguments");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: misplaced return")
    void testMisplacedReturn() {
        logger.info("RUNNING TEST: Misplaced return");
        String code = "return 5;";
        
        logger.info("Testing return statement outside of function");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test compound errors")
    void testCompoundErrors() {
        logger.info("RUNNING TEST: Compound errors");
        String code = "var x = 5; var x = 10; print y; return 42;";
        
        logger.info("Testing code with multiple errors: redeclared variable, undeclared variable, misplaced return");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(3, semantics.getErrorCount(), "Should have 3 semantic errors");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test return type consistency")
    void testReturnTypeConsistency() {
        logger.info("RUNNING TEST: Return type consistency");
        String code = "let inconsistent a = { if (a > 0) { return true; } else { return \"negative\"; } }";
        
        logger.info("Testing inconsistent return types in function");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }

    @Test
    @DisplayName("Test error: calling a non-existing function")
    void testCallingNonExistingFunction() {
        logger.info("RUNNING TEST: Calling a non-existing function");
        String code = "var result = nonExistingFunction(5, 10);";
        
        logger.info("Testing call to undeclared function");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }
    
    @Test
    @DisplayName("Test error: using incompatible types in comparison")
    void testIncompatibleTypesInComparison() {
        logger.info("RUNNING TEST: Incompatible types in comparison");
        String code = "var str = \"hello\"; var num = 42; var result = str > num;";
        
        logger.info("Testing comparison between string and integer");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(1, semantics.getErrorCount(), "Should have 1 semantic error");
        logger.info("Verified correct semantic error count: {}", semantics.getErrorCount());
    }
    
    @Test
    @DisplayName("Test error: nested scope variable shadowing")
    void testNestedScopeVariableShadowing() {
        logger.info("RUNNING TEST: Nested scope variable shadowing");
        // Variable shadowing is not an error but worth testing
        String code = "var x = 5; { var x = 10; print x; }";
        
        logger.info("Testing variable shadowing in nested scope (should be valid)");
        Semantics semantics = analyzeCode(code);
        
        assertEquals(0, semantics.getErrorCount(), "Variable shadowing should not cause semantic errors");
        logger.info("Verified variable shadowing is allowed: {} errors", semantics.getErrorCount());
    }
}