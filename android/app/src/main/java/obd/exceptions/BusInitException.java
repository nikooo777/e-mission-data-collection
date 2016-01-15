package obd.exceptions;

/**
 * Thrown when there is a "BUS INIT... ERROR" message
 */
public class BusInitException extends ResponseException {

    public BusInitException() {
        super("BUS INIT... ERROR");
    }

}
