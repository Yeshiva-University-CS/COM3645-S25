package edu.yu.compilers;

import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr4.EmmyLexer;
import antlr4.EmmyParser;
import edu.yu.compilers.backend.compiler.Compiler;
import edu.yu.compilers.backend.compiler.TACCodeGenerator;
import edu.yu.compilers.backend.irgen.TupleIRBuilder;
import edu.yu.compilers.frontend.ast.ASTBuilder;
import edu.yu.compilers.frontend.parser.SyntaxErrorHandler;
import edu.yu.compilers.frontend.semantic.Semantics;
import edu.yu.compilers.intermediate.ast.ASTYamlPrinter;
import edu.yu.compilers.intermediate.ast.Program;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.TupleIRUtils;

public class Emmy {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("USAGE: Emmy {-type | -ast | -ir | -execute | -convert | -compile} sourceFileName");
            return;
        }

        enum Mode {
            TYPE, AST, IR, EXECUTE, CONVERT, COMPILE
        }

        String option = args[0];
        String sourceFileName = args[1];

        Mode mode = switch (option.toLowerCase()) {
            case "-type" -> Mode.TYPE;
            case "-ast" -> Mode.AST;
            case "-ir" -> Mode.IR;
            case "-execute" -> Mode.EXECUTE;
            case "-convert" -> Mode.CONVERT;
            case "-compile" -> Mode.COMPILE;
            default -> {
                System.out.println("ERROR: Invalid option.");
                System.out.println("USAGE: Emmy {-type | -ast | -ir | -execute | -convert | -compile} sourceFileName");
                yield null;
            }
        };

        if (mode == null) {
            return;
        }

        // Create the input stream.
        InputStream source = new FileInputStream(sourceFileName);

        // Create the character stream from the input stream.
        CharStream cs = CharStreams.fromStream(source);

        // Custom syntax error handler.
        SyntaxErrorHandler syntaxErrorHandler = new SyntaxErrorHandler();

        // Create a lexer which scans the character stream
        // to create a token stream.
        EmmyLexer lexer = new EmmyLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorHandler);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create a parser which parses the token stream.
        EmmyParser parser = new EmmyParser(tokens);

        // Pass 1: Check syntax and create the parse tree.
        parser.removeErrorListeners();
        parser.addErrorListener(syntaxErrorHandler);
        ParseTree tree = parser.program();

        int errorCount = syntaxErrorHandler.getCount();
        if (errorCount > 0) {
            System.out.printf("\nThere were %d syntax errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
            return;
        }

        // Pass 2: Semantic operations.
        var pass2 = new Semantics();
        pass2.visit(tree);

        errorCount = pass2.getErrorCount();
        if (errorCount > 0) {
            System.out.printf("\nThere were %d semantic errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
        }

        if (mode.equals(Mode.TYPE)) {
            pass2.printSymbolTableStack();
            return;
        } else if (errorCount > 0) {
            return;
        }

        // Pass 2B: Build the AST
        System.out.println("\nPASS 2B Build AST IR:");
        System.out.println("---------------------");
        Program program = ASTBuilder.build(tree);
        ASTYamlPrinter.print(program);

        if (mode.equals(Mode.AST)) {
            return;
        }

        // Pass 2C: Build the IR
        System.out.println("\nPASS 2C Build IR:");
        System.out.println("-----------------");
        TupleIR ir = TupleIRBuilder.build(program);
        System.out.print(TupleIRUtils.printIR(ir));

        if (mode.equals(Mode.IR)) {
            return;
        }

        switch (mode) {
            case EXECUTE -> {
                // Pass 3: Execute the Emmy program.
                System.out.println("\nPASS 3 Execute: ");
                System.out.print("\nTBD:\n\n");
            }
            case CONVERT -> {
                // Pass 3: Convert from Emmy to Java.
                System.out.println("\nPASS 3 Convert: ");
                System.out.print("\nTBD:\n\n");
            }
            case COMPILE -> {
                // Pass 3: Compile the Emmy program.
                System.out.println("\nPASS 3 Compile:");
                System.out.println("---------------");
                TACCodeGenerator codegen = new TACCodeGenerator();
                Compiler compiler = new Compiler(codegen);
                System.out.println(compiler.compile(ir));
            }
            default -> {
                System.out.println("ERROR: Invalid option.");
                System.out.println("USAGE: Emmy {-type | -ast | -ir | -execute | -convert | -compile} sourceFileName");
            }
        }
    }
}
