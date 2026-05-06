package br.com.viagensigreja.service;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public User login(LoginDTO dto) {
        String cpfLimpo = dto.getCpf() == null ? "" : dto.getCpf().replaceAll("\\D", "");

        User user = repository.findById(cpfLimpo).orElse(null);

        if (user != null && user.getPassword() != null && user.getPassword().equals(dto.getPassword())) {
            return user;
        }

        return null;
    }
}