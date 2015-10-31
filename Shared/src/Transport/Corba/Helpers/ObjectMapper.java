package Transport.Corba.Helpers;

import Data.CustomerInfo;
import Transport.Corba.BankServerPackage.Bank;
import Transport.Corba.BankServerPackage.Customer;
import Transport.Corba.BankServerPackage.Loan;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Calendar;
import java.util.Date;

public class ObjectMapper {

    public static Transport.Corba.BankServerPackage.Bank toCorbaBank(Data.Bank bank) {

        Transport.Corba.BankServerPackage.Bank corbaBank = null;
        try {
            corbaBank = ObjectMapper.mapToCorbaObject(bank);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return corbaBank;
    }

    public static Transport.Corba.BankServerPackage.Date toCorbaDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new Transport.Corba.BankServerPackage.Date(cal.YEAR, cal.MONTH, cal.DAY_OF_MONTH);
    }

    public static Customer toCorbaCustomer(Data.Customer customer) {
        return new Customer(
                (short)customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                toCorbaBank(customer.getBank()),
                (short)customer.getAccountNumber(),
                customer.getPhone(),
                customer.getPassword()
        );
    }

    public static Data.Customer toCustomer(Transport.Corba.BankServerPackage.Customer corbaCustomer) {

        Data.Customer customer = null;
        try {
            customer = ObjectMapper.mapToClientObject(corbaCustomer);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return customer;
    }

    public static Data.Loan toLoan(Transport.Corba.BankServerPackage.Loan corbaLoan) {

        Data.Loan loan = null;
        try {
            loan = ObjectMapper.mapToClientObject(corbaLoan);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return loan;
    }

    public static Data.Bank toBank(Transport.Corba.BankServerPackage.Bank corbaBank) {

        Data.Bank bank = null;
        try {
            bank = ObjectMapper.mapToClientObject(corbaBank);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return bank;
    }

    public static CustomerInfo[] toCustomersInfo(String customersInfo) {
        throw new NotImplementedException();
    }

    private static Bank mapToCorbaObject(Data.Bank source) throws ObjectMappingException {
        Bank destination = Bank.None;

        switch (source) {
            case Dominion:
                destination = Bank.Dominion;
                break;
            case National:
                destination = Bank.National;
                break;
            case Royal:
                destination = Bank.Royal;
                break;
            case None:
                destination = Bank.None;
                break;
            default:
                throw new ObjectMappingException(
                        "This Bank is not implemented in the Corba Interface (BankServer.idl)"
                );
        }
        return destination;
    }

    private static Data.Bank mapToClientObject(Bank source) throws ObjectMappingException {
        Data.Bank destination = Data.Bank.None;


        switch (source.toString()) {
            case "Dominion":
                destination = Data.Bank.Dominion;
                break;
            case "National":
                destination = Data.Bank.National;
                break;
            case "Royal":
                destination = Data.Bank.Royal;
                break;
            case "None":
                destination = Data.Bank.None;
                break;
            default:
                throw new ObjectMappingException(
                        "This Bank is not implemented in the Corba Interface (BankServer.idl)"
                );
        }
        return destination;
    }

    private static Data.Customer mapToClientObject(Customer corbaCustomer) throws ObjectMappingException {

        return new Data.Customer(
                corbaCustomer.id,
                corbaCustomer.accountNumber,
                corbaCustomer.firstName,
                corbaCustomer.lastName,
                corbaCustomer.password,
                mapToClientObject(corbaCustomer.bank),
                corbaCustomer.email,
                corbaCustomer.phone
        );
    }

    private static Data.Loan mapToClientObject(Loan corbaLoan) throws ObjectMappingException {

        return new Data.Loan(
                corbaLoan.loanNumber,
                corbaLoan.customerAccountNumber,
                corbaLoan.amount,
                mapToClientObject(corbaLoan.dueDate)
        );
    }

    private static Date mapToClientObject(Transport.Corba.BankServerPackage.Date corbaDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH, corbaDate.day);
        calendar.set(Calendar.MONTH, corbaDate.month);
        calendar.set(Calendar.YEAR, corbaDate.year);
        return calendar.getTime();
    }
}
