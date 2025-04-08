package edu.yu.compilers.intermediate.ir;

import edu.yu.compilers.intermediate.symbols.SymTableEntry;

/**
 * The Operand interface represents operands in a tuple.
 */
public abstract class Operand {

    abstract public String toString();

    /**
     * The OperandType enum defines the possible operand types.
     */
    public enum OperandType {
        INTEGER, FLOAT, BOOLEAN, STRING, NONE
    }

    /**
     * The Constant Operand class represents constant values in tuples.
     */
    public static class Constant extends Operand {
        private Object value;
        private OperandType type;

        /**
         * Construct a new constant operand.
         * 
         * @param value the constant value
         */
        public Constant(Object value) {
            this.value = value;

            if (value instanceof Integer || value instanceof Long) {
                this.type = OperandType.INTEGER;
            } else if (value instanceof Float || value instanceof Double) {
                this.type = OperandType.FLOAT;
            } else if (value instanceof Boolean) {
                this.type = OperandType.BOOLEAN;
            } else if (value instanceof String) {
                this.type = OperandType.STRING;
            } else if (value == null) {
                this.type = OperandType.NONE;
            } else {
                throw new IllegalArgumentException("Unsupported constant type: " + value.getClass());
            }
        }

        /**
         * Get the constant value.
         * 
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public OperandType getType() {
            return type;
        }

        @Override
        public String toString() {
            if (type == OperandType.STRING) {
                return "\"" + value + "\"";
            } else if (type == OperandType.NONE) {
                return "none";
            } else {
                return value.toString();
            }
        }
    }

    /**
     * The Variable Operand class represents variables in tuples.
     */
    public static class Variable extends Operand {
        private SymTableEntry entry;
        private OperandType type;

        /**
         * Construct a new variable operand.
         * 
         * @param etnry the SymTableEntry
         */
        public Variable(SymTableEntry entry) {
            this.entry = entry;
            this.type = OperandType.NONE;
        }

        /**
         * Get the variable name.
         *
         * @return the name
         */
        public String getName() {
            return entry.getName();
        }

        /**
         * Get the variable entry.
         * 
         * @return the entry
         */
        public SymTableEntry getEntry() {
            return entry;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public OperandType getType() {
            return type;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public void setType(OperandType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * The Variable Operand class represents variables in tuples.
     */
    public static class Function extends Operand {
        private SymTableEntry entry;
        private OperandType type;

        /**
         * Construct a new variable operand.
         * 
         * @param etnry the SymTableEntry
         */
        public Function(SymTableEntry entry) {
            this.entry = entry;
            this.type = OperandType.NONE;
        }

        /**
         * Get the variable name.
         * 
         * @return
         */
        public String getName() {
            return entry.getName();
        }

        /**
         * Get the variable entry.
         * 
         * @return the name
         */
        public SymTableEntry getEntry() {
            return entry;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public OperandType getType() {
            return type;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public void setType(OperandType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * The Label Operand class represents labels in tuples.
     */
    public static class Label extends Operand {
        private String name;

        /**
         * Construct a new label operand.
         * 
         * @param name the label name
         */
        public Label(String name) {
            this.name = name;
        }

        /**
         * Get the label name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * The Temporary Operand class represents temporary values in tuples.
     */
    public static class Temporary extends Operand {
        private int number;
        private OperandType type;

        /**
         * Construct a new temporary operand.
         * 
         * @param number the temporary number
         */
        public Temporary(int number) {
            this.number = number;
            this.type = OperandType.NONE;
        }

        /**
         * Get the temporary number.
         * 
         * @return the number
         */
        public int getNumber() {
            return number;
        }

        /**
         * Get the operand type.
         * 
         * @return the type
         */
        public OperandType getType() {
            return type;
        }

        /**
         * Set the operand type.
         * 
         * @param type the type
         */
        public void setType(OperandType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "t" + number;
        }
    }

}
