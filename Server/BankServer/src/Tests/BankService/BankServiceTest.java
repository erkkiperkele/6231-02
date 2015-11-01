package Tests.BankService;

import Contracts.IBankService;
import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;
import Data.Loan;
import Services.BankService;
import Services.SessionService;
import Exceptions.RecordNotFoundException;

import javax.security.auth.login.FailedLoginException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BankServiceTest {


    private static IBankService bankService;

    public static void main(String[] args) {

        String serverArg = args.length == 0
                ? "1"
                :args[0];

        initialize(serverArg);

        //Server tests
        runTests();
    }

    private static void initialize(String arg) {
        Bank serverName = Bank.fromInt(Integer.parseInt(arg));
        SessionService.getInstance().setBank(serverName);
        bankService = new BankService();
    }

    private static void runTests() {
        testInitial();
        testOpeningMultipleAccounts();
        testUDPGetLoan();
        testPrintCustomersInfo();
        testDelayPayment();
    }

    public static void testInitial() {

        try {
            String unknownUsername = "dummy@dummy.com";
        Customer unknown = bankService.getCustomer(unknownUsername, "aa");
        printCustomer(unknown, unknownUsername);

        String mariaUsername = "maria.etinger@gmail.com";
        Customer maria = bankService.getCustomer(mariaUsername, "aa");
        printCustomer(maria, mariaUsername);

        String justinUsername = "justin.paquette@gmail.com";
        Customer justin = bankService.getCustomer(justinUsername, "aa");
        printCustomer(justin, justinUsername);

        String alexUserName = "alex.emond@gmail.com";
        Customer alex = null;
            alex = bankService.getCustomer(alexUserName, "aa");
        printCustomer(alex, alexUserName);

        List<Loan> alexLoans = bankService.getLoans(alex.getAccountNumber());
        printLoans(alexLoans, alex.getFirstName());

        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
    }

    public static void testOpeningMultipleAccounts() {
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

    public static void testUDPGetLoan() {

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

    public static void testPrintCustomersInfo() {
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

    public static void testDelayPayment() {
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
