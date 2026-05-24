package com.dusanbranovic.bookme.auth;

import com.dusanbranovic.bookme.config.JwtService;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidCredentialsException;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.models.UserType;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            AuthenticationManager authenticationManager
    ) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(AuthRegisterRequest registerRequest) {

        User user = new User(
                registerRequest.getUserType(),
                registerRequest.getEmail(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getPhoneNumber()
        );

        System.out.println(registerRequest.getEmail());
        System.out.println(user);

        User savedUser = userRepository.save(user);

        System.out.println(savedUser);

        String jwt = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(savedUser.getId(), savedUser.getRole(), jwt);

    }

    public AuthResponse login(AuthLoginRequest loginRequest) {


//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword())
//            );

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Translate the Spring Security exception into your custom one
            throw new InvalidCredentialsException("Incorrect email or password");
        }

        // Use your existing EntityNotFoundException here as well!
        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + loginRequest.getEmail()));

//            var user = userRepository.findByEmail(loginRequest.getEmail())
//                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            var jwt = jwtService.generateToken(user.getEmail());
            return new AuthResponse(user.getId(),user.getRole(),jwt);

    }
}
