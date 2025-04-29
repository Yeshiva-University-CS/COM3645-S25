package edu.yu.compilers.backend.compiler;

/**
 * Represents an operand in X86_64 assembly code.
 */
public abstract class X86_64Operand {
    
    /**
     * Returns a string representation of the operand.
     */
    public abstract String toString();
    
    /**
     * Represents a register operand.
     */
    public static class Register extends X86_64Operand {
        private final X86_64Register register;
        
        public Register(X86_64Register register) {
            this.register = register;
        }
        
        @Override
        public String toString() {
            return register.toString().toLowerCase();
        }
    }
    
    /**
     * Represents an immediate value operand.
     */
    public static class Immediate extends X86_64Operand {
        private final String value;
        
        public Immediate(String value) {
            this.value = value;
        }
        
        public Immediate(int value) {
            this.value = Integer.toString(value);
        }
        
        public Immediate(long value) {
            this.value = Long.toString(value);
        }
        
        @Override
        public String toString() {
            return "$" + value;
        }
    }
    
    /**
     * Represents a memory operand.
     */
    public static class Memory extends X86_64Operand {
        private final String address;
        
        public Memory(String address) {
            this.address = address;
        }
        
        @Override
        public String toString() {
            return address;
        }
    }
    
    /**
     * Represents a label operand.
     */
    public static class Label extends X86_64Operand {
        private final String name;
        
        public Label(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Represents a symbolic reference operand, 
     * typically used for function calls through GOT entries.
     */
    public static class Symbol extends X86_64Operand {
        private final String symbol;
        
        public Symbol(String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return "*" + symbol;
        }
    }
}
