package com.logistics.supply.fixture;

import com.logistics.supply.dto.LoginRequest;

public class LoginRequestFixture {

     LoginRequestFixture() {
    }

    public static LoginRequest getLoginRequest() {
        return new LoginRequest("kikinewton@gmail.com", "password1.com");
    }
}
