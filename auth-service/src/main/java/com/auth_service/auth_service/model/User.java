package com.fintech.fin_tech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();


    public User(String username, String password, String email, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;

    }

    /**
     * TODO cascade = CascadeType.ALL projeye göre ileride güncellenebilir
     * cascade = CascadeType.ALL: Bir  User kaydedildiğinde/güncellendiğinde silindiğinde ilişkili Transaction'lar da etkilenir)
     * mappedBy = "user" ilişkinin Transaction entity'sindeki user alanı tarafından yönetildiğini belirtir.
     * orphanRemoval = true: Bir Transaction User'ın transactions setinden çıkarıldığında, o Transaction'ın veritabanından da silinmesini sağlar.
     */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

}
