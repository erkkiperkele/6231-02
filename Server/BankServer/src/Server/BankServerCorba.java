package Server;


import Contracts.IBankService;
import Data.CustomerInfo;
import Transport.Corba.BankServerPackage.*;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.LoanManager.BankServerPOA;
import Exceptions.RecordNotFoundException;

import javax.security.auth.login.FailedLoginException;

public class BankServerCorba extends BankServerPOA {

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
        return (short) accountNumber;
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password)
            throws Transport.Corba.BankServerPackage.FailedLoginException {

        System.out.println("get customer!!!!");

        Data.Customer serverCustomer = null;
        try {
            serverCustomer = bankService.getCustomer(email, password);
        } catch (FailedLoginException e) {
            throw new Transport.Corba.BankServerPackage.FailedLoginException(e.getMessage());
        }

        return ObjectMapper.toCorbaCustomer(serverCustomer);
    }

    @Override
    public Customer signIn(Bank bank, String email, String password)
            throws Transport.Corba.BankServerPackage.FailedLoginException {

        return getCustomer(bank, email, password);
    }

    @Override
    public Loan getLoan(Bank bank, short accountNumber, String password, int loanAmount)
            throws Transport.Corba.BankServerPackage.FailedLoginException {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        Data.Loan serverLoan = null;
        try {
            serverLoan = bankService.getLoan(serverBank, accountNumber, password, loanAmount);
        } catch (FailedLoginException e) {
            throw new Transport.Corba.BankServerPackage.FailedLoginException(e.getMessage());
        }
        return ObjectMapper.toCorbaLoan(serverLoan);
    }

    @Override
    public void delayPayment(Bank bank, short loanID, Date currentDueDate, Date newDueDate)
            throws Transport.Corba.BankServerPackage.RecordNotFoundException {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        java.util.Date serverCurrentDueDate = ObjectMapper.toDate(currentDueDate);
        java.util.Date serverNewDueDate = ObjectMapper.toDate(newDueDate);
        try {
            bankService.delayPayment(serverBank, (int) loanID, serverCurrentDueDate, serverNewDueDate);
        } catch (RecordNotFoundException e) {
            throw new Transport.Corba.BankServerPackage.RecordNotFoundException(e.getMessage());
        }
    }

    @Override
    public BankInfo getCustomersInfo(Bank bank)
            throws Transport.Corba.BankServerPackage.FailedLoginException {

        Data.Bank serverBank = ObjectMapper.toBank(bank);
        CustomerInfo[] customersInfo;
        try {
            customersInfo = bankService.getCustomersInfo(serverBank);
            return ObjectMapper.toCorbaBankInfo(customersInfo);
        } catch (FailedLoginException e) {
            throw new Transport.Corba.BankServerPackage.FailedLoginException(e.getMessage());
        }
    }

    //TODO: Real implementation!
    @Override
    public Loan transferLoan(short LoanId, Bank CurrentBank, Bank OtherBank) throws TransferException {

        short loanId = -1;
        return new Loan(loanId, loanId, -1, new Date());
    }
}
