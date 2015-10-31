package Server;


import Contracts.IBankService;
import Data.CustomerInfo;
import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Date;
import Transport.Corba.BankServerPackage.Loan;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.LoanManager.BankServerPOA;
import Transport.RMI.RecordNotFoundException;

import javax.security.auth.login.FailedLoginException;

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

        return getCustomer(bank, email, password);
    }

	@Override
	public Loan getLoan(Bank bank, short accountNumber, String password, int loanAmount) {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        Data.Loan serverLoan = null;
        try {
            serverLoan = bankService.getLoan(serverBank, accountNumber, password, loanAmount);
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
        return ObjectMapper.toCorbaLoan(serverLoan);
	}

	@Override
	public void delayPayment(Bank bank, short loanID, Date currentDueDate, Date newDueDate) {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        java.util.Date serverCurrentDueDate = ObjectMapper.toDate(currentDueDate);
        java.util.Date serverNewDueDate = ObjectMapper.toDate(newDueDate);
        try {
            bankService.delayPayment(serverBank, (int)loanID, serverCurrentDueDate, serverNewDueDate);
        } catch (RecordNotFoundException e) {
            e.printStackTrace();
        }
    }

	@Override
	public String getCustomersInfo(Bank bank) {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        CustomerInfo[] customersInfo = new CustomerInfo[0];
        try {
            customersInfo = bankService.getCustomersInfo(serverBank);
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
        return ObjectMapper.toCorbaCustomersInfo(customersInfo);
    }
}
