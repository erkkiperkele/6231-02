package Transport;

import Contracts.ICustomerServer;
import Contracts.IManagerServer;
import Data.*;
import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;
import Data.Loan;
import Exceptions.RecordNotFoundException;
import Exceptions.TransferException;
import Transport.Corba.BankServerPackage.*;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.LoanManager.BankServer;
import Transport.Corba.LoanManager.BankServerHelper;
import org.omg.CORBA.ORB;

import javax.security.auth.login.FailedLoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class BankClientCorba implements ICustomerServer, IManagerServer {

    private BankServer bankServer;

    public BankClientCorba(Bank bank) {

        String[] args = new String[]{};

        Properties props = new Properties();
        props.put("ORBPort", ServerPorts.getRMIPort(bank));
        ORB orb = ORB.init(args, props);

        //Fetch ior
        BufferedReader br = null;
        String ior = null;
        try {
            br = new BufferedReader(new FileReader(String.format("ior_%s.txt", bank.name())));
            ior = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Transform ior to CORBA obj
        org.omg.CORBA.Object o = orb.string_to_object(ior);
        bankServer = BankServerHelper.narrow(o);
    }

    @Override
    public int openAccount(
            Bank bank,
            String firstName,
            String lastName,
            String emailAddress,
            String phoneNumber,
            String password
    ) {

        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);

        return bankServer.openAccount(corbaBank, firstName, lastName, emailAddress, phoneNumber, password);
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password) throws FailedLoginException {

        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Customer corbaCustomer = null;

        try {
            corbaCustomer = bankServer.getCustomer(corbaBank, email, password);
        } catch (Transport.Corba.BankServerPackage.FailedLoginException e) {
            throw new FailedLoginException(e.message);
        }

        return ObjectMapper.toCustomer(corbaCustomer);
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws FailedLoginException {
        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Customer corbaCustomer = null;

        try {
            corbaCustomer = bankServer.signIn(corbaBank, email, password);
        } catch (Transport.Corba.BankServerPackage.FailedLoginException e) {
            throw new FailedLoginException(e.message);
        }

        return ObjectMapper.toCustomer(corbaCustomer);
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount) throws FailedLoginException {
        Transport.Corba.BankServerPackage.Bank corbaBank = ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Loan corbaLoan = null;

        try {
            corbaLoan = bankServer.getLoan(corbaBank, (short) accountNumber, password, (int) loanAmount);
        } catch (Transport.Corba.BankServerPackage.FailedLoginException e) {
            throw new FailedLoginException(e.message);
        }

        return ObjectMapper.toLoan(corbaLoan);
    }

    @Override
    public void delayPayment(Bank bank, int loanID, Date currentDueDate, Date newDueDate) throws RecordNotFoundException {

        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Date corbaCurrentDueDate =
                ObjectMapper.toCorbaDate(currentDueDate);
        Transport.Corba.BankServerPackage.Date corbaNewDueDate =
                ObjectMapper.toCorbaDate(newDueDate);

        try {
            bankServer.delayPayment(corbaBank, (short) loanID, corbaCurrentDueDate, corbaNewDueDate);
        } catch (Transport.Corba.BankServerPackage.RecordNotFoundException e) {
            throw new RecordNotFoundException(e.message);
        }
    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws FailedLoginException {

        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);

        try {
            BankInfo bankInfo = bankServer.getCustomersInfo(corbaBank);
            return ObjectMapper.toCustomersInfo(bankInfo);
        } catch (Transport.Corba.BankServerPackage.FailedLoginException e) {
            throw new FailedLoginException(e.message);
        }
    }

    @Override
    public Loan transferLoan(int loanId, Bank currentBank, Bank otherBank) throws TransferException {

        Transport.Corba.BankServerPackage.Bank corbaCurrentBank = ObjectMapper.toCorbaBank(currentBank);
        Transport.Corba.BankServerPackage.Bank corbaOtherBank = ObjectMapper.toCorbaBank(otherBank);

        try {
            Transport.Corba.BankServerPackage.Loan newLoan =
                    bankServer.transferLoan((short) loanId, corbaCurrentBank, corbaOtherBank);
            return ObjectMapper.toLoan(newLoan);

        } catch (Transport.Corba.BankServerPackage.TransferException e) {
            throw new TransferException(e.message);
        }
    }
}
