package edu.yu.compilers.backend.irgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.yu.compilers.intermediate.ast.BaseASTVisitor;
import edu.yu.compilers.intermediate.ast.Expr;
import edu.yu.compilers.intermediate.ast.Expr.Assign;
import edu.yu.compilers.intermediate.ast.Expr.Binary;
import edu.yu.compilers.intermediate.ast.Expr.Call;
import edu.yu.compilers.intermediate.ast.Expr.FuncId;
import edu.yu.compilers.intermediate.ast.Expr.Literal;
import edu.yu.compilers.intermediate.ast.Expr.Logical;
import edu.yu.compilers.intermediate.ast.Expr.Unary;
import edu.yu.compilers.intermediate.ast.Expr.VarId;
import edu.yu.compilers.intermediate.ast.Program;
import edu.yu.compilers.intermediate.ast.Stmt;
import edu.yu.compilers.intermediate.ast.Stmt.Block;
import edu.yu.compilers.intermediate.ast.Stmt.Expression;
import edu.yu.compilers.intermediate.ast.Stmt.If;
import edu.yu.compilers.intermediate.ast.Stmt.Loop;
import edu.yu.compilers.intermediate.ast.Stmt.Print;
import edu.yu.compilers.intermediate.ast.Stmt.Return;
import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.Operand.Constant;
import edu.yu.compilers.intermediate.ir.Operand.Function;
import edu.yu.compilers.intermediate.ir.Operand.Label;
import edu.yu.compilers.intermediate.ir.Operand.OperandType;
import edu.yu.compilers.intermediate.ir.Operand.Temporary;
import edu.yu.compilers.intermediate.ir.Operand.Variable;
import edu.yu.compilers.intermediate.ir.Operator;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.TupleIR.VariableInfo;
import edu.yu.compilers.intermediate.symbols.SymTable;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.types.Typespec;

/**
 * TupleIRBuilder translates an AST to TupleIR by visiting AST nodes
 * and generating tuples using the TupleFactory.
 */
public class TupleIRBuilder extends BaseASTVisitor<Object> {

    public static TupleIR build(Program program) {
        TupleIRBuilder builder = new TupleIRBuilder();
        builder.visitProgram(program);
        return builder.getTupleIR();
    }

    private TupleIR ir;
    private Map<Loop, Label> loopStartLabels;
    private Map<Loop, Label> loopEndLabels;
    private int labelCounter;

    private Map<SymTableEntry, Stmt.Block> functionDefinitions = new HashMap<>();
    private Set<SymTableEntry> processedFunctions = new HashSet<>();

    private TupleIRBuilder() {
        ir = new TupleIR();
        loopStartLabels = new HashMap<>();
        loopEndLabels = new HashMap<>();
        labelCounter = 0;
    }

    /**
     * Get the generated TupleIR.
     * 
     * @return the TupleIR
     */
    public TupleIR getTupleIR() {
        return ir;
    }

    /**
     * Create a new unique label.
     * 
     * @param prefix the label prefix
     * @return the newly created label
     */
    private Label createLabel(String prefix) {
        return new Label(prefix + "_" + labelCounter++);
    }

    private List<Label> createLabelSet(String... prefixes) {
        int counter = labelCounter++;
        List<Label> labels = new ArrayList<>();
        for (String prefix : prefixes) {
            Label label = new Label(prefix + "_" + counter);
            labels.add(label);
        }
        return labels;
    }

    /**
     * Convert a Typespec to an OperandType for the IR.
     * 
     * @param typespec the Typespec from the AST expression
     * @return the corresponding OperandType for IR
     */
    private OperandType convertTypespec(Typespec typespec) {
        if (typespec == null)
            return OperandType.NONE;

        switch (typespec.getForm()) {
            case SCALAR:
                // Get the identifier name from the Typespec
                SymTableEntry identifier = typespec.getIdentifier();
                if (identifier != null) {
                    String typeName = identifier.getName().toLowerCase();
                    switch (typeName) {
                        case "integer":
                            return OperandType.INTEGER;
                        case "real":
                            return OperandType.FLOAT;
                        case "boolean":
                            return OperandType.BOOLEAN;
                        case "string":
                            return OperandType.STRING;
                        default:
                            return OperandType.NONE;
                    }
                }
                return OperandType.NONE;

            case FUNCTION:
            case DYNAMIC:
            default:
                return OperandType.NONE;
        }
    }

    /**
     * Get the type from an expression.
     * 
     * @param expr the AST expression
     * @return the corresponding IR OperandType
     */
    private OperandType getTypeFromExpr(Expr expr) {
        if (expr == null)
            return OperandType.NONE;

        return convertTypespec(expr.getType());
    }

    /*
     * ********************
     * Visit Methods
     * ********************
     */

