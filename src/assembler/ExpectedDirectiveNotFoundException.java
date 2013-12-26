package assembler;

public class ExpectedDirectiveNotFoundException extends Exception {
    public ExpectedDirectiveNotFoundException(String errMsg) {
        super(errMsg);
    }
}
