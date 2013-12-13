package assembler;

import java.io.Serializable;

/**
 *
 * @author Malthael
 */
public class Statement implements Serializable {
    private final String _label;
    private final String _operation;
    private final String[] _symbols;
    private final String _comment;
    private final boolean _extended;
    private String _location;
    
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
        return _operation.compareTo(".") == 0;
    }
    
    public boolean isExtended() {
        return _extended;
    }
    
    public void setLoc(int loc) {
        _location = Integer.toHexString(loc).toUpperCase();
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

            if (index < tokens.length) {
                symbols = new String[2];
                int pos = tokens[index].indexOf(',');
                if (pos >= 0) {
                    symbols[0] = tokens[index].substring(0, pos);
                    symbols[1] = tokens[index].substring(pos + 1);
                } else {
                    symbols[0] = tokens[index];
                    symbols[1] = null;
                }
            } else {
                symbols = null;
            }

            return new Statement(label, operation, extended, symbols);
        }
    }
    
    @Override
    public String toString() {
        String s = _location + "\t";
        
        if (isComment()) {
            s += ".\t" + _comment;
        } else {
            if (_label != null) {
                s += _label;
            }

            s += "\t" + _operation + "\t";

            if (_symbols != null) {
                s += _symbols[0];

                if (_symbols[1] != null) {
                    s +=  "," + _symbols[1];
                }
            }
        }
        
        return s;
    }
}
