package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.ContactPersonRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactPersonService {

    private final ContactPersonRepository contactPersonRepository;

    public ContactPersonService(ContactPersonRepository contactPersonRepository) {
        this.contactPersonRepository = contactPersonRepository;
    }
}
