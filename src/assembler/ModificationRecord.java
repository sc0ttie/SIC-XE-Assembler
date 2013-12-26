package assembler;

public class ModificationRecord implements Record {
    private final int _location;
    private final int _length;
    
    public ModificationRecord(int modifiedLoc, int modifiedLen) {
        _location = modifiedLoc;
        _length = modifiedLen;
    }
    
    @Override
    public String toObjectProgram() {
        return String.format("M%06X%02X", _location, _length);
    }
    
}
