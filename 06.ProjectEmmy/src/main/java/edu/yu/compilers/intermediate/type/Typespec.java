package edu.yu.compilers.intermediate.type;

import edu.yu.compilers.intermediate.symtable.SymTableEntry;

public class Typespec {
    private final Form form; // type form
    private SymTableEntry identifier; // type identifier

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
        SCALAR, UNKNOWN;

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
        } else if (!(obj instanceof Typespec)) {
            return false;
        } else {
            Typespec type = (Typespec) obj;
            return form == type.form && (identifier == null ? type.identifier == null : identifier.equals(type.identifier));
        }
    }

}
