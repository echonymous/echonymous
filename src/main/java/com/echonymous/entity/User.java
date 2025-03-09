package com.echonymous.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email cannot be blank.")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Username cannot be blank.")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @JsonIgnore
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role_junction",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private Set<Role> roles;

    // Prolly add fields for createdAt, updatedAt
}
