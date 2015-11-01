package Presentation;

import Contracts.ICustomerService;
import Data.Bank;
import Data.Customer;
import Data.Loan;
import Helpers.Console;
import Services.CustomerService;
import Services.SessionService;

import javax.security.auth.login.FailedLoginException;

/**
 * A simple console project to provide a UI to customers
 * in order to access the Banks API
 */
public class CustomerConsole {

    private static ICustomerService customerService;
    private static Console console;

    public static void main(String[] args) {

        customerService = new CustomerService();
        console = new Console(System.in);

        boolean isExiting = false;

        while (!isExiting) {
            displayChoices();
            isExiting = executeChoice();
        }

    }

    private static void displayChoices() {

        String message = String.format(
                "Please chose an option:"
                        + "%1$s 1: Sign in"
                        + "%1$s 2: Open an account"
                        + "%1$s 3: Get Loan"
                        + "%1$s 4: Transfer Loan"
                        + "%1$s Press any other key to exit."
                , console.newLine());

        console.println(message);
    }

    private static boolean executeChoice() {

        char choice = console.readChar();
        boolean isExiting = false;

        switch (choice) {
            case '1':
                displaySignin();
                break;
            case '2':
                displayOpenAccount();
                break;
            case '3':
                displayGetLoan();
                break;
            case '4':
                displayTransferLoan();
                break;
            default:
                console.println("See you!");
                isExiting = true;
                break;
        }
        return isExiting;
    }

    private static void displayGetLoan() {

        displaySignInIfNeeded();

        Customer customer = SessionService.getInstance().getCurrentCustomer();
        long loanAmount = askLoanAmount();

        SessionService.getInstance().log().info(String.format("Requested a loan of %1$s $", loanAmount));
        Loan newLoan = getLoan(customer.getBank(), customer.getAccountNumber(), customer.getPassword(), loanAmount);

        if (newLoan != null && newLoan.getAmount() > 0) {
            SessionService.getInstance().log().info(
                    String.format("New loan (#%1$d) granted for an amount of %2$s $",
                            newLoan.getLoanNumber(),
                            newLoan.getAmount())
            );
        } else {
            SessionService.getInstance().log().warn(
                    String.format("New loan refused for an amount of %1$s $ (check your credit line)", loanAmount)
            );
        }
    }

    private static void displayTransferLoan() {

        displaySignInIfNeeded();
        Customer customer = SessionService.getInstance().getCurrentCustomer();

        int loanId = askLoanId();
        Bank currentBank = customer.getBank();
        Bank newBank = askNewBank();


        SessionService.getInstance().log().info(
                String.format("Requested to transfer loan #%1$d from %2$s to %3$s", loanId, currentBank, newBank)
        );
        Loan newLoan = transferLoan(loanId, currentBank, newBank);

        if (newLoan != null) {
            SessionService.getInstance().log().info(
                    String.format("loan #%1$s successfully transferred to bank %2$s as loan #%3$s",
                            loanId,
                            newBank.name(),
                            newLoan.getLoanNumber())
            );
        } else {
            SessionService.getInstance().log().error(
                    String.format("An error has occurred while transferring the loan #%1$s to bank %2$s, please try again.",
                            loanId,
                            newBank.name())
            );
        }
    }

    private static void displaySignin() {
        Bank bank = askBankId();
        String email = askEmail();
        String password = askPassword();

        try {
            Customer customer = customerService.signIn(bank, email, password);
            SessionService.getInstance().setCurrentCustomer(customer);

        } catch (FailedLoginException e) {
            SessionService.getInstance().log().error("Wrong email or password, please try again.");
            displaySignin();
        }

    }

    private static void displaySignInIfNeeded() {
        boolean isLoggedIn = SessionService.getInstance().isLoggedIn();
        if (!isLoggedIn) {
            console.println("Please sign in before to process.");
            displaySignin();
        }
    }

    private static void displayOpenAccount() {

        Bank bank = askBankId();
        String firstName = askFirstName();
        String lastName = askLastName();
        String email = askEmail();
        String phone = askPhone();
        String password = askPassword();

        SessionService.getInstance().log().info("Requesting server to open a new account:");

        int accountNumber = openAccount(bank, firstName, lastName, email, phone, password);
        SessionService.getInstance().log().info(String.format("Account #%d created.", accountNumber));

        try {
            Customer newCustomer = customerService.getCustomer(bank, email, password);
            SessionService.getInstance().setCurrentCustomer(newCustomer);
        } catch (FailedLoginException e) {
            //Should not happen, account was created with this email.
        }
    }

    private static Bank askBankId() {

        console.println("Enter bankId");
        console.println(String.format("(1 - %1$s, 2 - %2$s, 3 %3$s): ", Bank.Royal, Bank.National, Bank.Dominion));

        int userAnswer = 0;

        try {
            userAnswer = console.readint();
        } catch (NumberFormatException e) {
            console.println("Wrong value answered. Please chose one of the given options.");
            askBankId();
        }

        Bank answer = userAnswer == 0
                ? Bank.Royal
                : Bank.fromInt(userAnswer);

        displayAnswer(answer.toString());
        return answer;
    }

    private static String askFirstName() {

        console.println("Enter firstName: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "Aymeric"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static String askLastName() {

        console.println("Enter lastName: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "Grail"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static String askEmail() {
        console.println("Enter email: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "Aymeric.Grail@gmail.com"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static String askPhone() {
        console.println("Enter phone: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "514.660.2812"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static String askPassword() {
        console.println("Enter password: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "zaza"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static long askLoanAmount() {
        console.println("Enter required amount for the loan: ");
        String userAnswer = console.readString();

        long answer = 0;
        try {
            answer = userAnswer.equals("")
                    ? 100
                    : Long.parseLong(userAnswer);

        } catch (NumberFormatException e) {
            console.println("Wrong value answered. Please enter a valid amount.");
            askLoanAmount();
        }
        displayAnswer(String.valueOf(answer));
        return answer;
    }

    private static int askLoanId() {
        console.println("Enter loan number");
        int answer = 0;
        try {

            int userAnswer = console.readint();
            answer = userAnswer == 0
                    ? 2
                    : userAnswer;

        } catch (NumberFormatException e) {
            console.println("Wrong value. Please enter a valid Number.");
            askLoanId();
        }

        displayAnswer(String.valueOf(answer));
        return answer;
    }

    private static Bank askNewBank() {

        console.println("Chose the bank where to transfer the loan");
        console.println(String.format("(1 - %1$s, 2 - %2$s, 3 %3$s): ", Bank.Royal, Bank.National, Bank.Dominion));

        Bank answer = Bank.None;
        try {
            int userAnswer = console.readint();
            answer = userAnswer == 0
                    ? Bank.National
                    : Bank.fromInt(userAnswer);

        } catch (NumberFormatException e) {
            console.println("Wrong value. Please enter a valid bank.");
            askNewBank();
        }


        displayAnswer(answer.toString());
        return answer;
    }

    private static int openAccount(
            Bank bankId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String password) {

        int accountNumber = customerService.openAccount(bankId, firstName, lastName, email, phone, password);
        return accountNumber;
    }

    private static Loan getLoan(
            Bank bankId,
            int accountNumber,
            String password,
            long loanAmount) {
        return customerService.getLoan(bankId, accountNumber, password, loanAmount);
    }

    private static Loan transferLoan(int loanId, Bank currentBank, Bank newBank) {
        return customerService.transferLoan(loanId, currentBank, newBank);
    }

    private static void displayAnswer(String answer) {
        console.println("Value Entered: " + answer + console.newLine());
    }
}
