package com.logistics.supply.util;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class Helper {

  public String getAccessToken(String username, String pass, String host, String authCode)
      throws UnirestException {
    try {
      String token = "";
      String refreshToken = "";

      String tokenUrl = host + "/oauth/token";
      JSONObject jsonResponse = null;
      try {
        System.out.println(tokenUrl);
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response =
            Unirest.post(tokenUrl)
                .queryString("grant_type", "password")
                .queryString("username", username.trim())
                .queryString("password", pass.trim())
                .header("Authorization", "Basic " + authCode.trim())
                .asString();

        jsonResponse = new JSONObject(response.getBody());
      } catch (UnirestException e) {

        e.printStackTrace();
      }
      if (jsonResponse.has("access_token")) {
        token = jsonResponse.getString("access_token");
        refreshToken = jsonResponse.getString("refresh_token");
        return token + "," + refreshToken;
      } else if (jsonResponse.has("error")) {
        System.out.println("jsonResponse = " + jsonResponse);
        System.out.println("=================> " + "ERROR FOUND");
        token = "invalid_grant";
        return token;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return "";
  }
}
