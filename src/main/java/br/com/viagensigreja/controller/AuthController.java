package br.com.viagensigreja.controller;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.dto.UserResponseDTO;
import br.com.viagensigreja.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public AuthController(AuthService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        User user = service.login(dto);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("CPF ou senha incorretos.");
        }

        UserResponseDTO response = userMapper.toResponse(user);
        return ResponseEntity.ok(response);
    }
}
