package assembler;

import java.util.HashMap;
import java.util.Map;

public class Utility {
    private static final Map<String, Operation> _operationTable;
    private static final Map<String, Integer> _registerTable;
    
    static {
        _operationTable = new HashMap<>();
        
        _operationTable.put("CLEAR", new Operation("CLEAR", "2",   "B4"));
        _operationTable.put("COMP",  new Operation("COMP",  "3/4", "28"));
        _operationTable.put("COMPR", new Operation("COMPR", "2",   "A0"));
        _operationTable.put("J",     new Operation("J",     "3/4", "3C"));
        _operationTable.put("JEQ",   new Operation("JEQ",   "3/4", "30"));
        _operationTable.put("JLT",   new Operation("JLT",   "3/4", "38"));
        _operationTable.put("JSUB",  new Operation("JSUB",  "3/4", "48"));
        _operationTable.put("LDA",   new Operation("LDA",   "3/4", "00"));
        _operationTable.put("LDB",   new Operation("LDB",   "3/4", "68"));
        _operationTable.put("LDCH",  new Operation("LDCH",  "3/4", "50"));
        _operationTable.put("LDT",   new Operation("LDT",   "3/4", "74"));
        _operationTable.put("RD",    new Operation("RD",    "3/4", "D8"));
        _operationTable.put("RSUB",  new Operation("RSUB",  "3/4", "4C"));
        _operationTable.put("STA",   new Operation("STA",   "3/4", "0C"));
        _operationTable.put("STCH",  new Operation("STCH",  "3/4", "54"));
        _operationTable.put("STL",   new Operation("STL",   "3/4", "14"));
        _operationTable.put("STX",   new Operation("STX",   "3/4", "10"));
        _operationTable.put("TD",    new Operation("TD",    "3/4", "E0"));
        _operationTable.put("TIXR",  new Operation("TIXR",  "2",   "B8"));
        _operationTable.put("WD",    new Operation("WD",    "3/4", "DC"));
        
        _registerTable = new HashMap<>();
        
        _registerTable.put(null, 0);
        _registerTable.put("A", 0);
        _registerTable.put("X", 1);
        _registerTable.put("L", 2);
        _registerTable.put("B", 3);
        _registerTable.put("S", 4);
        _registerTable.put("T", 5);
        _registerTable.put("F", 6);
        _registerTable.put("SW", 9);
        
    }
    
    public static Map<String, Operation> getOperaionTable() {
        return _operationTable;
    }
    
    public static Map<String, Integer> getRegisterTable() {
        return _registerTable;
    }
}
