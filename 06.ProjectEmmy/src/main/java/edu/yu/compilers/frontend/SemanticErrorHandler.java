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

    public int getCount() {
        return count;
    }


    public void flag(Code code, int lineNumber, String context, String message) {
        if (count == 0) {
            System.out.println("\n===== SEMANTIC ERRORS =====\n");
            System.out.printf("%-4s %-25s %-25s %s\n", "Line", "Type", "Found near", "Message");
            System.out.printf("%-4s %-25s %-25s %s\n", "----", "----", "----------", "-------");
        }

        count++;

        System.out.printf(" %03d %-25s %-25s %s\n",
                lineNumber,
                code.name(),
                context,
                message == null ? code.message : message
        );
    }

    public void flag(Code code, int lineNumber, String message) {
        flag(code, lineNumber, "", message);
    }

    public void flag(Code code, ParserRuleContext ctx, String message) {
        flag(code, ctx.getStart().getLine(), ctx.getText(), message);
    }

}
