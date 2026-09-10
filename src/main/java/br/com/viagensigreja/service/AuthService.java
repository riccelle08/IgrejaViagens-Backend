package br.com.viagensigreja.service;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginDTO dto) {
        String cpfLimpo = dto.getCpf() == null ? "" : dto.getCpf().replaceAll("\\D", "");

        User user = repository.findById(cpfLimpo).orElse(null);

        if (user != null && passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return user;
        }

        return null;
    }
}
