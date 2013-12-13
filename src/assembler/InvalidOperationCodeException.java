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
class InvalidOperationCodeException extends Exception {
    public InvalidOperationCodeException(Statement statement) {
        super("Invalid operation code found: " + statement.operation());
    }
}
