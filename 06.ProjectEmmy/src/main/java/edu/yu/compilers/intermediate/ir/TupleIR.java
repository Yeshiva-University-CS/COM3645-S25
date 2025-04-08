package edu.yu.compilers.intermediate.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.yu.compilers.intermediate.ir.Operand.OperandType;
import edu.yu.compilers.intermediate.ir.Operand.Temporary;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;

/**
 * TupleIR is the intermediate representation used in the compiler.
 * It maintains collections of tuples organized by function, as well as
 * information about variables, functions, and constants.
 */
public class TupleIR {
    private int tempCounter = 0;

    private final Map<String, Integer> stringConstantPool = new LinkedHashMap<>();
    private int stringConstantCounter = 0;

    public TupleIR() {
        FunctionInfo f = enterFunctionScope("__global__");
        functionInfoList.add(f);
    }

    private final Stack<FunctionInfo> scopeStack = new Stack<>();

    private final List<FunctionInfo> functionInfoList = new ArrayList<>();
;
    public FunctionInfo globalFunctionScope() {
        return scopeStack.get(0);
    }

    private FunctionInfo currentFunctionScope() {
        return scopeStack.peek();
    }

    public FunctionInfo enterFunctionScope(String name) {
        int level = scopeStack.size();
        FunctionInfo scope = new FunctionInfo(name, level);
        scopeStack.push(scope);
        return scope;
    }

    public void exitFunctionScope() {
        if (scopeStack.isEmpty()) {
            return;
        }
        FunctionInfo functionInfo = scopeStack.pop();
        functionInfoList.add(functionInfo);
    }

    public List<FunctionInfo> getFunctionList() {
        return Collections.unmodifiableList(functionInfoList);
    }

    public static class FunctionInfo {
        private final int level;
        private final String name;
        private final Map<SymTableEntry, VariableInfo> variables = new HashMap<>();
        private final List<FunctionInfo> functions = new ArrayList<>();
        private final List<Tuple> tuples = new ArrayList<>();

        public FunctionInfo(String name, int level) {
            this.name = name;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public int getLevel() {
            return level;
        }

        public boolean isGlobalLevel() {
            return level == 0;
        }

        public List<VariableInfo> getVariables() {
            return Collections.unmodifiableList(variables.values().stream().toList());
        }

        public void addVariable(VariableInfo var) {
            variables.put(var.getEntry(), var);
        }

        public List<FunctionInfo> getFunctions() {
            return Collections.unmodifiableList(functions);
        }

        public void addFunction(FunctionInfo func) {
            functions.add(func);
        }

        public List<Tuple> getTuples() {
            return Collections.unmodifiableList(tuples);
        }

        public void addTuple(Tuple tuple) {
            tuples.add(tuple);
        }
    }

    /**
     * Class to store information about a variable.
     */
    public static class VariableInfo {
        private final SymTableEntry entry;
        private final OperandType type;
        private int paramIndex;

        public VariableInfo(SymTableEntry entry, OperandType type) {
            this.entry = entry;
            this.type = type;
        }

        public String getName() {
            return entry.getName();
        }

        public SymTableEntry getEntry() {
            return entry;
        }

        public OperandType getType() {
            return type;
        }

        public boolean isParameter() {
            return entry.isValueParameter();
        }

        public int getParamIndex() {
            return paramIndex;
        }

        public void setParamIndex(int paramIndex) {
            this.paramIndex = paramIndex;
        }

    }

    public void addTuple(Tuple tuple) {
        currentFunctionScope().addTuple(tuple);
    }

    public void addVariable(VariableInfo var) {
        currentFunctionScope().addVariable(var);
    }

    /**
     * Create a new temporary variable.
     * 
     * @return a new temporary operand
     */
    public Temporary newTemp() {
        return new Temporary(tempCounter++);
    }

    /**
     * Get the current temporary counter value.
     * 
     * @return the current temp count
     */
    public int getTempCount() {
        return tempCounter;
    }

    /**
     * Reset the temporary counter.
     */
    public void resetTempCounter() {
        tempCounter = 0;
    }

    /**
     * Get the string constant pool.
     * 
     * @return the string constant pool
     */
    public Map<String, Integer> getStringConstantPool() {
        return Collections.unmodifiableMap(stringConstantPool);
    }

    /**
     * Get the index for a string constant, adding it to the pool if needed.
     * 
     * @param str the string constant
     * @return the index in the pool
     */
    public int getStringConstant(String str) {
        return stringConstantPool.computeIfAbsent(str, s -> stringConstantCounter++);
    }

    /**
     * Register a string constant, adding it to the pool if needed.
     * 
     * @param str the string constant
     */
    public void registerStringConstant(String str) {
        getStringConstant(str);
    }

    /**
     * Get the set of string constants.
     * 
     * @return the set of string constants
     */
    public Set<String> getStringConstants() {
        return stringConstantPool.keySet();
    }

    @Override
    public String toString() {
        return String.format("%s with %d tuples", this.getClass().getSimpleName(), 0);
    }
}
