package br.com.viagensigreja.service;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> listar() {
        return repository.findAll();
    }

    public User salvar(User user) {
        codificarNovaSenha(user);
        return repository.save(user);
    }

    public User atualizar(String cpf, User novosDados) {
        novosDados.setCpf(cpf);

        if (novosDados.getPassword() == null) {
            repository.findById(cpf).ifPresent(usuarioExistente ->
                    novosDados.setPassword(usuarioExistente.getPassword())
            );
        } else {
            codificarNovaSenha(novosDados);
        }

        return repository.save(novosDados);
    }

    @Transactional
    public List<User> substituirTodos(List<User> users) {
        Map<String, String> senhasExistentes = new HashMap<>();
        repository.findAll().forEach(user ->
                senhasExistentes.put(user.getCpf(), user.getPassword())
        );

        users.forEach(user -> {
            user.setCpf(user.getCpf().replaceAll("\\D", ""));
            if (user.getPassword() == null) {
                user.setPassword(senhasExistentes.get(user.getCpf()));
            } else {
                codificarNovaSenha(user);
            }
        });

        repository.deleteAll();
        return repository.saveAll(users);
    }

    public User buscarPorCpf(String cpf) {
        return repository.findById(cpf).orElse(null);
    }

    public void deletar(String cpf) {
        repository.deleteById(cpf);
    }

    private void codificarNovaSenha(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }
}
