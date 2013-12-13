package assembler;

/**
 *
 * @author Malthael
 */
public class Operation {
    private String _mnemonic;
    private String _opcode;
    private int _format;
    
    public Operation(String mnemonic, String format, String opcode) {
        _mnemonic = mnemonic;
        _opcode = opcode;
        
        switch (format) {
            case "1":
                _format = 1;
                break;
            case "2":
                _format = 2;
                break;
            case "3/4":
                _format = 3;
                break;
        }
    }
    
    public String mnemonic() {
        return _mnemonic;
    }
    
    public String opcode() {
        return _opcode;
    }
    
    public int format() {
        return _format;
    }
}
