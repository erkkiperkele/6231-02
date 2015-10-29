package Data;

public enum ServerPorts {

    CustomerRMI,
    ManagerRMI,
    UDP;

    /**
     * maps a port name to its actual local port.
     * @return
     */
    public int getRMIPort() {
        switch (this) {
            case CustomerRMI:
                return 4242;

            case ManagerRMI:
                return 4343;

            case UDP:
                return 4444;

            default:
                return 0;
        }
    }

    /**
     * maps each bank to a local port.
     * @param bank
     * @return
     */
    public static int getRMIPort(Bank bank) {
        switch (bank) {
            case Royal:
                return 4242;

            case National:
                return 4243;

            case Dominion:
                return 4244;

            default:
                return 0;
        }
    }


    /**
     * maps each bank to another local port for its UDP server.
     * @param bank
     * @return
     */
    public static int getUDPPort(Bank bank) {
        switch (bank) {
            case Royal:
                return 4245;

            case National:
                return 4246;

            case Dominion:
                return 4247;

            default:
                return 0;
        }
    }
}
