package Services;

import Contracts.IBankService;
import Data.*;
import Exceptions.RecordNotFoundException;
import Exceptions.TransferException;
import Transport.UDP.*;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * The bank service provides an implementation for both the customer and manager services.
 * (See interface documentation for details on the functionality provided by both those services)
 */
public class BankService implements IBankService {

    private DataRepository repository;
    private UDPClient udp;
    private final long DEFAULT_CREDIT_LIMIT = 1500;

    public BankService() {
        this.repository = new DataRepository();
        this.udp = new UDPClient();
    }

    @Override
    public int openAccount(Bank bank, String firstName, String lastName, String email, String phone, String password) {


        Customer newCustomer = new Customer(firstName, lastName, password, email, phone);

        this.repository.createAccount(newCustomer, DEFAULT_CREDIT_LIMIT);

        return this.repository.getAccount(newCustomer.getUserName()).getAccountNumber();
        //log inside the repository to manage errors.
    }

    @Override
    public Customer getCustomer(String email, String password) throws FailedLoginException {

        Customer foundCustomer = this.repository.getCustomer(email);


        if (foundCustomer == null) {
            throw new FailedLoginException(String.format("Customer doesn't exist for email: %s", email));
        }
        if (!foundCustomer.getPassword().equals(password)) {
            throw new FailedLoginException(String.format("Wrong password for email %s", email));
        }

        return foundCustomer;
    }

    @Override
    public List<Loan> getLoans(int accountNumber) {

        List<Loan> loans = this.repository.getLoans(accountNumber);

        if (loans.size() < 0) {
            SessionService.getInstance().log().info(
                    String.format("%1$d loans have been retrieved for account %2$d",
                            loans.size(),
                            accountNumber)
            );
        }

        return loans;
    }

    @Override
    public Loan getLoan(Bank bank, int accountNumber, String password, long loanAmount) throws FailedLoginException {

        Loan newLoan = null;
        Account account = this.repository.getAccount(accountNumber);
        Customer customer = account.getOwner();
        String userName = account.getOwner().getUserName();

        if (!customer.getPassword().equals(password)) {

            SessionService.getInstance().log().warn(
                    String.format("%s failed to log in when trying to get a new loan",
                            customer.getFirstName())
            );
            throw new FailedLoginException(String.format("Wrong password for account %d", accountNumber));
        }

        long internalLoansAmount = this.repository.getLoans(accountNumber)
                .stream()
                .mapToLong(l -> l.getAmount())
                .sum();


        long externalLoansAmount = getExternalLoans(customer.getFirstName(), customer.getLastName());

        long currentCredit = externalLoansAmount + internalLoansAmount;

        if (currentCredit + loanAmount < account.getCreditLimit()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(calendar.MONTH, 6);
            Date dueDate = calendar.getTime();

            newLoan = this.repository.createLoan(userName, loanAmount, dueDate);
            SessionService.getInstance().log().info(
                    String.format("new Loan accepted for %s (%d$), current credit: %d$",
                            customer.getFirstName(),
                            newLoan.getAmount(),
                            loanAmount + currentCredit)
            );
        } else {
            SessionService.getInstance().log().info(
                    String.format("new Loan refuse for %s (%d$), current credit: %d$",
                            customer.getFirstName(),
                            loanAmount,
                            currentCredit)
            );
        }

        return newLoan;
    }

    @Override
    public Account getAccount(String firstName, String lastName) {
        return this.repository.getAccount(firstName, lastName);
    }

    @Override
    public void delayPayment(Bank bank, int loanId, Date currentDueDate, Date newDueDate)
            throws RecordNotFoundException {

        //Note: in the context of this assignment, the current due date is not verified.
        this.repository.updateLoan(loanId, newDueDate);
        //Log inside the repository.
    }

    @Override
    public CustomerInfo[] getCustomersInfo(Bank bank) throws FailedLoginException {
        if (bank == SessionService.getInstance().getBank()) {
            CustomerInfo[] customersInfo = this.repository.getCustomersInfo();

            SessionService.getInstance().log().info(
                    String.format("Server returned %d customers info", customersInfo.length)
            );
            return customersInfo;
        } else {
            SessionService.getInstance().log().info(
                    String.format("Wrong bank: Info requested for bank: %1$s at bank server: %2$s",
                            bank,
                            SessionService.getInstance().getBank()
                    ));
            throw new FailedLoginException(
                    String.format("Wrong bank: Info requested for bank: %1$s at bank server: %2$s",
                            bank,
                            SessionService.getInstance().getBank()
                    ));
        }
    }


