package Transport.UDP;

import Contracts.ICustomerService;
import Data.Account;
import Data.Loan;
import Data.ServerPorts;
import Services.SessionService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class UDPServer {
    private ICustomerService customerService;

    public UDPServer(ICustomerService customerService) {

        this.customerService = customerService;
    }

    public void startServer() {
        System.out.println(String.format("UDP Server is starting"));


        DatagramSocket aSocket = null;
        try {

            //Setup the socket
            int serverPort = ServerPorts.getUDPPort(SessionService.getInstance().getBank());
            aSocket = new DatagramSocket(serverPort);
            byte[] buffer = new byte[1000];

            //Setup the loop to process request
            while (true) {
                System.err.println(String.format("UDP Server STARTED"));

                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                System.out.println(String.format("UDP Server received Message"));

                byte[] message = request.getData();
                byte[] answer = processMessage(message);
                int answerPort = request.getPort();

                DatagramPacket reply = new DatagramPacket(
                        answer,
                        answer.length,
                        request.getAddress(),
                        answerPort
                );
                aSocket.send(reply);

                System.out.println(String.format("UDP Server Answered Message on port: %d", answerPort));
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }

    }

    /**
     * Upon reception of a GetLoanMessage() the udp server will calculate the current
     * Credit line of the mentioned customer and return it to the sender of the request.
     * returns 0 if Customer has no account at the requested bank.
     * @param message a serialized GetLoanMessage() Please use the serializer provided to ensure message is valid.
     * @return a serialized long indicating the current credit line at this bank for the given customer.
     * @throws IOException
     */
    private byte[] processMessage(byte[] message) throws IOException {

        long currentLoanAmount = 0;
        Serializer loanMessageSerializer = new Serializer<GetLoanMessage>();
        Serializer loanSerializer = new Serializer<Loan>();

        try {

            GetLoanMessage loanMessage = (GetLoanMessage) loanMessageSerializer.deserialize(message);

            Account account = this.customerService.getAccount(loanMessage.getFirstName(), loanMessage.getLastName());

            if (account != null) {
                List<Loan> loans = this.customerService.getLoans(account.getAccountNumber());

                currentLoanAmount = loans
                        .stream()
                        .mapToLong(l -> l.getAmount())
                        .sum();

                System.out.println(String.format("Loan amount %d", currentLoanAmount));
            } else {
                SessionService.getInstance().log().info(
                        String.format("%1$s %2$s doesn't have a credit record at our bank",
                                loanMessage.getFirstName(),
                                loanMessage.getLastName())
                );
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        byte[] serializedLoan = loanSerializer.serialize(currentLoanAmount);
        return serializedLoan;
    }
}
