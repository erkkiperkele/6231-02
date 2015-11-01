package Presentation;

import Contracts.IManagerService;
import Data.Bank;
import Data.Customer;
import Data.CustomerInfo;
import Helpers.Console;
import Services.ManagerService;
import Services.SessionService;

import javax.security.auth.login.FailedLoginException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple console project to provide a UI to managers
 * in order to access the Banks API
 */
public class ManagerConsole {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static IManagerService managerService;
    private static Console console;

    public static void main(String[] args) {

        managerService = new ManagerService();
        console = new Console(System.in);

        boolean isExiting = false;

        displaySignin();

        while (!isExiting) {
            displayChoices();
            isExiting = executeChoice();
        }

    }

    private static void displayChoices() {

        String message = String.format(
                "%1$s Please chose an option:"
                        + "%1$s 1: Delay Payment"
                        + "%1$s 2: Print Customers Info"
                        + "%1$s Press any other key to exit.",
                console.newLine());

        console.println(message);
    }

    private static boolean executeChoice() {

        char choice = console.readChar();
        boolean isExiting = false;

        switch (choice) {
            case '1':
                displayDelayPayment();
                break;
            case '2':
                displayPrintCustomersInfo();
                break;
            default:
                console.println("See you!");
                isExiting = true;
                break;
        }
        return isExiting;
    }

    private static void displayDelayPayment() {

        Bank bank = askBankId();
        int loanId = askLoanId();
        Date currentDate = askCurrentDate();
        Date newDate = askNewDate();

        managerService.delayPayment(bank, loanId, currentDate, newDate);
    }

    private static void displayPrintCustomersInfo() {

        Bank bank = askBankId();

        CustomerInfo[] customersInfo = managerService.getCustomersInfo(bank);

        for (CustomerInfo info : customersInfo) {
            System.out.println(info.toString());
        }

        SessionService.getInstance().log().info(
                String.format("Manager printed %d customers info for bank: %s", customersInfo.length, bank)
        );
    }

    private static void displaySignin() {
        Bank bank = askBankId();
        String email = askUserName();
        String password = askPassword();

        try {
            Customer customer = managerService.signIn(bank, email, password);
            SessionService.getInstance().setCurrentCustomer(customer);

        } catch (FailedLoginException e) {
            displaySignin();
        }

    }

    private static Bank askBankId() {

        console.println("Enter bankId");
        console.println(String.format("(1 - %1$s, 2 - %2$s, 3 %3$s): ", Bank.Royal, Bank.National, Bank.Dominion));
        int userAnswer = console.readint();
        Bank answer = userAnswer == 0
                ? Bank.Royal
                : Bank.fromInt(userAnswer);

        displayAnswer(answer.toString());
        return answer;
    }

    private static String askUserName() {
        console.println("Enter userName: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "Manager"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static String askPassword() {
        console.println("Enter password: ");
        String userAnswer = console.readString();
        String answer = userAnswer.equals("")
                ? "manager"
                : userAnswer;

        displayAnswer(answer);
        return answer;
    }

    private static int askLoanId() {
        console.println("Enter loanId");
        int userAnswer = console.readint();
        int answer = userAnswer == 0
                ? 2
                : userAnswer;

        displayAnswer(String.valueOf(answer));
        return answer;
    }

    private static Date askCurrentDate() {

        console.println(String.format("Enter loan's current date (%s)", DATE_FORMAT));

        Date answer = getDate("01-12-2015");
        return answer;
    }

    private static Date askNewDate() {

        console.println(String.format("Enter loan's new date (%s)", DATE_FORMAT));

        Date answer = getDate("01-12-2016");
        return answer;
    }

    private static Date getDate(String defaultDate) {
        Date answer = null;

        try {

            String userAnswer = console.readString();
            userAnswer = userAnswer.equals("")
                    ? defaultDate
                    : userAnswer;

            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            answer = dateFormat.parse(userAnswer);
            displayAnswer(dateFormat.format(answer));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return answer;
    }

    private static void displayAnswer(String answer) {
        console.println("Value Entered: " + answer + console.newLine());
    }
}
