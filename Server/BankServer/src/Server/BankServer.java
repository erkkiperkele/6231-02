package Server;

import Contracts.ICustomerServer;
import Contracts.IManagerServer;
import Data.*;
import Services.BankService;
import Services.SessionService;
import Transport.RMI.RecordNotFoundException;
import Transport.UDP.UDPServer;

import javax.security.auth.login.FailedLoginException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class starts both RMI and UDP servers for a given bank.
 * It also contains basic tests for data access (concurrency, UDP protocols...)
 */
public class BankServer implements ICustomerServer, IManagerServer {

    private static int serverPort;
    private static BankService bankService;

    private static UDPServer udp;

    public BankServer(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Instatiates and starts the RMI and UDP servers.
     * ATTENTION: needs a single integer argument which is the bank Id for the server
     * See the Bank enum to know what integer corresponds to what bank.
     *
     * @param args a single integer defining what bank this server belongs to.
     */
    public static void main(String[] args) {

        String serverArg = args[0];
        initialize(serverArg);

        //Starting bank server
        startRMIServer();
        startUDPServer();

        //Server tests
        testInitial();
        testOpeningMultipleAccounts();
        testUDPGetLoan();
        testPrintCustomersInfo();
        testDelayPayment();
    }

    private static void initialize(String arg) {
        Bank serverName = Bank.fromInt(Integer.parseInt(arg));
        SessionService.getInstance().setBank(serverName);
        bankService = new BankService();
        udp = new UDPServer(bankService);
    }

    private static void startRMIServer() {

        Bank bank = SessionService.getInstance().getBank();
        int serverPort = ServerPorts.getRMIPort(bank);

        try {
            (new BankServer(serverPort)).exportServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SessionService.getInstance().log().info(
                String.format("%s Server is up and running on port %d!",
                        bank,
                        serverPort)
        );
    }

    private static void startUDPServer() {
        Thread startUdpServer = new Thread(() ->
        {
            udp.startServer();
        });
        startUdpServer.start();
    }

    /**
     * Exports both the customer and manager RMI servers.
     * Actually both are the same server, but 2 endpoints
     * (so that customers and managers can't access each others API)
     *
     * @throws Exception
     */
    public void exportServer() throws Exception {
        Remote obj = UnicastRemoteObject.exportObject(this, serverPort);
        Registry r = LocateRegistry.createRegistry(serverPort);

        r.bind("customer", obj);    //Access for customer console
        r.bind("manager", obj);    //Access for manager console
    }

    @Override
    public int openAccount(Bank bank, String firstName, String lastName, String emailAddress, String phoneNumber, String password)
            throws RemoteException {

        int accountNumber = bankService.openAccount(bank, firstName, lastName, emailAddress, phoneNumber, password);

        return accountNumber;
    }

    @Override
    public Customer getCustomer(Bank bank, String email, String password)
            throws RemoteException, FailedLoginException {

        Customer foundCustomer = bankService.getCustomer(email);

        if (foundCustomer == null) {
            throw new FailedLoginException(String.format("Customer doesn't exist for email: %s", email));
        }
        if (!foundCustomer.getPassword().equals(password)) {
            throw new FailedLoginException(String.format("Wrong password for email %s", email));
        }

        return foundCustomer;
    }

    @Override
    public Customer signIn(Bank bank, String email, String password) throws RemoteException, FailedLoginException {
        return getCustomer(bank, email, password);
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount)
            throws RemoteException, FailedLoginException {

        Loan newLoan = bankService.getLoan(bank, accountNumber, password, loanAmount);
        return newLoan;
    }

    @Override
    public void delayPayment(Bank bank, int loanID, Date currentDueDate, Date newDueDate) throws RecordNotFoundException {
        bankService.delayPayment(bank, loanID, currentDueDate, newDueDate);
    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws FailedLoginException {
        return bankService.getCustomersInfo(bank);
    }

    private static void testInitial() {
        String unknownUsername = "dummy@dummy.com";
        Customer unknown = bankService.getCustomer(unknownUsername);
        printCustomer(unknown, unknownUsername);

        String mariaUsername = "maria.etinger@gmail.com";
        Customer maria = bankService.getCustomer(mariaUsername);
        printCustomer(maria, mariaUsername);

        String justinUsername = "justin.paquette@gmail.com";
        Customer justin = bankService.getCustomer(justinUsername);
        printCustomer(justin, justinUsername);

        String alexUserName = "alex.emond@gmail.com";
        Customer alex = bankService.getCustomer(alexUserName);
        printCustomer(alex, alexUserName);

        List<Loan> alexLoans = bankService.getLoans(alex.getAccountNumber());
        printLoans(alexLoans, alex.getFirstName());
    }

    private static void testOpeningMultipleAccounts() {
        System.out.println(String.format("Start of concurrent account creation"));

        Bank bank = SessionService.getInstance().getBank();

        String firstName = "Concurrent";
        String lastName = "concu";
        String email = "concurrent@thread.com";
        String phone = "";
        String password = "c";

        Thread openAccountTask1 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "1", lastName, email + "1", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s1", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask2 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "2", lastName, email + "2", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s2", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask3 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "3", lastName, email + "3", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s3", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask4 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "4", lastName, email + "4", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s4", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask5 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "5", lastName, email + "5", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s5", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask6 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "6", lastName, email + "6", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s6", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask7 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "7", lastName, email + "7", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s7", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountTask8 = new Thread(() ->
        {
            bankService.openAccount(bank, firstName + "8", lastName, email + "8", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s8", Thread.currentThread().getId(), firstName));
        });

        Thread openAccountCreatedByTask1 = new Thread(() ->
        {
            System.out.println(String.format("thread #%d STARTING an account for %s1", Thread.currentThread().getId(), firstName));
            bankService.openAccount(bank, firstName + "1", lastName, email + "1", phone, password);
            System.out.println(String.format("thread #%d OPENED an account for %s1", Thread.currentThread().getId(), firstName));
        });

//        Thread getAccount = new Thread(() ->
//        {
//            bankService.getCustomer(email + "1");
//            System.out.println(String.format("thread #%d OPENED an account for %s1", Thread.currentThread().getId(), firstName));
//        });

        openAccountTask1.start();
        openAccountTask2.start();
        openAccountTask3.start();
        openAccountTask4.start();
        openAccountTask5.start();
        openAccountTask6.start();
        openAccountTask7.start();
        openAccountTask8.start();
        openAccountCreatedByTask1.start();

        System.out.println(String.format("End of concurrent account creation"));
    }

    private static void testUDPGetLoan() {

        Bank bank = SessionService.getInstance().getBank();

        if (bank == Bank.Royal) {

            int accountNumber = 2;
            String password = "aa";
            long loanAmount = 200;


            //Wait for all servers to start before sending a message.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                bankService.getLoan(bank, accountNumber, password, loanAmount);
            } catch (FailedLoginException e) {
                e.printStackTrace();
            }
        }
    }

    private static void testPrintCustomersInfo() {
        Bank bank = SessionService.getInstance().getBank();
        try {
            CustomerInfo[] customersInfo = bankService.getCustomersInfo(bank);
            for (CustomerInfo info : customersInfo) {
                System.out.println(info.toString());
            }
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
    }

    private static void testDelayPayment() {
        Bank bank = SessionService.getInstance().getBank();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date currentDate = dateFormat.parse("02-03-2016");
            Date newDueDate = dateFormat.parse("18-12-2016");
            bankService.delayPayment(bank, 2, currentDate, newDueDate);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (RecordNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void printCustomer(Customer customer, String username) {
        if (customer != null) {
            SessionService.getInstance().log().info(customer.toString());
        } else {
            SessionService.getInstance().log().info(String.format("No customer found for this username %s", username));
        }
    }

    private static void printLoans(List<Loan> loans, String customerName) {
        if (loans != null && loans.size() > 0) {
            for (Loan loan : loans) {
                SessionService.getInstance().log().info(loan.toString());
            }
        } else {
            SessionService.getInstance().log().info(String.format("%1$s has no loans currently", customerName));
        }
    }
}
