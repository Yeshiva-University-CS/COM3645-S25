package edu.yu.compilers.intermediate.symbols;

import edu.yu.compilers.intermediate.symbols.SymTableEntry.Kind;
import edu.yu.compilers.intermediate.types.Typespec;
import edu.yu.compilers.intermediate.types.Typespec.Form;

import java.util.List;

public class CrossReferencer {
    private static final int NAME_WIDTH = 16;

    private static final String NAME_FORMAT = "%-" + NAME_WIDTH + "s";
    private static final String NUMBERS_LABEL = " Line numbers    ";
    private static final String NUMBERS_UNDERLINE = " ------------    ";
    private static final String NUMBER_FORMAT = " %03d";

    private static final int LABEL_WIDTH = NUMBERS_LABEL.length();
    private static final int INDENT_WIDTH = NAME_WIDTH + LABEL_WIDTH;

    private static final StringBuilder INDENT = new StringBuilder(INDENT_WIDTH);
    static {
        INDENT.append(" ".repeat(INDENT_WIDTH));
    }

    /**
     * Print the cross-reference table.
     *
     * @param symTableStack the symbol table stack.
     */
    public void print(SymTableStack symTableStack) {
        System.out.println("\n===== CROSS-REFERENCE TABLE =====");

        SymTableEntry programId = symTableStack.getProgramId();
        printRoutine(programId);
    }

    /**
     * Print a cross-reference table for a routine.
     *
     * @param routineId the routine identifier's symbol table entry.
     */
    private void printRoutine(SymTableEntry routineId) {
        SymTableEntry.Kind kind = routineId.getKind();
        System.out.println("\n*** " + kind.toString().toUpperCase() + " " + routineId.getName() + " ***");
        printColumnHeadings();

        // Print the entries in the routine's symbol table.
        SymTable symTable = routineId.getRoutineSymTable();
        printSymTable(symTable);

        // Print any procedures and functions defined in the routine.
        List<SymTableEntry> subroutineIds = routineId.getSubroutines();
        if (subroutineIds != null) {
            for (SymTableEntry rtnId : subroutineIds)
                printRoutine(rtnId);
        }
    }

    /**
     * Print column headings.
     */
    private void printColumnHeadings() {
        System.out.println();
        System.out.println(String.format(NAME_FORMAT, "Identifier") + NUMBERS_LABEL + "Type specification");
        System.out.println(String.format(NAME_FORMAT, "----------") + NUMBERS_UNDERLINE + "------------------");
    }

    /**
     * Print the entries in a symbol table.
     *
     * @param symTable the symbol table.
     */
    private void printSymTable(SymTable symTable) {
        List<SymTableEntry> sorted = symTable.sortedEntries();

        // Loop over the sorted list of table entries
        // to print each entry of this symbol table.
        for (SymTableEntry entry : sorted) {
            List<Integer> lineNumbers = entry.getLineNumbers();

            // For each entry, print the identifier name
            // followed by the line numbers.
            System.out.printf(NAME_FORMAT, entry.getName());
            if (lineNumbers != null) {
                for (Integer lineNumber : lineNumbers) {
                    System.out.printf(NUMBER_FORMAT, lineNumber);
                }
            }

            // Print the symbol table entry.
            System.out.println();
            printEntry(entry);
        }
    }

    /**
     * Print a symbol table entry.
     *
     * @param entry the symbol table entry.
     */
    private void printEntry(SymTableEntry entry) {
        Kind kind = entry.getKind();
        int nestingLevel = entry.getSymTable().getNestingLevel();
        System.out.println(INDENT + "Kind: " + kind.toString().replace("_", " "));
        System.out.println(INDENT + "Scope nesting level: " + nestingLevel);

        // Print the type specification.
        Typespec type = entry.getType();
        printType(type);

        switch (kind) {
            case CONSTANT -> {
                Object value = entry.getValue();
                System.out.println(INDENT + "Value: " + toString(value, type));

                // Print the type details only if the type is unnamed.
                if (type.getIdentifier() == null) {
                    printTypeDetail(type);
                }

            }
            case TYPE -> {
                // Print the type details only when the type is first defined.
                if (entry == type.getIdentifier()) {
                    printTypeDetail(type);
                }

            }
            case VARIABLE -> {
                // Print the type details only if the type is unnamed.
                if (type.getIdentifier() == null) {
                    printTypeDetail(type);
                }

            }
            case FUNCTION -> {
                List<SymTableEntry> parms = entry.getRoutineSymTable().sortedEntries();
                System.out.println(INDENT + "Routine Sym Table");
                printSymTable(entry.getRoutineSymTable());

                // Print the formal parameters.
                if (!parms.isEmpty()) {
                    System.out.println(INDENT + "Parameters");
                    for (SymTableEntry parmId : parms) {
                        // printEntry(parmId);
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Print a type specification.
     *
     * @param type the type specification.
     */
    private void printType(Typespec type) {
        if (type != null) {
            Form form = type.getForm();
            SymTableEntry typeId = type.getIdentifier();
            String typeName = typeId != null ? typeId.getName() : "<unnamed>";

            System.out.println(INDENT + "Type form: " + form + ", Type id: " + typeName);
        }
    }

    /**
     * Print the details of a type specification.
     *
     * @param type the type specification.
     */
    private void printTypeDetail(Typespec type) {
        Form form = type.getForm();
    }

    /**
     * Convert a value to a string.
     *
     * @param value the value.
     * @param type  the value's datatype.
     * @return the string.
     */
    private String toString(Object value, Typespec type) {
        return type == Predefined.stringType ? "'" + value + "'" : value.toString();
    }
}
