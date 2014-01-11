package assembler;

import java.util.ArrayList;
import java.util.List;

public class TextRecord implements Record {
    private final int _startAddress;
    private int _length;
    private final List<String> _objectCodes;
    
    public static final int MAX_LENGTH = 0x20;
    
    public TextRecord(int startAddr) {
        _startAddress = startAddr;
        _length = 0;
        _objectCodes = new ArrayList<>();
    }
    
    public boolean add(String objectCode) {
        if (objectCode.length() == 0) {
            return true;
        } else if (_length + objectCode.length() / 2 <= MAX_LENGTH) {
            _objectCodes.add(objectCode);
            _length += objectCode.length() / 2;
            
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public String toObjectProgram() {
        String buf = String.format("T%06X%02X", _startAddress, _length);
        
        for (String s : _objectCodes) {
            buf += s;
        }
        
        return buf;
    }
    
}
