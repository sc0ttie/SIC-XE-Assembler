/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package assembler;

/**
 *
 * @author Malthael
 */
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
