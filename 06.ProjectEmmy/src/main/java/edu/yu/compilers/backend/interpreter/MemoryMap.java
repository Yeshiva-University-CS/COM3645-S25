/**
 * <h1>MemoryMap</h1>
 * <p>The interpreter's runtime memory map.</p>
 * <p>Adapted from</p>
 * <p>Copyright (c) 2020 by Ronald Mak</p>
 */

package edu.yu.compilers.backend.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.yu.compilers.intermediate.symbols.SymTable;
import edu.yu.compilers.intermediate.symbols.SymTableEntry;
import edu.yu.compilers.intermediate.symbols.SymTableEntry.Kind;

public class MemoryMap extends HashMap<String, Cell> {
    /**
     * Constructor.
     * Create a memory map and allocate its memory cells
     * based on the entries in a symbol table.
     *
     * @param symTable the symbol table.
     */
    public MemoryMap(SymTable symTable) {
        ArrayList<SymTableEntry> entries = symTable.sortedEntries();

        // Loop for each entry of the symbol table.
        for (SymTableEntry entry : entries) {
            Kind kind = entry.getKind();

            switch (kind) {
                case VARIABLE:
                case FUNCTION:
                case VALUE_PARAMETER:

                default:
                    break;
            }
        }
    }

    /**
     * Return the memory cell with the given name.
     *
     * @param name the name.
     * @return the cell.
     */
    public Cell getCell(String name) {
        return get(name);
    }

    /**
     * Replace the memory cell with the given name.
     *
     * @param name the name.
     * @param cell the replacement cell.
     */
    public void replaceCell(String name, Cell cell) {
        put(name, cell);
    }

    /**
     * Get an arraylist of all the names in the memory map.
     *
     * @return the arraylist.
     */
    public ArrayList<String> getAllNames() {

        Set<String> names = keySet();

        return new ArrayList<>(names);
    }


}
