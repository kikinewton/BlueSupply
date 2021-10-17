package com.logistics.supply;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

public class PasswordEncoderTest {

    @Test
    void bcryptPasswordEncoder() {
        BCryptPasswordEncoder encoder4 = new BCryptPasswordEncoder(4);
        System.out.println("appclient's Bcrypt encrypted password = " + encoder4.encode("BlueSupplyPass12!@"));
    }

    @Test
    void generateBase64EncodedValue() {

        // Get the Base64 password for appclient:appclient@123
        String base64AuthHeader = Base64.getEncoder().encodeToString("appclient:BlueSupplyPass12!@".getBytes());

        // This Base64 password for appclient:appclient@123 will be used in the http
        // header when requesting the token
        System.out.println("appclient's Base64 Password is " + base64AuthHeader);
    }
}
