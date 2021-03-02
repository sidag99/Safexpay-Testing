package Functions;

public class ErrorMessageException extends Exception {
    String str;
    public ErrorMessageException() {

    }
    @Override
    public String getMessage() {
        return "Red Error Message appeared";
    }
}
