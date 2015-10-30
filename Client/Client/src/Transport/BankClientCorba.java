package Transport;

import Contracts.ICustomerServer;
import Contracts.IManagerServer;
import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;
import Data.Loan;
import Transport.Corba.Helpers.ObjectMapper;
import Transport.Corba.Helpers.ObjectMappingException;
import Transport.Corba.LoanManager.BankServer;
import Transport.Corba.LoanManager.BankServerHelper;
import Transport.RMI.RecordNotFoundException;
import org.omg.CORBA.ORB;

import javax.security.auth.login.FailedLoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;

import Transport.Corba.*;

public class BankClientCorba implements ICustomerServer, IManagerServer{

    private BankServer bankServer;

//    public static void main(String[] args) throws IOException {
    public BankClientCorba(Bank bank) {

        //TODO: Set the bank! (how ot specify which port to use?

        String[] args = new String[]{};
        ORB orb = ORB.init(args, null);

        //Fetch ior
        BufferedReader br = null;
        String ior = null;
        try {
            br = new BufferedReader(new FileReader("ior.txt"));
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


        //TODO: User methods from here like in the customerRMI! Eg:
        //aBankServer.delayPayment();

    }

    @Override
    public int openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password) throws RemoteException {

        Transport.Corba.BankServerPackage.Bank corbaBank = null;
        try {
            corbaBank = ObjectMapper.MapToCorbaObject(bank);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return bankServer.openAccount(corbaBank, firstName, lastName, emailAddress, phoneNumber, password);
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password) throws RemoteException, FailedLoginException {
        return null;
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws RemoteException, FailedLoginException {
        return null;
    }

    @Override
    public Loan getLoan(Bank bankId, int accountNumber, String password, long loanAmount) throws RemoteException, FailedLoginException {
        return null;
    }

    @Override
    public void delayPayment(Bank bank, int loanID, Date currentDueDate, Date newDueDate) throws RemoteException, RecordNotFoundException {

    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws RemoteException, FailedLoginException {
        return new CustomerInfo[0];
    }
}
