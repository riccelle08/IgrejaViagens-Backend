package br.com.viagensigreja.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String cpf;

    private String name;
    private String password;
    private String role;
    private String birthdate;
    private boolean firstLogin;

    private boolean married;
    private String spouseName;

    private boolean hasKids;

    @ElementCollection
    private List<String> kids;
}