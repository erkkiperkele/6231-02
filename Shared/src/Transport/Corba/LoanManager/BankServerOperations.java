package Transport.Corba.LoanManager;

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
    public short openAccount(Transport.Corba.BankServerPackage.Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password);

    /**
     * Operation getCustomer
     */
    public Transport.Corba.BankServerPackage.Customer getCustomer(Transport.Corba.BankServerPackage.Bank bank, String email, String password)
        throws Transport.Corba.BankServerPackage.FailedLoginException;

    /**
     * Operation signIn
     */
    public Transport.Corba.BankServerPackage.Customer signIn(Transport.Corba.BankServerPackage.Bank bank, String email, String password)
        throws Transport.Corba.BankServerPackage.FailedLoginException;

    /**
     * Operation getLoan
     */
    public Transport.Corba.BankServerPackage.Loan getLoan(Transport.Corba.BankServerPackage.Bank bankId, short accountNumber, String password, int loanAmount)
        throws Transport.Corba.BankServerPackage.FailedLoginException;

    /**
     * Operation delayPayment
     */
    public void delayPayment(Transport.Corba.BankServerPackage.Bank bank, short loanID, Transport.Corba.BankServerPackage.Date currentDueDate, Transport.Corba.BankServerPackage.Date newDueDate)
        throws Transport.Corba.BankServerPackage.RecordNotFoundException;

    /**
     * Operation getCustomersInfo
     */
    public Transport.Corba.BankServerPackage.BankInfo getCustomersInfo(Transport.Corba.BankServerPackage.Bank bank)
        throws Transport.Corba.BankServerPackage.FailedLoginException;

    /**
     * Operation TransferLoan
     */
    public Transport.Corba.BankServerPackage.Loan TransferLoan(short LoanId, Transport.Corba.BankServerPackage.Bank CurrentBank, Transport.Corba.BankServerPackage.Bank OtherBank)
        throws Transport.Corba.BankServerPackage.TransferException;

}
