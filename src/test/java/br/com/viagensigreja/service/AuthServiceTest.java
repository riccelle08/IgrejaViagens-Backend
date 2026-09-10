package br.com.viagensigreja.service;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import br.com.viagensigreja.security.LegacyCompatiblePasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        passwordEncoder = new LegacyCompatiblePasswordEncoder();
        service = new AuthService(repository, passwordEncoder);
    }

    @Test
    void senhaLegadaAutenticaSemRehashAutomatico() {
        User user = usuarioComSenha("senha-legada");
        when(repository.findById("52998224725")).thenReturn(Optional.of(user));

        User autenticado = service.login(login("529.982.247-25", "senha-legada"));

        assertSame(user, autenticado);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void senhaBCryptAutentica() {
        User user = usuarioComSenha(passwordEncoder.encode("senha-segura"));
        when(repository.findById("52998224725")).thenReturn(Optional.of(user));

        User autenticado = service.login(login("52998224725", "senha-segura"));

        assertSame(user, autenticado);
    }

    @Test
    void senhaInvalidaFalha() {
        User user = usuarioComSenha(passwordEncoder.encode("senha-correta"));
        when(repository.findById("52998224725")).thenReturn(Optional.of(user));

        User autenticado = service.login(login("52998224725", "senha-incorreta"));

        assertNull(autenticado);
    }

    private LoginDTO login(String cpf, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setCpf(cpf);
        dto.setPassword(password);
        return dto;
    }

    private User usuarioComSenha(String password) {
        return new User(
                "52998224725",
                "Maria",
                password,
                "traveler",
                "1990-05-12",
                false,
                false,
                "",
                false,
                List.of()
        );
    }
}
