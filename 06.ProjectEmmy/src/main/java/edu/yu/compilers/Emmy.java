package edu.yu.compilers;

import antlr4.EmmyLexer;
import antlr4.EmmyParser;
import edu.yu.compilers.frontend.Semantics;
import edu.yu.compilers.frontend.SyntaxErrorHandler;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

public class Emmy {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("USAGE: pascalCC {-type | ast | execute | -convert | -compile} sourceFileName");
            return;
        }

        enum Mode {
            TYPE, AST, EXECUTE, CONVERT, COMPILE
        }

        String option = args[0];
        String sourceFileName = args[1];

        Mode mode = switch (option.toLowerCase()) {
            case "-type" -> Mode.TYPE;
            case "-ast" -> Mode.AST;
            case "-execute" -> Mode.EXECUTE;
            case "-convert" -> Mode.CONVERT;
            case "-compile" -> Mode.COMPILE;
            default -> {
                System.out.println("ERROR: Invalid option.");
                System.out.println("USAGE: pascalCC {-type | -ast | -execute | -convert | -compile} sourceFileName");
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

            if (!mode.equals(Mode.TYPE)) {
                return;
            }

            switch (mode) {
                case TYPE -> {
                    // Print the symbol table stack.
                    pass2.printSymbolTableStack();
                }
                case AST -> {
                    // Pass 2B: Build the AST IR
                    System.out.print("\nPASS 2B Build AST IR: ");
                    System.out.print("\nTBD:\n\n");
                }
                case EXECUTE -> {
                    // Pass 3: Execute the Emmy program.
                    System.out.print("\nPASS 3 Execute: ");
                    System.out.print("\nTBD:\n\n");
                }
                case CONVERT -> {
                    // Pass 3: Convert from Emmy to Java.
                    System.out.print("\nPASS 3 Convert: ");
                    System.out.print("\nTBD:\n\n");
                }
                case COMPILE -> {
                    // Pass 3: Compile the Emmy program.
                    System.out.print("\nPASS 3 Compile: ");
                    System.out.print("\nTBD:\n\n");
                }
            }
        }
    }
}