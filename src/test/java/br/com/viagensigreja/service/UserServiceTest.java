package br.com.viagensigreja.service;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void atualizarSemPasswordPreservaSenhaExistente() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository);
        User existente = usuario("senha-existente", "Nome antigo");
        User atualizacao = usuario(null, "Nome atualizado");

        when(repository.findById("52998224725")).thenReturn(Optional.of(existente));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User salvo = service.atualizar("52998224725", atualizacao);

        assertEquals("senha-existente", salvo.getPassword());
        assertEquals("Nome atualizado", salvo.getName());
        verify(repository).save(atualizacao);
    }

    @Test
    void atualizarComPasswordMantemNovoValorParaPrimeiroAcessoAtual() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository);
        User existente = usuario("senha-existente", "Nome antigo");
        User atualizacao = usuario("nova-senha", "Nome atualizado");

        when(repository.findById("52998224725")).thenReturn(Optional.of(existente));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User salvo = service.atualizar("52998224725", atualizacao);

        assertEquals("nova-senha", salvo.getPassword());
    }

    private User usuario(String password, String name) {
        return new User(
                "cpf-do-corpo",
                name,
                password,
                "traveler",
                "1990-05-12",
                false,
                false,
                null,
                false,
                List.of()
        );
    }
}
