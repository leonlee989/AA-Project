/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package context;

/**
 *
 * @author Melvrick
 */
public class TransactionFailureException extends Exception{
    public TransactionFailureException (String m){
        super(m);
    }
}
