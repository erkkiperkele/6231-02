package Transport.UDP;

import Data.Account;
import Data.Loan;

public class CreateLoanMessage {

    private Account account;
    private Loan loan;

    public CreateLoanMessage(Account account, Loan loan) {
        this.account = account;
        this.loan = loan;
    }

    public Account getAccount() {
        return account;
    }

    public Loan getLoan() {
        return loan;
    }
}
