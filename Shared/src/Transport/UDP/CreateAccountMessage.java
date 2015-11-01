package Transport.UDP;

import Data.Customer;

public class CreateAccountMessage {


    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public CreateAccountMessage(Customer customer) {

        this.customer = customer;
    }
}
