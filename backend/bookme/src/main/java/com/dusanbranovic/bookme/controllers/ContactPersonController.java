package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.service.ContactPersonService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact-persons")
public class ContactPersonController {

    private final ContactPersonService contactPersonService;

    public ContactPersonController(ContactPersonService contactPersonService) {
        this.contactPersonService = contactPersonService;
    }
}