    //TODO: TEST AND CLEANUP!
    @Override
    public Loan transferLoan(int loanId, Bank currentBank, Bank otherBank) throws TransferException {

        Loan loan = this.repository.getLoan(loanId);
        if (loan == null)
        {
            String message = String.format("Loan #%1$d doesn't exist.", loanId);
            SessionService.getInstance().log().warn(message);
            throw new TransferException(message);
        }
        Customer customer = this.repository.getAccount(loan.getCustomerAccountNumber()).getOwner();

        Account externalAccount = getExternalAccount(otherBank, customer);

        if(externalAccount == null)
        {
            String message = String.format(
                    "Could not either get or create an acount at bank %1$s, for user %2$s.",
                    otherBank.name(),
                    customer.getUserName());

            SessionService.getInstance().log().error(message);
            throw new TransferException(message);
        }

        Loan externalLoan;
        try{
            LockFactory.getInstance().writeLock(customer.getUserName());
            externalLoan = createLoanAtBank(otherBank, externalAccount, loan);

            boolean isTransferred = false;
            if (externalLoan != null)
            {
                isTransferred = this.repository.deleteLoan(customer.getUserName(), loan.getLoanNumber());
            }
            else
            {
                String message = String.format(
                        "Could not create loan at bank %1$s, for current loan #%2$d.",
                        otherBank.name(),
                        loanId);

                SessionService.getInstance().log().error(message);
                throw new TransferException(message);
            }

            if (!isTransferred)
            {
                String message = String.format(
                        "Could not delete loan #%1$d AFTER transfer.",
                        loanId);

                SessionService.getInstance().log().error(message);
                throw new TransferException(message);
            }
        }
        finally{
            LockFactory.getInstance().writeUnlock(customer.getUserName());
        }


        return externalLoan;
    }

    private long getExternalLoans(String firstName, String lastName) {
        Bank currentBank = SessionService.getInstance().getBank();

        long externalLoans = 0;

        if (currentBank != Bank.National) {
            long externalLoan = getLoanAtBank(Bank.National, firstName, lastName);
            externalLoans += externalLoan;
        }
        if (currentBank != Bank.Royal) {
            long externalLoan = getLoanAtBank(Bank.Royal, firstName, lastName);
            externalLoans += externalLoan;
        }
        if (currentBank != Bank.Dominion) {
            long externalLoan = getLoanAtBank(Bank.Dominion, firstName, lastName);
            externalLoans += externalLoan;
        }
        return externalLoans;
    }

    private long getLoanAtBank(Bank bank, String firstName, String lastName) {

        long externalLoan = 0;
        try {

            Serializer getLoanMessageSerializer = new Serializer<GetLoanMessage>();
            Serializer getLoanSerializer = new Serializer<Long>();
            GetLoanMessage message = new GetLoanMessage(firstName, lastName);

            byte[] data = getLoanMessageSerializer.serialize(message);
            byte[] udpAnswer = this.udp.sendMessage(data, ServerPorts.getUDPPort(bank));

            externalLoan = (Long) getLoanSerializer.deserialize(udpAnswer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return externalLoan;
    }

    private Account getExternalAccount(Bank otherBank, Customer customer) {

        Account account = getAccountAtBank(otherBank, customer);

        if (account == null)
        {
            account = createAccountAtBank(otherBank, customer);
        }
        return account;
    }

    private Account getAccountAtBank(Bank bank, Customer customer) {

        Account externalAccount = null;
        try {

            Serializer getAccountMessageSerializer = new Serializer<GetAccountMessage>();
            Serializer getAccountSerializer = new Serializer<Account>();
            GetAccountMessage message = new GetAccountMessage(customer.getFirstName(), customer.getLastName());

            byte[] data = getAccountMessageSerializer.serialize(message);
            byte[] udpAnswer = this.udp.sendMessage(data, ServerPorts.getUDPPort(bank));

            externalAccount = (Account) getAccountSerializer.deserialize(udpAnswer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return externalAccount;
    }

    private Account createAccountAtBank(Bank bank, Customer customer) {

        Account externalAccount = null;
        try {

            Serializer createAccountMessageSerializer = new Serializer<CreateAccountMessage>();
            Serializer getAccountSerializer = new Serializer<Account>();
            CreateAccountMessage message = new CreateAccountMessage(customer);

            byte[] data = createAccountMessageSerializer.serialize(message);
            byte[] udpAnswer = this.udp.sendMessage(data, ServerPorts.getUDPPort(bank));

            externalAccount = (Account) getAccountSerializer.deserialize(udpAnswer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return externalAccount;
    }

    private Loan createLoanAtBank(Bank bank, Account externalAccount, Loan loan) {

        Loan externalLoan = null;
        try {

            Serializer createLoanMessageSerializer = new Serializer<CreateLoanMessage>();
            Serializer getLoanSerializer = new Serializer<Loan>();
            CreateLoanMessage message = new CreateLoanMessage(externalAccount, loan);

            byte[] data = createLoanMessageSerializer.serialize(message);
            byte[] udpAnswer = this.udp.sendMessage(data, ServerPorts.getUDPPort(bank));

            externalLoan = (Loan) getLoanSerializer.deserialize(udpAnswer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return externalLoan;
    }
}
