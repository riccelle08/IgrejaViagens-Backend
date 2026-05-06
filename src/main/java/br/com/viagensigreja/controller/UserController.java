package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import br.com.viagensigreja.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService service;
    private final UserRepository repository;

    public UserController(UserService service, UserRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<User> listar() {
        return service.listar();
    }

    @GetMapping("/{cpf}")
    public User buscar(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf.replaceAll("\\D", ""));
    }

    @PostMapping
    public User criar(@RequestBody User user) {
        user.setCpf(user.getCpf().replaceAll("\\D", ""));
        return service.salvar(user);
    }

    @PutMapping("/{cpf}")
    public User atualizar(@PathVariable String cpf, @RequestBody User user) {
        user.setCpf(cpf.replaceAll("\\D", ""));
        return service.salvar(user);
    }

    @PutMapping("/bulk")
    public List<User> substituirTodos(@RequestBody List<User> users) {
        repository.deleteAll();
        users.forEach(u -> u.setCpf(u.getCpf().replaceAll("\\D", "")));
        return repository.saveAll(users);
    }

    @DeleteMapping("/{cpf}")
    public void deletar(@PathVariable String cpf) {
        repository.deleteById(cpf.replaceAll("\\D", ""));
    }
}
