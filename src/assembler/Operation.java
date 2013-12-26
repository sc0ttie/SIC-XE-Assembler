package assembler;

public class Operation {
    private String _mnemonic;
    private String _opcode;
    private String _format;
    
    public Operation(String mnemonic, String format, String opcode) {
        _mnemonic = mnemonic;
        _opcode = opcode;
        _format = format;
    }
    
    public String mnemonic() {
        return _mnemonic;
    }
    
    public String opcode() {
        return _opcode;
    }
    
    public String format() {
        return _format;
    }
}
