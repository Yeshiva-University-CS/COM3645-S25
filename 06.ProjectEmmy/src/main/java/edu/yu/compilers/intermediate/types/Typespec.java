package edu.yu.compilers.intermediate.types;

import edu.yu.compilers.intermediate.symbols.SymTableEntry;

public class Typespec {
    private final Form form; // type form
    private SymTableEntry identifier; // type identifier

    static public Typespec newFunctionType(SymTableEntry entry) {
        Typespec type = new Typespec(Form.FUNCTION);
        type.setIdentifier(entry);
        return type;
    }
    public Typespec(Form form) {
        this.form = form;
    }

    public Form getForm() {
        return form;
    }

    public SymTableEntry getIdentifier() {
        return identifier;
    }

    public void setIdentifier(SymTableEntry identifier) {
        this.identifier = identifier;
    }

    public enum Form {
        SCALAR, FUNCTION, DYNAMIC;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return form.toString() + (identifier != null ? " " + identifier.getName() : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Typespec type)) {
            return false;
        } else {
            return form == type.form && (identifier == null ? type.identifier == null : identifier.equals(type.identifier));
        }
    }

}
