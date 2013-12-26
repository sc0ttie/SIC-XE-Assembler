package assembler;

public class HeaderRecord implements Record {
    private final String _programName;
    private final int _startAddress;
    private final int _programLength;
    
    public HeaderRecord(String name, int startAddr, int length) {
        _programName = name;
        _startAddress = startAddr;
        _programLength = length;
    }
    
    @Override
    public String toObjectProgram() {
        return String.format("H%1$-6s%2$06X%3$06X", _programName, _startAddress, _programLength);
    }
    
}
