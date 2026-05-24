package com.dusanbranovic.bookme.integrations.repository;

import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setup(){
        User user = new User();
        user.setEmail("guest1@test.com");
        userRepository.save(user);
    }

    @Test
    @DisplayName("Test if the DB is finding User by email")
    void findByEmailTest(){
        String email = "guest1@test.com";


        Optional<User> optionalUser = userRepository.findByEmail(email);
        assertTrue(optionalUser.isPresent());
        assertEquals(email,optionalUser.get().getEmail());
    }

}