package assembler;

public class EndRecord implements Record {
    private final int _startAddress;
    
    public EndRecord(int startAddr) {
        _startAddress = startAddr;
    }
    
    @Override
    public String toObjectProgram() {
        return String.format("E%1$06X", _startAddress);
    }
    
}
