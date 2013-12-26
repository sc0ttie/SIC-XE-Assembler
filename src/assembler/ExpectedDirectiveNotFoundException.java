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
public class ExpectedDirectiveNotFoundException extends Exception {
    public ExpectedDirectiveNotFoundException(String errMsg) {
        super(errMsg);
    }
}
