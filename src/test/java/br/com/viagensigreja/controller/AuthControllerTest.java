package br.com.viagensigreja.controller;

import br.com.viagensigreja.dto.LoginDTO;
import br.com.viagensigreja.mapper.UserMapper;
import br.com.viagensigreja.model.User;
import br.com.viagensigreja.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, new UserMapper());
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void loginNaoExpoePasswordNaResposta() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenReturn(usuarioComSenha());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"52998224725\",\"password\":\"segredo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.birthdate").value("1990-05-12"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private User usuarioComSenha() {
        return new User(
                "52998224725",
                "Maria",
                "segredo",
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
