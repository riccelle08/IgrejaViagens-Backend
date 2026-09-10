package br.com.viagensigreja.controller;

import br.com.viagensigreja.dto.UserResponseDTO;
import br.com.viagensigreja.mapper.UserMapper;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService service;
    private final UserMapper userMapper;

    public UserController(UserService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponseDTO> listar() {
        return userMapper.toResponseList(service.listar());
    }

    @GetMapping("/{cpf}")
    public UserResponseDTO buscar(@PathVariable String cpf) {
        return userMapper.toResponse(service.buscarPorCpf(cpf.replaceAll("\\D", "")));
    }

    @PostMapping
    public UserResponseDTO criar(@RequestBody User user) {
        user.setCpf(user.getCpf().replaceAll("\\D", ""));
        return userMapper.toResponse(service.salvar(user));
    }

    @PutMapping("/{cpf}")
    public UserResponseDTO atualizar(@PathVariable String cpf, @RequestBody User user) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return userMapper.toResponse(service.atualizar(cpfLimpo, user));
    }

    @PutMapping("/bulk")
    public List<UserResponseDTO> substituirTodos(@RequestBody List<User> users) {
        return userMapper.toResponseList(service.substituirTodos(users));
    }

    @DeleteMapping("/{cpf}")
    public void deletar(@PathVariable String cpf) {
        service.deletar(cpf.replaceAll("\\D", ""));
    }
}