    @Override
    public Void visitProgram(Program program) {
        ir.addTuple(TupleFactory.createProgram());

        SymTable symTable = program.getEntry().getRoutineSymTable();

        processVariables(symTable, new ArrayList<>());
        processStatements(program.getStatements());

        while (!functionDefinitions.isEmpty()) {
            Map.Entry<SymTableEntry, Stmt.Block> definition = functionDefinitions.entrySet().iterator().next();

            SymTableEntry funcEntry = definition.getKey();
            Stmt.Block funcBlock = definition.getValue();

            ir.enterFunctionScope(funcEntry.getName());

            Label funcLabel = new Label(funcEntry.getName() + "_func");
            ir.addTuple(TupleFactory.createFunction(funcLabel));

            SymTable functionSymTable = funcEntry.getRoutineSymTable();

            processVariables(functionSymTable, funcEntry.getRoutineParameters());
            processStatements(funcBlock.getStatements());

            ir.addTuple(TupleFactory.createEndFunction(funcLabel));

            functionDefinitions.remove(definition.getKey());
            processedFunctions.add(definition.getKey());

            ir.exitFunctionScope();
        }

        ir.addTuple(TupleFactory.createEndProgram());

        return null;
    }

    private void processVariables(SymTable symTable, List<SymTableEntry> parameters) {
        List<SymTableEntry> entries = symTable.sortedEntries().stream()
                .filter(entry -> entry.getKind() == SymTableEntry.Kind.VARIABLE || entry.getKind() == SymTableEntry.Kind.VALUE_PARAMETER)
                .toList();

        for (SymTableEntry entry : entries) {
            OperandType type = convertTypespec(entry.getType());
            VariableInfo varInfo = new VariableInfo(entry, type);
            if (parameters.contains(entry)) {
                varInfo.setParamIndex(parameters.indexOf(entry));
            }
            ir.addVariable(varInfo);
        }
    }

