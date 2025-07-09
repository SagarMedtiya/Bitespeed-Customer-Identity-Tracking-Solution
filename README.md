# Bitespeed Customer Identity Tracking Solution

A Spring Boot application that identifies and tracks customer identities across multiple purchases using email and phone number matching.

## Features

- Identity resolution via email/phone matching
- Primary/secondary contact linking
- Contact merging logic
- REST API endpoint for identity resolution

## API Documentation

### Base URL
`https://bitespeed-customer-identity-tracking.onrender.com/api/identity` 

### Endpoints

#### POST `/identify`
Identify or create a customer contact

**Request:**
```json
{
  "email" : "test@example.com",
    "phoneNumber" : "000008"
}
```
## Test Cases

### Test Case 1: Create New Primary Contact
**Request**:
```json
POST /api/identify
{
    "email": "test1@example.com",
    "phoneNumber": "1111111111"
}
```
Expected Response:

```json

{
    "contact": {
        "primaryContactId": 1,
        "emails": ["test1@example.com"],
        "phoneNumbers": ["1111111111"],
        "secondaryContactIds": []
    }
}
```
![Create New Primary Contact](screenshot/Create_Contact.png)
### Test Case 2: Link Secondary Contact (Same Phone)

Request:

```json

POST /api/identify
{
    "email": "test2@example.com",
    "phoneNumber": "1111111111"
}
```
Expected Response:

```json

{
    "contact": {
        "primaryContactId": 1,
        "emails": ["test1@example.com", "test2@example.com"],
        "phoneNumbers": ["1111111111"],
        "secondaryContactIds": [2]
    }
}
```
![Link Secondary Contact (Same Phone)](screenshot/Add_the_secondary_contact(same_phone).png)
### Test Case 3: Link Secondary Contact (Same Email)

Request:

```json

POST /api/identify
{
    "email": "test1@example.com",
    "phoneNumber": "2222222222"
}
```
Expected Response:

```json

{
    "contact": {
        "primaryContactId": 1,
        "emails": ["test1@example.com"],
        "phoneNumbers": ["1111111111", "2222222222"],
        "secondaryContactIds": [2, 3]
    }
}
```
![Link Secondary Contact (Same Email)](screenshot/Add_the_secondary_contact(same_email).png)
### Test Case 4: Merge Two Primary Contacts

Step 1: Create first primary contact

```json

POST /api/identify
{
    "email": "george@hillvalley.edu",
    "phoneNumber": "919191"
}
```
![Merge Two Primary Contacts](screenshot/merge1.png)
Step 2: Create second primary contact

```json

POST /api/identify
{
    "email": "biffsucks@hillvalley.edu",
    "phoneNumber": "717171"
}
```
![Merge Two Primary Contacts](screenshot/merge2.png)
Step 3: Merge them

```json

POST /api/identify
{
    "email": "george@hillvalley.edu",
    "phoneNumber": "717171"
}
```
![Merge Two Primary Contacts](screenshot/merge3.png)
Expected Response:

```json

{
    "contact": {
        "primaryContactId": 1,
        "emails": ["george@hillvalley.edu", "biffsucks@hillvalley.edu"],
        "phoneNumbers": ["919191", "717171"],
        "secondaryContactIds": [2]
    }
}
```
### Test Case 5: Partial Information

Request with only email:

```json

POST /api/identify
{
    "email": "mcfly@hillvalley.edu",
    "phoneNumber": null
}
```
Request with only phone:

```json

POST /api/identify
{
    "email": null,
    "phoneNumber": "123456"
}
```
![Partial Information](screenshot/partial.png)
