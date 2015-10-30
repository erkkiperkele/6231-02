package Transport.Corba.LoanManager;

import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Date;
import Transport.Corba.BankServerPackage.Loan;

/**
 * Interface definition: BankServer.
 * 
 * @author OpenORB Compiler
 */
public interface BankServerOperations
{
    /**
     * Operation openAccount
     */
    public short openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password);

    /**
     * Operation getCustomer
     */
    public Customer getCustomer(Bank bank, String email, String password);

    /**
     * Operation signIn
     */
    public Customer signIn(Bank bank, String email, String password);

    /**
     * Operation getLoan
     */
    public Loan getLoan(Bank bankId, short accountNumber, String password, int loanAmount);

    /**
     * Operation delayPayment
     */
    public void delayPayment(Bank bank, short loanID, Date currentDueDate, Date newDueDate);

    /**
     * Operation getCustomersInfo
     */
    public String getCustomersInfo(Bank bank);

}
