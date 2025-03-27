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
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.types.TypeChecker;
import edu.yu.compilers.intermediate.types.Typespec;

import java.util.List;

/**
 * AST visitor that prints the tree structure in YAML format.
 * Functions are printed inline at the point where they are called,
 * rather than in a separate "functions" section.
 */
public class ASTYamlPrinter extends BaseASTVisitor<String> {
    /**
     * Print a program in YAML format.
     */
    public static void print(Program program) {
        ASTYamlPrinter printer = new ASTYamlPrinter();
        System.out.println(printer.generateYAML(program));
    }

    private static final String INDENT = "  ";
    
    /**
     * Generate a YAML string representation of the program.
     * 
     * @param program the program to convert to YAML
     * @return YAML string representation
     */
    public String generateYAML(Program program) {
        StringBuilder sb = new StringBuilder();
        
        // Program node type
        sb.append("nodeType: Program\n");
        
        // Program statements (main body)
        sb.append("statements:\n");
        
        List<Stmt> statements = program.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            String stmtYaml = statements.get(i).accept(this);
            sb.append(INDENT).append("- ").append(stmtYaml.replace("\n", "\n" + INDENT + "  "));
            
            if (i < statements.size() - 1) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String visitBlockStmt(Block stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: BlockStmt\n");
        
        sb.append("statements:\n");
        
        List<Stmt> statements = stmt.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            String stmtYaml = statements.get(i).accept(this);
            sb.append(INDENT).append("- ").append(stmtYaml.replace("\n", "\n" + INDENT + "  "));
            
            if (i < statements.size() - 1) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String visitExpressionStmt(Expression stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: ExpressionStmt\n");
        sb.append("expression: |\n");
        
        String exprYaml = stmt.getExpression().accept(this);
        String[] lines = exprYaml.split("\n");
        for (String line : lines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitIfStmt(If stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: IfStmt\n");
        
        sb.append("condition: |\n");
        String conditionYaml = stmt.getCondition().accept(this);
        String[] condLines = conditionYaml.split("\n");
        for (String line : condLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        sb.append("thenBranch: |\n");
        String thenYaml = stmt.getThenBranch().accept(this);
        String[] thenLines = thenYaml.split("\n");
        for (String line : thenLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        if (stmt.getElseBranch() != null) {
            sb.append("elseBranch: |\n");
            String elseYaml = stmt.getElseBranch().accept(this);
            String[] elseLines = elseYaml.split("\n");
            for (String line : elseLines) {
                sb.append(INDENT).append(line).append("\n");
            }
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitLoopStmt(Loop stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: LoopStmt\n");
        
        if (stmt.getInitializer() != null) {
            sb.append("initializer: |\n");
            String initYaml = stmt.getInitializer().accept(this);
            String[] initLines = initYaml.split("\n");
            for (String line : initLines) {
                sb.append(INDENT).append(line).append("\n");
            }
        }
        
        sb.append("body:\n");
        
        List<Stmt> body = stmt.getBody();
        for (int i = 0; i < body.size(); i++) {
            String stmtYaml = body.get(i).accept(this);
            sb.append(INDENT).append("- ").append(stmtYaml.replace("\n", "\n" + INDENT + "  "));
            
            if (i < body.size() - 1) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String visitLoopBreakTestStmt(BreakTest stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: LoopBreakTestStmt\n");
        
        sb.append("condition: |\n");
        String conditionYaml = stmt.getCondition().accept(this);
        String[] condLines = conditionYaml.split("\n");
        for (String line : condLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: PrintStmt\n");
        
        sb.append("expression: |\n");
        String exprYaml = stmt.getExpression().accept(this);
        String[] exprLines = exprYaml.split("\n");
        for (String line : exprLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitReturnStmt(Return stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: ReturnStmt\n");
        
        if (stmt.getValue() != null) {
            sb.append("value: |\n");
            String valueYaml = stmt.getValue().accept(this);
            String[] valueLines = valueYaml.split("\n");
            for (String line : valueLines) {
                sb.append(INDENT).append(line).append("\n");
            }
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitVarStmt(Var stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: VarStmt\n");
        
        appendSymbolTableInfo(sb, stmt.getEntry());
        
        if (stmt.getInitializer() != null) {
            sb.append("initializer: |\n");
            String initYaml = stmt.getInitializer().accept(this);
            String[] initLines = initYaml.split("\n");
            for (String line : initLines) {
                sb.append(INDENT).append(line).append("\n");
            }
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: AssignExpr\n");
        
        appendSymbolTableInfo(sb, expr.getEntry());
        
        sb.append("value: |\n");
        String valueYaml = expr.getValue().accept(this);
        String[] valueLines = valueYaml.split("\n");
        for (String line : valueLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        // Remove the last newline
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        
        return sb.toString();
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: BinaryExpr\n");
        appendTypeInfo(sb, expr.getType());

        sb.append("operator: \"").append(expr.getOperator()).append("\"\n");
        
        sb.append("left: |\n");
        String leftYaml = expr.getLeft().accept(this);
        String[] leftLines = leftYaml.split("\n");
        for (String line : leftLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        sb.append("right: |\n");
        String rightYaml = expr.getRight().accept(this);
        String[] rightLines = rightYaml.split("\n");
        for (String line : rightLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public String visitCallExpr(Call expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: CallExpr\n");
        appendTypeInfo(sb, expr.getType());

        // Get the callee (function identifier)
        FuncId callee = expr.getCallee();
        sb.append("callee: |\n");
        String calleeYaml = callee.accept(this);
        String[] calleeLines = calleeYaml.split("\n");
        for (String line : calleeLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        // Get arguments list
        sb.append("arguments:\n");
        
        List<Expr> args = expr.getArguments();
        for (int i = 0; i < args.size(); i++) {
            sb.append(INDENT).append("- |\n");
            String argYaml = args.get(i).accept(this);
            String[] argLines = argYaml.split("\n");
            for (String line : argLines) {
                sb.append(INDENT).append(INDENT).append(line).append("\n");
            }
            
            if (i < args.size() - 1) {
                sb.append("\n");
            }
        }
        
        // Get the function body
        Stmt.Block codeBlock = expr.getCodeBlock();
        if (codeBlock != null) {
            sb.append("codeBlock: |\n");
            String codeYaml = codeBlock.accept(this);
            String[] codeLines = codeYaml.split("\n");
            for (String line : codeLines) {
                sb.append(INDENT).append(line).append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String visitFuncIdExpr(FuncId expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: FuncIdExpr\n");
        
        appendSymbolTableInfo(sb, expr.getEntry());
        
        return sb.toString();
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: LiteralExpr\n");
        appendTypeInfo(sb, expr.getType());

        Object value = expr.getValue();
        String valueStr = value != null ? value.toString().replace("\"", "\\\"") : "null";
        sb.append("value: \"").append(valueStr).append("\"\n");
        
        return sb.toString();
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: LogicalExpr\n");
        appendTypeInfo(sb, expr.getType());

        sb.append("operator: \"").append(expr.getOperator()).append("\"\n");
        
        sb.append("left: |\n");
        String leftYaml = expr.getLeft().accept(this);
        String[] leftLines = leftYaml.split("\n");
        for (String line : leftLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        sb.append("right: |\n");
        String rightYaml = expr.getRight().accept(this);
        String[] rightLines = rightYaml.split("\n");
        for (String line : rightLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: UnaryExpr\n");
        appendTypeInfo(sb, expr.getType());

        sb.append("operator: \"").append(expr.getOperator()).append("\"\n");
        
        sb.append("operand: |\n");
        String operandYaml = expr.getOperand().accept(this);
        String[] operandLines = operandYaml.split("\n");
        for (String line : operandLines) {
            sb.append(INDENT).append(line).append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public String visitVarIdExpr(VarId expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("nodeType: VarIdExpr\n");
        
        appendSymbolTableInfo(sb, expr.getEntry());
        
        return sb.toString();
    }

    /**
     * Append symbol table information to the string builder.
     */
    private void appendSymbolTableInfo(StringBuilder sb, SymTableEntry entry) {
        if (entry != null) {
            sb.append("name: \"").append(entry.getName()).append("\"\n");
            
            String kind = "";
            if (entry.isVariable()) {
                kind = "VARIABLE";
            } else if (entry.isFunction()) {
                kind = "FUNCTION";
            }
            sb.append("kind: \"").append(kind).append("\"\n");
            
            // Add type information if available
            Typespec type = entry.getType();
            if (type != null) {
                String typeName = type.getIdentifier() != null ? type.getIdentifier().getName() : type.toString();
                sb.append("type: \"").append(typeName).append("\"\n");
            }
        }
    }

    /**
     * Append type information to the string builder.
     */
    private void appendTypeInfo(StringBuilder sb, Typespec type) {
        if (type != null) {
            String typeName = type.getIdentifier() != null && !TypeChecker.isFunction(type)
                    ? type.getIdentifier().getName() : type.toString();
            sb.append("type: \"").append(typeName).append("\"\n");
        }
    }
}
