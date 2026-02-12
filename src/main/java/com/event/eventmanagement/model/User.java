package com.event.eventmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private String name;

    // ----- Constructors -----

    public User() {
    }

    // ----- Getters and Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {   // ✅ IMPORTANT
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {   // ✅ IMPORTANT
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {       // ✅ IMPORTANT
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {       // Optional
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