    private void processStatements(List<Stmt> statements) {
        for (Stmt statement : statements) {
            visit(statement);
        }
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        // Visit all statements in the block
        for (Stmt s : stmt.getStatements()) {
            visit(s);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        // Visit the expression
        visit(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        // Generate labels for then and else branches
        Label endIfLabel = createLabel("endif");
        Label elseLabel = stmt.getElseBranch() != null ? createLabel("else") : endIfLabel;

        // Visit condition and get its operand
        Operand condition = (Operand) visit(stmt.getCondition());

        // Add IF tuple for condition
        ir.addTuple(TupleFactory.createIf(condition, elseLabel));

        // Visit then branch
        visit(stmt.getThenBranch());

        if (stmt.getElseBranch() != null) {
            // Add GOTO to skip else branch if then branch completes
            ir.addTuple(TupleFactory.createGoto(endIfLabel));

            // Add LABEL for else branch
            ir.addTuple(TupleFactory.createLabel(elseLabel));

            // Visit else branch
            visit(stmt.getElseBranch());
        }

        // Add LABEL for end of if statement
        ir.addTuple(TupleFactory.createLabel(endIfLabel));

        return null;
    }

    @Override
    public Void visitLoopStmt(Loop stmt) {
        // Generate labels for loop start and end
        List<Label> labelSet = createLabelSet("loop_start", "loop_end");
        Label startLabel = labelSet.get(0);
        Label endLabel = labelSet.get(1);

        // Store labels for potential nested break statements
        loopStartLabels.put(stmt, startLabel);
        loopEndLabels.put(stmt, endLabel);

        // Visit initializer if it exists
        if (stmt.getInitializer() != null) {
            visit(stmt.getInitializer());
        }

        // Add LABEL for loop start
        ir.addTuple(TupleFactory.createLabel(startLabel));

        // Visit all statements in the loop body
        for (Stmt s : stmt.getBody()) {
            visit(s);
        }

        // Jump back to the start of the loop
        ir.addTuple(TupleFactory.createGoto(startLabel));

        // Add LABEL for end of loop
        ir.addTuple(TupleFactory.createLabel(endLabel));

        // Clean up the loop labels map
        loopStartLabels.remove(stmt);
        loopEndLabels.remove(stmt);

        return null;
    }

    @Override
    public Void visitLoopBreakTestStmt(Loop.BreakTest stmt) {
        // Get the current loop from the context
        Loop currentLoop = null;
        for (Loop loop : loopEndLabels.keySet()) {
            currentLoop = loop; // Get the most recent loop
            break;
        }

        if (currentLoop == null) {
            throw new RuntimeException("Break test outside of loop context");
        }

        // Get the condition
        Operand condition = (Operand) visit(stmt.getCondition());

        // If condition is true, jump to the end of the loop
        Label endLabel = loopEndLabels.get(currentLoop);
        ir.addTuple(TupleFactory.createIf(condition, endLabel));

        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        // Visit the expression to print
        Operand expr = (Operand) visit(stmt.getExpression());

        // Add PRINT tuple
        ir.addTuple(TupleFactory.createPrint(expr));

        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        // Visit the return value if it exists
        Operand value = stmt.getValue() != null ? (Operand) visit(stmt.getValue()) : null;

        // Add RETURN tuple
        ir.addTuple(TupleFactory.createReturn(value));

        return null;
    }

    @Override
    public Operand visitAssignExpr(Assign expr) {
        // Get the symbol table entry and visit the value
        SymTableEntry entry = expr.getEntry();
        Operand value = (Operand) visit(expr.getValue());

        // Add ASSIGN tuple
        Variable var = new Variable(entry);
        ir.addTuple(TupleFactory.createAssign(var, value));

        // Set type from expression's type
        OperandType type = getTypeFromExpr(expr);
        var.setType(type);

        return var;
    }

    @Override
    public Operand visitBinaryExpr(Binary expr) {
        // Visit left and right operands
        Operand left = (Operand) visit(expr.getLeft());
        Operand right = (Operand) visit(expr.getRight());

        // Map the AST operator to the IR operator
        Operator op;
        switch (expr.getOperator()) {
            case ADD:
                op = Operator.ADD;
                break;
            case SUB:
                op = Operator.SUB;
                break;
            case MUL:
                op = Operator.MUL;
                break;
            case DIV:
                op = Operator.DIV;
                break;
            case EQ:
                op = Operator.EQ;
                break;
            case NE:
                op = Operator.NEQ;
                break;
            case LT:
                op = Operator.LT;
                break;
            case LE:
                op = Operator.LTE;
                break;
            case GT:
                op = Operator.GT;
                break;
            case GE:
                op = Operator.GTE;
                break;
            default:
                throw new IllegalArgumentException("Unknown binary operator: " + expr.getOperator());
        }

        // Add binary operation tuple
        Temporary result = ir.newTemp();
        result.setType(getTypeFromExpr(expr));
        ir.addTuple(TupleFactory.createBinaryOp(op, result, left, right));

        return result;
    }

    @Override
    public Operand visitCallExpr(Call expr) {
        FuncId functionId = expr.getCallee();

        Operand func = (Operand) visit(functionId);

        List<Operand> args = new ArrayList<>();
        for (Expr arg : expr.getArguments()) {
            args.add((Operand) visit(arg));
        }

        // Create CALL tuple
        Temporary result = ir.newTemp();
        result.setType(getTypeFromExpr(expr));
        ir.addTuple(TupleFactory.createCall(result, func, args));

        return result;
    }

    @Override
    public Operand visitFuncIdExpr(FuncId expr) {
        // Get the symbol table entry
        SymTableEntry entry = expr.getEntry();
        Function func = new Function(entry);
        func.setType(getTypeFromExpr(expr));

        if (!processedFunctions.contains(entry)) {
            functionDefinitions.put(entry, expr.getCodeBlock());
        }

        return func;
    }

    @Override
    public Operand visitLiteralExpr(Literal expr) {
        // Create a constant operand for the literal

        Object value = expr.getValue();
        Constant constant = new Constant(value);

        if (value instanceof String) {
            ir.registerStringConstant((String) value);
        }

        return constant;
    }

    @Override
    public Operand visitLogicalExpr(Logical expr) {
        // Visit left and right operands
        Operand left = (Operand) visit(expr.getLeft());
        Operand right = (Operand) visit(expr.getRight());

        // Map the AST operator to the IR operator
        Operator op;
        switch (expr.getOperator()) {
            case AND:
                op = Operator.AND;
                break;
            case OR:
                op = Operator.OR;
                break;
            default:
                throw new IllegalArgumentException("Unknown logical operator: " + expr.getOperator());
        }

        // Add logical operation tuple
        Temporary result = ir.newTemp();
        result.setType(getTypeFromExpr(expr));
        ir.addTuple(TupleFactory.createBinaryOp(op, result, left, right));

        return result;
    }

    @Override
    public Operand visitUnaryExpr(Unary expr) {
        // Visit the operand
        Operand operand = (Operand) visit(expr.getOperand());

        // Map the AST operator to the IR operator
        Operator op;
        switch (expr.getOperator()) {
            case SUB:
                op = Operator.SUB;
                break; // Note: Using SUB for unary minus
            case NOT:
                op = Operator.NOT;
                break;
            default:
                throw new IllegalArgumentException("Unknown unary operator: " + expr.getOperator());
        }

        // Add unary operation tuple
        Temporary result = ir.newTemp();
        result.setType(getTypeFromExpr(expr));
        ir.addTuple(TupleFactory.createUnaryOp(op, result, operand));

        return result;
    }

    @Override
    public Operand visitVarIdExpr(VarId expr) {
        // Get the symbol table entry
        SymTableEntry entry = expr.getEntry();

        Variable var = new Variable(entry);
        OperandType type = getTypeFromExpr(expr);
        var.setType(type);

        return var;
    }
}
