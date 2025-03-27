package edu.yu.compilers;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import antlr4.EmmyLexer;
import antlr4.EmmyParser;
import edu.yu.compilers.frontend.semantic.Semantics;

class OfficialTest {

    protected final static Logger logger = LogManager.getLogger(OfficialTest.class);

    static {
        Configurator.setLevel("edu.yu.compilers", Level.INFO);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
    
    /**
     * Parses Emmy code and runs semantic analysis
     * @param code The Emmy code to parse
     * @return Semantics object after parsing
     */
    protected Semantics analyzeCode(String code) {
        logger.info("Analyzing code: {}", code);
        
        // Create the lexer and parser
        EmmyLexer lexer = new EmmyLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EmmyParser parser = new EmmyParser(tokens);

        // Parse the input
        ParseTree tree = parser.program();

        // Create and run the semantics visitor
        Semantics semantics = new Semantics();
        semantics.visit(tree);
        
        logger.info("Semantic analysis complete. Error count: {}", semantics.getErrorCount());

        return semantics;
    }
}