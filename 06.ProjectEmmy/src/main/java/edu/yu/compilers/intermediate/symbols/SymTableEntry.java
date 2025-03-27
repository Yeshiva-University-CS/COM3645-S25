/**
 * <h1>SymTableEntryImpl</h1>
 * <p>An implementation of a symbol table entry.</p>
 * <p>Adapted from:</p>
 * <p>Copyright (c) 2020 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */

package edu.yu.compilers.intermediate.symbols;

import java.util.ArrayList;
import java.util.List;

import edu.yu.compilers.intermediate.types.Typespec;

public class SymTableEntry {
    private final String name; // entry name
    private final SymTable symTable; // parent symbol table
    private final List<Integer> lineNumbers; // source line numbers
    private Kind kind; // what kind of identifier
    private Typespec typespec; // type specification
    private EntryInfo info; // entry information

    /**
     * Constructor.
     *
     * @param name     the name of the entry.
     * @param kind     the kind of entry.
     * @param symTable the symbol table that contains this entry.
     */
    public SymTableEntry(String name, Kind kind, SymTable symTable) {
        this.name = name;
        this.kind = kind;
        this.symTable = symTable;
        this.lineNumbers = new ArrayList<>();

        // Initialize the appropriate entry information.
        switch (kind) {
            case CONSTANT, VARIABLE, VALUE_PARAMETER -> {
                this.typespec = Predefined.noneType;
                info = new ValueInfo();
            }
            case FUNCTION, PROGRAM -> {
                this.typespec = Typespec.newFunctionType(this);
                info = new RoutineInfo();
                ((RoutineInfo) info).parameters = new ArrayList<>();
                ((RoutineInfo) info).subroutines = new ArrayList<>();
                ((RoutineInfo) info).returnType = Predefined.noneType;
            }
            default -> {
            }
        }
    }

    /**
     * Get the name of the entry.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the kind of entry.
     *
     * @return the kind.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Returns true if the Kind of the entry
     * is one of the Constant kinds
     *
     * @return true if yes
     */
    public boolean isContant() {
        return kind == Kind.CONSTANT;
    }

    /**
     * Returns true if the Kind of the entry
     * is the Function
     *
     * @return true if yes
     */
    public boolean isFunction() {
        return kind == Kind.FUNCTION;
    }

    /**
     * Returns true if the Kind of the entry
     * is a variable
     * @return
     */
    public boolean isVariable() {
        return kind == Kind.VARIABLE;
    }
    
    /**
     * Set the kind of entry.
     *
     * @param kind the kind to set.
     */
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Get the symbol table that contains this entry.
     *
     * @return the symbol table.
     */
    public SymTable getSymTable() {
        return symTable;
    }

    /**
     * Get the type specification of the entry.
     *
     * @return the type specification.
     */
    public Typespec getType() {
        return typespec;
    }

    /**
     * Set the type specification.
     *
     * @param typespec the type specification to set.
     */
    public void setType(Typespec typespec) {
        this.typespec = typespec;
    }

    /**
     * Get the arraylist of source line numbers for the entry.
     *
     * @return the arraylist.
     */
    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    /**
     * Append a source line number to the entry.
     *
     * @param lineNumber the line number to append.
     */
    public void appendLineNumber(int lineNumber) {
        lineNumbers.add(lineNumber);
    }

    /**
     * Get the data value stored with this entry.
     *
     * @return the data value.
     */
    public Object getValue() {
        return ((ValueInfo) info).value;
    }

    /**
     * Set the data value into this entry.
     *
     * @param value the value to set.
     */
    public void setValue(Object value) {
        ((ValueInfo) info).value = value;
    }

    /**
     * Get the routine's symbol table.
     *
     * @return the symbol table.
     */
    public SymTable getRoutineSymTable() {
        return ((RoutineInfo) info).symTable;
    }

    /**
     * Set the routine's symbol table.
     *
     * @param symTable the symbol table to set.
     */
    public void setRoutineSymTable(SymTable symTable) {
        ((RoutineInfo) info).symTable = symTable;
    }

    /**
     * Get the arraylist of symbol table entries of the routine's formal parameters.
     *
     * @return the arraylist.
     */
    public List<SymTableEntry> getRoutineParameters() {
        return ((RoutineInfo) info).parameters;
    }

    /**
     * Set the arraylist symbol table entries of parameters of the routine.
     *
     * @param parameters the arraylist to set.
     */
    public void setRoutineParameters(List<SymTableEntry> parameters) {
        ((RoutineInfo) info).parameters = parameters;
    }

    /**
     * Get the arraylist of symbol table entries of the nested subroutines.
     *
     * @return the arraylist.
     */
    public List<SymTableEntry> getSubroutines() {
        return ((RoutineInfo) info).subroutines;
    }

    /**
     * Append to the arraylist of symbol table entries of the nested subroutines.
     *
     * @param subroutineId the symbol table entry of the subroutine to append.
     */
    public void appendSubroutine(SymTableEntry subroutineId) {
        ((RoutineInfo) info).subroutines.add(subroutineId);
    }

    /**
     * Get the routine's executable code.
     *
     * @return the executable code.
     */
    public Object getExecutable() {
        return ((RoutineInfo) info).executable;
    }

    /**
     * Set the routine's executable code.
     *
     * @param executable the executable code to set.
     */
    public void setExecutable(Object executable) {
        ((RoutineInfo) info).executable = executable;
    }

    /**
     * Get the routine's return type.
     *
     * @return the Typespec of the return value.
     */
    public Typespec getReturnType() {
        return ((RoutineInfo) info).returnType;
    }

    /**
     * Set the routine's return type.
     *
     * @param type of the return value.
     */
    public void setReturnType(Typespec type) {
        ((RoutineInfo) info).returnType = type;
    }

    /**
     * What kind of identifier.
     */
    public enum Kind {
        PROGRAM, CONSTANT, TYPE, VARIABLE, VALUE_PARAMETER, FUNCTION, UNDEFINED;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * Entry information interface.
     */
    private interface EntryInfo {
    }

    /**
     * Value information.
     */
    private static class ValueInfo implements EntryInfo {
        private Object value;
    }

    /**
     * Routine information.
     */
    private static class RoutineInfo implements EntryInfo {
        private SymTable symTable; // routine's symbol table
        private List<SymTableEntry> parameters; // routine's formal parameters
        private List<SymTableEntry> subroutines; // symTable entries of subroutines
        private Object executable; // routine's executable code
        private Typespec returnType; // routine's return type
    }
}
