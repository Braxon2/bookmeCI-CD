package com.dusanbranovic.bookme.mappers;

import com.dusanbranovic.bookme.dto.responses.GuestSummaryDTO;
import com.dusanbranovic.bookme.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public GuestSummaryDTO toDTO(User user){

        return new GuestSummaryDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber());

    }

    public User toEntity(GuestSummaryDTO dto){

        User user = new User();
        user.setId(dto.id());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());
        return user;
    }

}
