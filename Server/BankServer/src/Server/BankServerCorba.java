package Server;


import Contracts.IBankService;
import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Date;
import Transport.Corba.BankServerPackage.Loan;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.LoanManager.BankServerPOA;

public class BankServerCorba extends BankServerPOA{

    private IBankService bankService;
    private int serverPort;

    public BankServerCorba(IBankService bankService, int serverPort) {
        this.bankService = bankService;
        this.serverPort = serverPort;
    }


	//TODO: Implement the methods!

	@Override
	public short openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password) {
		System.out.println("openning account!!!!");

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        int accountNumber = bankService.openAccount(serverBank, firstName, lastName, emailAddress, phoneNumber, password);
        return (short)accountNumber;
    }

	@Override
	public Customer getCustomer(Bank bank, String email, String password) {
        System.out.println("get customer!!!!");

        Data.Customer serverCustomer = bankService.getCustomer(email);
        return ObjectMapper.toCorbaCustomer(serverCustomer);
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
