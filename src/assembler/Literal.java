package assembler;

public class Literal {
    private final String _name;
    private final String _value;
    private int _address;
    
    public Literal(String name, String value, int address) {
        _name = name;
        _value = value;
        _address = address;
    }
    
    public String name() {
        return _name;
    }
    
    public void updateAddress(int locctr) {
        _address = locctr;
    }
    
    public int address() {
        return _address;
    }
    
    public int length() {
        switch (_value.charAt(0)) {
            case 'C':
                return (_value.length() - 3); // C'EOF' -> EOF -> 3 bytes
            case 'X':
                return (_value.length() - 3) / 2; // X'05' -> 05 -> 2 half bytes
            default:
                return 0;
        }
    }
    
    public Statement toStatement() {
        return new Statement("*", _name, false, true, null);
    }
    
    public static Literal parse(String literal) {
        String name = literal;
        String value = name.substring(1);
        int address = 0;
        
        return new Literal(name, value, address);
    }
    
}
