/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package assembler;

/**
 *
 * @author Scott
 */
class DuplicateSymbolException extends Exception {
    public DuplicateSymbolException(Statement statement) {
        super("Duplicate symbol found: " + statement.operation());
    }
}
