package assembler;

import java.io.Serializable;

public class Statement implements Serializable, Comparable {
    private final String _label;
    private final String _operation;
    private final String[] _symbols;
    private final String _comment;
    private final boolean _extended;
    private int _location;
    
    private Statement(String label, String operation, boolean extended, String[] symbols, String comment) {
        _label = label;
        _operation = operation;
        _extended = extended;
        _symbols = symbols;
        _comment = comment;
    }
    
    public Statement(String label, String operation, boolean extended, String[] symbols) {
        this(label, operation, extended, symbols, null);
    }
    
    public Statement(String comment) {
        this(null, ".", false, null, comment);
    }
    
    public String label() {
        return _label;
    }
    
    public String operation() {
        return _operation;
    }
    
    public String operand1() {
        return _symbols[0];
    }
    
    public String operand2() {
        return _symbols[1];
    }
    
    public boolean isComment() {
        return _comment != null;
    }
    
    public boolean isExtended() {
        return _extended;
    }
    
    public void setLocation(int loc) {
        _location = loc;
    }
    
    public int location() {
        return _location;
    }
    
    public static Statement parse(String statement) {
        String[] tokens = statement.trim().split("\t");
        
        if (tokens[0].compareTo(".") == 0) {
            return new Statement(statement.substring(statement.indexOf('.') + 1));
        } else {
            String label, operation;
            String[] symbols;
            boolean extended = false;
            int index = 0;

            if (tokens.length == 3) {
                label = tokens[index++];
            } else {
                label = null;
            }

            operation = tokens[index++];
            if (operation.charAt(0) == '+') {
                extended = true;
                operation = operation.substring(1);
            }

            symbols = new String[2];
            if (index < tokens.length) {
                int pos = tokens[index].indexOf(',');
                if (pos >= 0) {
                    symbols[0] = tokens[index].substring(0, pos);
                    symbols[1] = tokens[index].substring(pos + 1);
                } else {
                    symbols[0] = tokens[index];
                    symbols[1] = null;
                }
            } else {
                symbols[0] = symbols[1] = null;
            }

            return new Statement(label, operation, extended, symbols);
        }
    }
    
    @Override
    public String toString() {
        String s = String.format("%1$04X", _location) + "\t";
        
        if (isComment()) {
            s += ".\t" + _comment;
        } else {
            if (_label != null) {
                s += _label;
            }

            s += "\t";
            
            if (_extended) {
                s += '+';
            }
            
            s += _operation + "\t";

            if (_symbols != null) {
                if (_symbols[0] != null) {
                    s += _symbols[0];
                }

                if (_symbols[1] != null) {
                    s +=  "," + _symbols[1];
                }
            }
        }
        
        return s;
    }

    @Override
    public int compareTo(Object o) {
        return _operation.compareTo((String) o);
    }
}
