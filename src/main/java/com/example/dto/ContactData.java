package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContactData {
    private Integer primaryContactId;
    private List<String> emails;
    private List<String> phoneNumbers;
    private List<Integer> secondaryContactIds;
}
