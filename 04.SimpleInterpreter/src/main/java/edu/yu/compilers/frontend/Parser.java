/**
 * Adapted from
 * Parser class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.frontend;

import edu.yu.compilers.intermediate.Node;
import edu.yu.compilers.intermediate.SymTable;
import edu.yu.compilers.intermediate.SymTableEntry;

import java.util.HashSet;
import java.util.Objects;

import static edu.yu.compilers.frontend.Token.TokenType.*;
import static edu.yu.compilers.intermediate.Node.NodeType.*;

public class Parser {
    private static final HashSet<Token.TokenType> statementStarters;
    private static final HashSet<Token.TokenType> statementFollowers;
    private static final HashSet<Token.TokenType> relationalOperators;
    private static final HashSet<Token.TokenType> simpleExpressionOperators;
    private static final HashSet<Token.TokenType> termOperators;

    static {
        statementStarters = new HashSet<>();
        statementFollowers = new HashSet<>();
        relationalOperators = new HashSet<>();
        simpleExpressionOperators = new HashSet<>();
        termOperators = new HashSet<>();

        // Tokens that can start a statement.
        statementStarters.add(BEGIN);
        statementStarters.add(IDENTIFIER);
        statementStarters.add(REPEAT);
        statementStarters.add(WHILE);
        statementStarters.add(FOR);
        statementStarters.add(Token.TokenType.IF);
        statementStarters.add(CASE);
        statementStarters.add(Token.TokenType.WRITE);
        statementStarters.add(Token.TokenType.WRITELN);

        // Tokens that can immediately follow a statement.
        statementFollowers.add(SEMICOLON);
        statementFollowers.add(END);
        statementFollowers.add(UNTIL);
        statementFollowers.add(END_OF_FILE);

        relationalOperators.add(EQUALS);
        relationalOperators.add(NOT_EQUALS);
        relationalOperators.add(LESS_THAN);
        relationalOperators.add(LESS_EQUALS);
        relationalOperators.add(GREATER_THAN);
        relationalOperators.add(GREATER_EQUALS);

        simpleExpressionOperators.add(PLUS);
        simpleExpressionOperators.add(MINUS);
        simpleExpressionOperators.add(Token.TokenType.OR);

        termOperators.add(STAR);
        termOperators.add(SLASH);
        termOperators.add(DIV);
        termOperators.add(MOD);
        termOperators.add(Token.TokenType.AND);
    }

    private final Scanner scanner;
    private final SymTable symTable;
    private Token currentToken;
    private int lineNumber;
    private int errorCount;

    public Parser(Scanner scanner, SymTable symTable) {
        this.scanner = scanner;
        this.symTable = symTable;
        this.currentToken = null;
        this.lineNumber = 1;
        this.errorCount = 0;
    }

    public int errorCount() {
        return errorCount;
    }

    public Node parseProgram() {
        Node programNode = new Node(Node.NodeType.PROGRAM);

        currentToken = scanner.nextToken();  // first token!

        if (currentToken.type == Token.TokenType.PROGRAM) {
            currentToken = scanner.nextToken();  // consume PROGRAM
        } else syntaxError("Expecting PROGRAM");

        if (currentToken.type == IDENTIFIER) {
            String programName = currentToken.text;
            symTable.enter(programName);
            programNode.text = programName;

            currentToken = scanner.nextToken();  // consume program name
        } else syntaxError("Expecting program name");

        if (currentToken.type == SEMICOLON) {
            currentToken = scanner.nextToken();  // consume ;
        } else syntaxError("Missing ;");

        if (currentToken.type != BEGIN) syntaxError("Expecting BEGIN");

        // The PROGRAM node adopts the COMPOUND tree.
        programNode.adopt(parseCompoundStatement());

        if (currentToken.type != PERIOD) syntaxError("Expecting .");
        return programNode;
    }

    private Node parseStatement() {
        Node stmtNode = null;
        int savedLineNumber = currentToken.lineNumber;
        lineNumber = savedLineNumber;

        switch (currentToken.type) {
            case IDENTIFIER -> stmtNode = parseAssignmentStatement();
            case BEGIN -> stmtNode = parseCompoundStatement();
            case REPEAT -> stmtNode = parseRepeatStatement();
            case WHILE -> stmtNode = parseWhileStatement();
            case FOR -> stmtNode = parseForStatement();
            case IF -> stmtNode = parseIfStatement();
            case CASE -> stmtNode = parseCaseStatement();
            case WRITE -> stmtNode = parseWriteStatement();
            case WRITELN -> stmtNode = parseWritelnStatement();
            case SEMICOLON -> {
            } // empty statement

            default -> syntaxError("Unexpected token");
        }

        if (stmtNode != null) stmtNode.lineNumber = savedLineNumber;
        return stmtNode;
    }

    private Node parseAssignmentStatement() {
        // The current token should now be the left-hand-side variable name.

        Node assignmentNode = new Node(ASSIGN);

        // Enter the variable name into the symbol table
        // if it isn't already in there.
        String variableName = currentToken.text;
        SymTableEntry variableId = symTable.lookup(variableName.toLowerCase());
        if (variableId == null) variableId = symTable.enter(variableName);

        // The assignment node adopts the variable node as its first child.
        Node lhsNode = new Node(VARIABLE);
        lhsNode.text = variableName;
        lhsNode.entry = variableId;
        assignmentNode.adopt(lhsNode);

        currentToken = scanner.nextToken();  // consume the LHS variable;

        if (currentToken.type == COLON_EQUALS) {
            currentToken = scanner.nextToken();  // consume :=
        } else syntaxError("Missing :=");

        // The assignment node adopts the expression node as its second child.
        Node rhsNode = parseExpression();
        assignmentNode.adopt(rhsNode);

        return assignmentNode;
    }

    private Node parseCompoundStatement() {
        Node compoundNode = new Node(COMPOUND);
        compoundNode.lineNumber = currentToken.lineNumber;

        currentToken = scanner.nextToken();  // consume BEGIN
        parseStatementList(compoundNode, END);

        if (currentToken.type == END) {
            currentToken = scanner.nextToken();  // consume END
        } else syntaxError("Expecting END");

        return compoundNode;
    }

    private void parseStatementList(Node parentNode, Token.TokenType terminalType) {
        while ((currentToken.type != terminalType) && (currentToken.type != END_OF_FILE)) {
            Node stmtNode = parseStatement();
            if (stmtNode != null) parentNode.adopt(stmtNode);

            // A semicolon separates statements.
            if (currentToken.type == SEMICOLON) {
                while (currentToken.type == SEMICOLON) {
                    currentToken = scanner.nextToken();  // consume ;
                }
            } else if (statementStarters.contains(currentToken.type)) {
                syntaxError("Missing ;");
            }
        }
    }

    private Node parseRepeatStatement() {
        // The current token should now be REPEAT.

        // Create a LOOP node.
        Node loopNode = new Node(LOOP);
        currentToken = scanner.nextToken();  // consume REPEAT

        parseStatementList(loopNode, UNTIL);

        if (currentToken.type == UNTIL) {
            // Create a TEST node. It adopts the test expression node.
            Node testNode = new Node(TEST);
            lineNumber = currentToken.lineNumber;
            testNode.lineNumber = lineNumber;
            currentToken = scanner.nextToken();  // consume UNTIL

            testNode.adopt(parseExpression());

            // The LOOP node adopts the TEST node as its final child.
            loopNode.adopt(testNode);
        } else syntaxError("Expecting UNTIL");

        return loopNode;
    }

    private Node parseWhileStatement() {
        // The current token should now be WHILE.

        // Create a LOOP node.
        Node loopNode = new Node(LOOP);
        currentToken = scanner.nextToken();  // consume WHILE

        // Create a TEST node and a NOT node.
        // The LOOP node adopts the TEST node.
        // The TEST node adopts the NOT node.
        Node testNode = new Node(TEST);
        Node notNode = new Node(Node.NodeType.NOT);
        loopNode.adopt(testNode);
        testNode.adopt(notNode);

        // The NOT node adopts the expression subtree.
        notNode.adopt(parseExpression());

        // The current token should now be DO.
        if (currentToken.type != DO) syntaxError("Expecting DO");
        else {
            currentToken = scanner.nextToken();  // consume DO
        }

        // The LOOP node adopts the statement;
        loopNode.adopt(parseStatement());
        return loopNode;
    }

    private Node parseForStatement() {
        // The current token should now be FOR.

        // Create a COMPOUND node.
        Node compoundNode = new Node(COMPOUND);
        currentToken = scanner.nextToken();  // consume FOR

        // The COMPOUND node adopts the control variable initialization.
        Node assignNode = parseAssignmentStatement();
        compoundNode.adopt(assignNode);

        // Get the tree node of the control variable.
        Node controlNode = assignNode.children.get(0);

        // The COMPOUND node's second child is a LOOP node.
        Node loopNode = new Node(LOOP);
        compoundNode.adopt(loopNode);

        // The LOOP node's first child is the TEST node.
        Node testNode = new Node(TEST);
        loopNode.adopt(testNode);

        // The current token should be TO or DOWNTO.
        boolean countUp = true;
        if (currentToken.type == TO) {
            currentToken = scanner.nextToken();  // consume TO
        } else if (currentToken.type == DOWNTO) {
            countUp = false;
            currentToken = scanner.nextToken();  // consume DOWNTO
        } else syntaxError("Expecting TO or DOWNTO");

        // Test against the terminal expression.
        Node compareNode = countUp ? new Node(GT) : new Node(LT);
        testNode.adopt(compareNode);
        compareNode.adopt(controlNode.copy());
        compareNode.adopt(parseExpression());  // terminating expression

        // The current token should be DO.
        if (currentToken.type == DO) {
            currentToken = scanner.nextToken();  // consume DO
        } else syntaxError("Expecting DO");

        // The LOOP node's second child is the statement.
        loopNode.adopt(parseStatement());

        // The LOOP node's third child is the assignment
        // to increment or decrement the control variable.
        assignNode = new Node(ASSIGN);
        loopNode.adopt(assignNode);
        assignNode.adopt(controlNode.copy());
        Node opNode = countUp ? new Node(ADD) : new Node(SUBTRACT);
        assignNode.adopt(opNode);
        opNode.adopt(controlNode.copy());
        Node oneNode = new Node(INTEGER_CONSTANT);
        oneNode.value = 1L;
        opNode.adopt(oneNode);

        return compoundNode;
    }

    private Node parseIfStatement() {
        // The current token should now be IF.

        // Create an IF node.
        Node ifNode = new Node(Node.NodeType.IF);
        currentToken = scanner.nextToken();  // consume IF

        // The IF node adopts the expression subtree as its first child.
        ifNode.adopt(parseExpression());

        if (currentToken.type != THEN) syntaxError("Expecting THEN");
        else {
            currentToken = scanner.nextToken();  // consume THEN
        }

        // The IF node adopts the THEN statement subtree as its second child.
        ifNode.adopt(parseStatement());

        // If there's an "ELSE" reserved word,
        // the IF node adopts the ELSE statement subtree as its third child.
        if (currentToken.type == ELSE) {
            currentToken = scanner.nextToken();  // consume ELSE
            ifNode.adopt(parseStatement());
        }

        return ifNode;
    }

    private Node parseCaseStatement() {
        // The current token should now be CASE.

        // Create a SWITCH node.
        Node switchNode = new Node(SWITCH);
        currentToken = scanner.nextToken();  // consume CASE

        // The SWITCH node adopts the expression subtree.
        Node exprNode = parseExpression();
        switchNode.adopt(exprNode);

        if (currentToken.type == OF) {
            currentToken = scanner.nextToken();  // consume OF
        } else syntaxError("Expecting OF");

        // Parse CASE branches.
        while ((currentToken.type == INTEGER) || (currentToken.type == PLUS) || (currentToken.type == MINUS)) {
            // The SWITCH node adopts a SELECT_BRANCH node.
            // The SELECT_BRANCH node adopts a SELECT_CONSTANTS node.
            Node branchNode = new Node(SELECT_BRANCH);
            Node constantsNode = new Node(SELECT_CONSTANTS);
            switchNode.adopt(branchNode);
            branchNode.adopt(constantsNode);

            // Parse comma-separated integer constants of a CASE branch until :
            // The constant may be preceded by + or -
            // The SELECT_CONSTANTS node adopts each INTEGER_CONSTANT node.
            do {
                boolean negate = false;
                if ((currentToken.type == PLUS) || (currentToken.type == MINUS)) {
                    negate = currentToken.type == MINUS;
                    currentToken = scanner.nextToken();  // consume + or -
                }

                Node constantNode = parseIntegerConstant();
                if (negate) constantNode.value = -((long) constantNode.value);
                constantsNode.adopt(constantNode);

                if (currentToken.type == COMMA) {
                    currentToken = scanner.nextToken();  // consume ,
                }
            } while (currentToken.type != COLON);

            currentToken = scanner.nextToken();  // consume :

            // The SELECT_BRANCH node adopts the branch statement subtree.
            branchNode.adopt(parseStatement());

            // Consume semicolons.
            if (currentToken.type == SEMICOLON) {
                do {
                    currentToken = scanner.nextToken();  // consume ;
                } while (currentToken.type == SEMICOLON);
            }
        }

        // The current token should now be END.
        if (currentToken.type == END) {
            currentToken = scanner.nextToken();  // consume END
        } else if (statementStarters.contains(currentToken.type)) {
            syntaxError("Missing END");
        }

        return switchNode;
    }

    private Node parseWriteStatement() {
        // The current token should now be WRITE.

        // Create a WRITE node. It adopts the variable or string node.
        Node writeNode = new Node(Node.NodeType.WRITE);
        currentToken = scanner.nextToken();  // consume WRITE

        parseWriteArguments(writeNode);
        if (writeNode.children.size() == 0) {
            syntaxError("Invalid WRITE statement");
        }

        return writeNode;
    }

    private Node parseWritelnStatement() {
        // The current token should now be WRITELN.

        // Create a WRITELN node. It adopts the variable or string node.
        Node writelnNode = new Node(Node.NodeType.WRITELN);
        currentToken = scanner.nextToken();  // consume WRITELN

        if (currentToken.type == LPAREN) parseWriteArguments(writelnNode);
        return writelnNode;
    }

    private void parseWriteArguments(Node node) {
        // The current token should now be (

        boolean hasArgument = false;

        if (currentToken.type == LPAREN) {
            currentToken = scanner.nextToken();  // consume (
        } else syntaxError("Missing left parenthesis");

        if (currentToken.type == IDENTIFIER) {
            node.adopt(parseVariable());
            hasArgument = true;
        } else if ((currentToken.type == CHARACTER) || (currentToken.type == STRING)) {
            node.adopt(parseStringConstant());
            hasArgument = true;
        } else syntaxError("Invalid WRITE or WRITELN statement");

        // Look for a field width and a count of decimal places.
        if (hasArgument) {
            if (currentToken.type == COLON) {
                currentToken = scanner.nextToken();  // consume ,

                if (currentToken.type == INTEGER) {
                    // Field width
                    node.adopt(parseIntegerConstant());

                    if (currentToken.type == COLON) {
                        currentToken = scanner.nextToken();  // consume ,

                        if (currentToken.type == INTEGER) {
                            // Count of decimal places
                            node.adopt(parseIntegerConstant());
                        } else syntaxError("Invalid count of decimal places");
                    }
                } else syntaxError("Invalid field width");
            }
        }

        if (currentToken.type == RPAREN) {
            currentToken = scanner.nextToken();  // consume )
        } else syntaxError("Missing right parenthesis");
    }

    private Node parseExpression() {
        // The current token should now be an identifier or a number
        // or + or -

        // The expression's root node.
        Node exprNode = parseSimpleExpression();

        // The current token might now be a relational operator.
        if (relationalOperators.contains(currentToken.type)) {
            Token.TokenType tokenType = currentToken.type;
            Node opNode = null;

            switch (tokenType) {
                case EQUALS -> opNode = new Node(EQ);
                case NOT_EQUALS -> opNode = new Node(NE);
                case LESS_THAN -> opNode = new Node(LT);
                case LESS_EQUALS -> opNode = new Node(LE);
                case GREATER_THAN -> opNode = new Node(GT);
                case GREATER_EQUALS -> opNode = new Node(GE);
                default -> syntaxError("Unexpected token");
            }

            currentToken = scanner.nextToken();  // consume relational operator

            // The relational operator node adopts the first simple expression
            // node as its first child and the second simple expression node
            // as its second child. Then it becomes the expression's root node.
            if (opNode != null) {
                opNode.adopt(exprNode);
                opNode.adopt(parseSimpleExpression());
                exprNode = opNode;
            }
        }

        return exprNode;
    }

    private Node parseSimpleExpression() {
        // The current token should now be an identifier or a number
        // or + or -

        // The simple expression's root node.
        Node simpExprNode = parseTerm();

        // Keep parsing more terms as long as the current token
        // is a + or - operator.
        while (simpleExpressionOperators.contains(currentToken.type)) {
            Node opNode = null;

            switch (currentToken.type) {
                case PLUS -> opNode = new Node(ADD);
                case MINUS -> opNode = new Node(SUBTRACT);
                case OR -> opNode = new Node(Node.NodeType.OR);
                default -> syntaxError("Unexpected token");
            }

            currentToken = scanner.nextToken();  // consume the operator

            // The simple expression node adopts the first term node as its
            // first child and the next term node as its second child.
            // Then it becomes the simple expression's root node.
            Objects.requireNonNull(opNode).adopt(simpExprNode);
            opNode.adopt(parseTerm());
            simpExprNode = opNode;
        }

        return simpExprNode;
    }

    private Node parseTerm() {
        // The current token should now be an identifier or a number
        // or - or +

        // The term's root node.
        Node termNode;

        if (currentToken.type == PLUS) {
            currentToken = scanner.nextToken();  // consume +
            termNode = parseFactor();
        } else if (currentToken.type == MINUS) {
            currentToken = scanner.nextToken();  // consume -
            termNode = new Node(NEGATE);
            termNode.adopt(parseFactor());
        } else termNode = parseFactor();

        // Keep parsing more factors as long as the current token
        // is a * or / operator.
        while (termOperators.contains(currentToken.type)) {
            Node opNode = null;

            switch (currentToken.type) {
                case STAR -> opNode = new Node(MULTIPLY);
                case SLASH -> opNode = new Node(DIVIDE);
                case DIV -> opNode = new Node(INTEGER_DIVIDE);
                case MOD -> opNode = new Node(MODULO);
                case AND -> opNode = new Node(Node.NodeType.AND);
                default -> syntaxError("Unexpected token");
            }

            currentToken = scanner.nextToken();  // consume the operator

            // The multiply or divide node adopts the first factor node as its
            // first child and the next factor node as its second child.
            // Then it becomes the term's root node.
            Objects.requireNonNull(opNode).adopt(termNode);
            opNode.adopt(parseFactor());
            termNode = opNode;
        }

        return termNode;
    }


    private Node parseFactor() {
        // The current token should now be an identifier or a number or (

        if (currentToken.type == IDENTIFIER) return parseVariable();
        else if (currentToken.type == INTEGER) return parseIntegerConstant();
        else if (currentToken.type == REAL) return parseRealConstant();

        else if (currentToken.type == LPAREN) {
            currentToken = scanner.nextToken();  // consume (
            Node exprNode = parseExpression();

            if (currentToken.type == RPAREN) {
                currentToken = scanner.nextToken();  // consume )
            } else syntaxError("Expecting )");

            return exprNode;
        } else if (currentToken.type == Token.TokenType.NOT) {
            Node notNode = new Node(Node.NodeType.NOT);
            currentToken = scanner.nextToken();  // consume NOT

            notNode.adopt(parseFactor());
            return notNode;
        } else syntaxError("Unexpected token");
        return null;
    }

    private Node parseVariable() {
        // The current token should now be an identifier.

        // Has the variable been "declared"?
        String variableName = currentToken.text;
        SymTableEntry variableId = symTable.lookup(variableName.toLowerCase());
        if (variableId == null) semanticError("Undeclared identifier");

        Node node = new Node(VARIABLE);
        node.text = variableName;
        node.entry = variableId;

        currentToken = scanner.nextToken();  // consume the identifier        
        return node;
    }

    private Node parseIntegerConstant() {
        // The current token should now be a number.

        Node integerNode = new Node(INTEGER_CONSTANT);
        integerNode.value = currentToken.value;

        currentToken = scanner.nextToken();  // consume the number        
        return integerNode;
    }

    private Node parseRealConstant() {
        // The current token should now be a number.

        Node realNode = new Node(REAL_CONSTANT);
        realNode.value = currentToken.value;

        currentToken = scanner.nextToken();  // consume the number        
        return realNode;
    }

    private Node parseStringConstant() {
        // The current token should now be CHARACTER or STRING.

        Node stringNode = new Node(STRING_CONSTANT);
        stringNode.value = currentToken.value;

        currentToken = scanner.nextToken();  // consume the string        
        return stringNode;
    }

    private void syntaxError(String message) {
        System.out.println("SYNTAX ERROR at line " + lineNumber + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;

        // Recover by skipping the rest of the statement.
        // Skip to a statement follower token.
        while (!statementFollowers.contains(currentToken.type)) {
            currentToken = scanner.nextToken();
        }
    }

    private void semanticError(String message) {
        System.out.println("SEMANTIC ERROR at line " + lineNumber + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;
    }
}
