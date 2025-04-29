package edu.yu.compilers;

import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr4.EmmyLexer;
import antlr4.EmmyParser;
import edu.yu.compilers.backend.compiler.CodeGenerator;
import edu.yu.compilers.backend.compiler.Compiler;
import edu.yu.compilers.backend.compiler.X86_64CodeGenerator;
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
    static boolean compileMode;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            return;
        }

        enum Mode {
            TYPE, AST, IR, EXECUTE, CONVERT, COMPILE
        }

        String option = args[0];
        Mode mode = switch (option.toLowerCase()) {
            case "-type" -> Mode.TYPE;
            case "-ast" -> Mode.AST;
            case "-ir" -> Mode.IR;
            case "-execute" -> Mode.EXECUTE;
            case "-convert" -> Mode.CONVERT;
            case "-compile" -> Mode.COMPILE;
            default -> {
                printUsage();
                yield null;
            }
        };

        if (mode == null) {
            return;
        }

        // Handle compile mode specially as it needs an extra argument
        if (mode == Mode.COMPILE) {
            if (args.length != 3) {
                System.out.println("ERROR: Compile mode requires codegen type and source file.");
                System.out.println("USAGE: Emmy -compile {tac|x86} sourceFileName");
                return;
            }
            String codegenType = args[1].toLowerCase();
            if (!codegenType.equals("tac") && !codegenType.equals("x86")) {
                System.out.println("ERROR: Invalid codegen type. Must be either 'tac' or 'x86'");
                System.out.println("USAGE: Emmy -compile {tac|x86} sourceFileName");
                return;
            }
        } else if (args.length != 2) {
            printUsage();
            return;
        }

        // Get the source file name (it's arg[2] for compile mode, arg[1] for others)
        String sourceFileName = (mode == Mode.COMPILE) ? args[2] : args[1];

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

        compileMode = mode == Mode.COMPILE;

        // Pass 2B: Build the AST
        println("\nPASS 2B Build AST IR:");
        println("---------------------");
        Program program = ASTBuilder.build(tree, pass2.getSymTableStack());
        if (mode.equals(Mode.AST)) {
            ASTYamlPrinter.print(program);
            return;
        }

        // Pass 2C: Build the IR
        println("\nPASS 2C Build IR:");
        println("-----------------");
        TupleIR ir = TupleIRBuilder.build(program);
        println(TupleIRUtils.printIR(ir));

        if (mode.equals(Mode.IR)) {
            return;
        }

        switch (mode) {
            case EXECUTE -> {
                // Pass 3: Execute the Emmy program.
                println("\nPASS 3 Execute: ");
                println("\nTBD:\n\n");
            }
            case CONVERT -> {
                // Pass 3: Convert from Emmy to Java.
                System.out.println("\nPASS 3 Convert: ");
                System.out.print("\nTBD:\n\n");
            }
            case COMPILE -> {
                // Pass 3: Compile the Emmy program.
                CodeGenerator codegen = args[1].equals("tac")
                    ? new TACCodeGenerator(ir) 
                    : new X86_64CodeGenerator(ir);
                Compiler compiler = new Compiler(codegen);
                System.out.println(compiler.compile(ir));
            }
            default -> {
                printUsage();
            }
        }
    }

    private static void printUsage() {
        System.out.println("USAGE: Emmy {-type | -ast | -ir | -execute | -convert} sourceFileName");
        System.out.println("   OR: Emmy -compile {tac|x86} sourceFileName");
    }

    private static void println(String str) {
        if (! compileMode)
            System.out.println(str);
    }
}
