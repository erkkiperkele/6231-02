package Server;


import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Date;
import Transport.Corba.BankServerPackage.Loan;
import Transport.Corba.LoanManager.BankServerPOA;

public class BankServerCorba extends BankServerPOA{


	//TODO: Implement the methods!

	@Override
	public short openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password) {
		return 42; //TEST!
	}

	@Override
	public Customer getCustomer(Bank bank, String email, String password) {
		return null;
	}

	@Override
	public Customer signIn(Bank bank, String email, String password) {
		return null;
	}

	@Override
	public Loan getLoan(Bank bankId, short accountNumber, String password, int loanAmount) {
		return null;
	}

	@Override
	public void delayPayment(Bank bank, short loanID, Date currentDueDate, Date newDueDate) {

	}

	@Override
	public String getCustomersInfo(Bank bank) {
		return null;
	}
}
