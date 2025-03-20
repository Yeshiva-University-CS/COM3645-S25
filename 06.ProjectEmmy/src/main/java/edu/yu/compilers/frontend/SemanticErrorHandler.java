package edu.yu.compilers.frontend;

import org.antlr.v4.runtime.ParserRuleContext;

public class SemanticErrorHandler {
    public enum Code {
        ARGUMENT_COUNT_MISMATCH("Argument count mismatch"),
        INVALID_ASSIGNMENT("Invalid assignment statement"), 
        INVALID_FUNCTION("Invalid function"),
        MISPLACED_RETURN("Misplaced return statement"), 
        REDECLARED_IDENTIFIER("Redeclared identifier"),
        TYPE_MISMATCH("Type mismatch"),
        UNDECLARED_IDENTIFIER("Undeclared identifier"), 
        ;

        private final String message;

        Code(String message) {
            this.message = message;
        }
    }

    private int count = 0;

    /**
     * Get the count of semantic errors.
     * 
     * @return the count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Flag a semantic error.
     * 
     * @param code       the error code.
     * @param lineNumber the line number of the offending line.
     * @param text       the text near the error.
     */
    public void flag(Code code, int lineNumber, String text) {
        if (count == 0) {
            System.out.println("\n===== SEMANTIC ERRORS =====\n");
            System.out.printf("%-4s %-40s %s\n", "Line", "Message", "Found near");
            System.out.printf("%-4s %-40s %s\n", "----", "-------", "----------");
        }

        count++;

        System.out.printf("%03d  %-40s \"%s\"\n",
                lineNumber, code.message, text);
    }

    /**
     * Flag a semantic error.
     * 
     * @param code the error code.
     * @param ctx  the context containing the error.
     */
    public void flag(Code code, ParserRuleContext ctx) {
        flag(code, ctx.getStart().getLine(), ctx.getText());
    }
}
