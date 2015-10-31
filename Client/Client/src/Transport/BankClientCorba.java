package Transport;

import Contracts.ICustomerServer;
import Contracts.IManagerServer;
import Data.*;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.LoanManager.BankServer;
import Transport.Corba.LoanManager.BankServerHelper;
import Transport.RMI.RecordNotFoundException;
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
        Transport.Corba.BankServerPackage.Customer corbaCustomer =
                bankServer.getCustomer(corbaBank, email, password);

        return ObjectMapper.toCustomer(corbaCustomer);
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws FailedLoginException {
        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Customer corbaCustomer =
                bankServer.signIn(corbaBank, email, password);

        return ObjectMapper.toCustomer(corbaCustomer);
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount) throws FailedLoginException {
        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        Transport.Corba.BankServerPackage.Loan corbaLoan =
                bankServer.getLoan(corbaBank, (short)accountNumber, password, (int)loanAmount);

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

        bankServer.delayPayment(corbaBank, (short)loanID, corbaCurrentDueDate, corbaNewDueDate);
    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws FailedLoginException {

        Transport.Corba.BankServerPackage.Bank corbaBank =
                ObjectMapper.toCorbaBank(bank);
        String customersInfo = bankServer.getCustomersInfo(corbaBank);

        //TODO: Implement the toCustomersInfo method!
        return ObjectMapper.toCustomersInfo(customersInfo);
    }
}
