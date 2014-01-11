package assembler;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private String _name;
    private int _address;
    private boolean _resolved;
    private List<Integer> _references;
    
    public Symbol(String name) {
        this(name, 0, false);
    }
    
    public Symbol(String name, int address) {
        this(name, address, true);
    }
    
    private Symbol(String name, int address, boolean resolved) {
        _name = name;
        _address = address;
        _resolved = resolved;
        
        _references = null;
    }
    
    public int address() {
        return _address;
    }
    
    public void setAddress(int address) {
        _address = address;
    }
    
    public boolean beResolved() {
        return _resolved;
    }
    
    public void addUnresolvedAddress(int address) {
        if (_references == null) {
            _references = new ArrayList<>();
        }
        
        _references.add(address);
    }
    
    public List<Integer> getUnresolvedAddresses() {
        return _references;
    }
}
