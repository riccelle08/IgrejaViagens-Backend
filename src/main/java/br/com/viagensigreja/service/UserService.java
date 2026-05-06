package br.com.viagensigreja.service;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> listar() {
        return repository.findAll();
    }

    public User salvar(User user) {
        return repository.save(user);
    }

    public User buscarPorCpf(String cpf) {
        return repository.findById(cpf).orElse(null);
    }
}