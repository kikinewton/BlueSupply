package java.com.logistics.supply.exception;

public class GrnNotFoundException extends NotFoundException {

    public GrnNotFoundException(int grnId) {
        super("GRN with id: %s not found".formatted(grnId));
    }


}
