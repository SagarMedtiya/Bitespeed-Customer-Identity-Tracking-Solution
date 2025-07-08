package com.example.service;

import com.example.entity.Contact;
import com.example.entity.LinkPrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dto.ContactResponse;
import com.example.repository.ContactRepository;
import com.example.dto.ContactRequest;
import com.example.dto.ContactData;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public ContactResponse processContact(ContactRequest request) {
        // Find all contacts matching the email or phone number
        List<Contact> matchingContacts = contactRepository
                .findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

        if (matchingContacts.isEmpty()) {
            // Create new primary contact if no matches found
            Contact newContact = createNewPrimaryContact(request);
            return createResponseFromPrimaryContact(newContact);
        }

        // Process existing contacts
        return processExistingContacts(request, matchingContacts);
    }

    private ContactResponse processExistingContacts(ContactRequest request, List<Contact> matchingContacts) {
        // Find the oldest primary contact among matches
        Contact primaryContact = findPrimaryContact(matchingContacts);

        // Check if we need to create a new secondary contact
        boolean shouldCreateSecondary = shouldCreateSecondaryContact(request, matchingContacts);

        if (shouldCreateSecondary) {
            Contact newSecondary = createNewSecondaryContact(request, primaryContact.getId());
            matchingContacts.add(newSecondary);
        }

        // Check if any primary contacts need to be converted to secondary
        convertPrimaryToSecondaryIfNeeded(matchingContacts, primaryContact);

        // Get all contacts linked to the primary
        List<Contact> allLinkedContacts = contactRepository
                .findByLinkedIdOrId(primaryContact.getId(), primaryContact.getId());

        return createResponseFromContacts(primaryContact, allLinkedContacts);
    }

    private boolean shouldCreateSecondaryContact(ContactRequest request, List<Contact> existingContacts) {
        boolean emailExists = existingContacts.stream()
                .anyMatch(c -> request.getEmail() != null && request.getEmail().equals(c.getEmail()));

        boolean phoneExists = existingContacts.stream()
                .anyMatch(c -> request.getPhoneNumber() != null && request.getPhoneNumber().equals(c.getPhoneNumber()));

        // Create secondary if at least one field matches but not all
        return (request.getEmail() != null && !emailExists) ||
                (request.getPhoneNumber() != null && !phoneExists);
    }

    private Contact findPrimaryContact(List<Contact> contacts) {
        // Find the oldest primary contact
        Optional<Contact> primary = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt));

        if (primary.isPresent()) {
            return primary.get();
        }

        // If no primary found (shouldn't happen), find the oldest contact and make it primary
        Contact oldest = contacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        oldest.setLinkPrecedence(LinkPrecedence.PRIMARY);
        oldest.setLinkedId(null);
        oldest.setUpdatedAt(LocalDateTime.now());
        contactRepository.save(oldest);

        return oldest;
    }

    private void convertPrimaryToSecondaryIfNeeded(List<Contact> contacts, Contact primaryContact) {
        List<Contact> primariesToConvert = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .filter(c -> !c.getId().equals(primaryContact.getId()))
                .collect(Collectors.toList());

        for (Contact contact : primariesToConvert) {
            contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
            contact.setLinkedId(primaryContact.getId());
            contact.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(contact);
        }
    }

    private Contact createNewPrimaryContact(ContactRequest request) {
        Contact contact = new Contact();
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setLinkPrecedence(LinkPrecedence.PRIMARY);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contactRepository.save(contact);
    }

    private Contact createNewSecondaryContact(ContactRequest request, Integer primaryId) {
        Contact contact = new Contact();
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
        contact.setLinkedId(primaryId);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contactRepository.save(contact);
    }

    private ContactResponse createResponseFromPrimaryContact(Contact primary) {
        ContactResponse response = new ContactResponse();
        ContactData contactData = new ContactData();

        contactData.setPrimaryContactId(primary.getId());
        contactData.setEmails(primary.getEmail() != null ?
                Collections.singletonList(primary.getEmail()) : Collections.emptyList());
        contactData.setPhoneNumbers(primary.getPhoneNumber() != null ?
                Collections.singletonList(primary.getPhoneNumber()) : Collections.emptyList());
        contactData.setSecondaryContactIds(Collections.emptyList());

        response.setContact(contactData);
        return response;
    }

    private ContactResponse createResponseFromContacts(Contact primary, List<Contact> allContacts) {
        ContactResponse response = new ContactResponse();
        ContactData contactData = new ContactData();

        contactData.setPrimaryContactId(primary.getId());

        // Collect all unique emails (primary first)
        Set<String> emails = new LinkedHashSet<>();
        if (primary.getEmail() != null) {
            emails.add(primary.getEmail());
        }
        allContacts.stream()
                .filter(c -> c.getEmail() != null && !c.getId().equals(primary.getId()))
                .map(Contact::getEmail)
                .forEach(emails::add);
        contactData.setEmails(new ArrayList<>(emails));

        // Collect all unique phone numbers (primary first)
        Set<String> phoneNumbers = new LinkedHashSet<>();
        if (primary.getPhoneNumber() != null) {
            phoneNumbers.add(primary.getPhoneNumber());
        }
        allContacts.stream()
                .filter(c -> c.getPhoneNumber() != null && !c.getId().equals(primary.getId()))
                .map(Contact::getPhoneNumber)
                .forEach(phoneNumbers::add);
        contactData.setPhoneNumbers(new ArrayList<>(phoneNumbers));

        // Collect all secondary contact IDs
        List<Integer> secondaryIds = allContacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .collect(Collectors.toList());
        contactData.setSecondaryContactIds(secondaryIds);

        response.setContact(contactData);
        return response;
    }


}
