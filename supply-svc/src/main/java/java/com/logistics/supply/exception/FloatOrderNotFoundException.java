package java.com.logistics.supply.exception;

public class FloatOrderNotFoundException extends NotFoundException {

    public FloatOrderNotFoundException(int floatOrderId) {
        super("Float order with id: %s not found".formatted(floatOrderId));
    }

    public FloatOrderNotFoundException(String floatOrderRef) {
        super("Float order with id: %s not found".formatted(floatOrderRef));
    }
}
