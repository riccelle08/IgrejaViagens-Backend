package br.com.viagensigreja.controller;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        User user = service.login(dto);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("CPF ou senha incorretos.");
        }

        return ResponseEntity.ok(user);
    }
}