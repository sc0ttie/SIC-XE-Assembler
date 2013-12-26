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
