package com.dusanbranovic.bookme.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRegisterRequest registerRequest){
        return authenticationService.register(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLoginRequest loginRequest){
        return authenticationService.login(loginRequest);
    }
}
