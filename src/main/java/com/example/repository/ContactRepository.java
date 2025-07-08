package com.example.repository;

import com.example.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository  extends JpaRepository<Contact, Integer> {
    @Query("SELECT c FROM Contact c WHERE "+
    "(c.email = :email OR :email IS NULL) OR "+
    "(c.phoneNumber = :phoneNumber OR :phoneNumber IS NULL)" +
    "AND c.deletedAt IS NULL")
    List<Contact> findByEmailOrPhoneNumber(@Param("email") String email,
                                           @Param("phoneNumber") String phoneNumber);

    @Query("SELECT c FROM Contact c WHERE " +
    "c.linkedId = :linkedId OR c.id = :linkedId "+
    "AND c.deletedAt IS NULL")
    List<Contact> findByLinkedIdOrId(@Param("linkedId") Integer linkedId);
}
