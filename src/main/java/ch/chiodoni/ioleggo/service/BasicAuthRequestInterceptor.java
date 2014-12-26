package ch.chiodoni.ioleggo.service;

import retrofit.RequestInterceptor;

import java.util.Base64;

public class BasicAuthRequestInterceptor implements RequestInterceptor {

    private String authorization;

    public BasicAuthRequestInterceptor(String username, String password) {
        this.authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Override
    public void intercept(RequestInterceptor.RequestFacade requestFacade) {
        requestFacade.addHeader("Authorization", authorization);
    }

}
