package br.com.viagensigreja.security;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthSecurityIntegrationTest {

    private static final String CPF = "52998224725";

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void loginValidoCriaAutenticacaoNaSessao() throws Exception {
        salvarUsuario(passwordEncoder.encode("senha-segura"));

        MvcResult login = autenticar("senha-segura")
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = sessionFrom(login);
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );

        assertNotNull(context);
        assertTrue(context.getAuthentication().isAuthenticated());
        assertEquals(CPF, context.getAuthentication().getName());
        assertNull(context.getAuthentication().getCredentials());
    }

    @Test
    void loginInvalidoRetornaUnauthorizedSemCriarAutenticacao() throws Exception {
        salvarUsuario(passwordEncoder.encode("senha-correta"));

        autenticar("senha-incorreta")
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("CPF ou senha incorretos."));
    }

    @Test
    void authMeRetornaUsuarioAutenticadoSemPassword() throws Exception {
        salvarUsuario(passwordEncoder.encode("senha-segura"));
        MockHttpSession session = sessionFrom(
                autenticar("senha-segura").andReturn()
        );

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.role").value("traveler"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void logoutInvalidaSessao() throws Exception {
        salvarUsuario(passwordEncoder.encode("senha-segura"));
        MockHttpSession session = sessionFrom(
                autenticar("senha-segura").andReturn()
        );

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isNoContent());

        assertTrue(session.isInvalid());
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNaoAutenticadoNaoAcessaEndpointProtegido() throws Exception {
        mockMvc.perform(get("/trips"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void senhaLegadaContinuaAutenticandoSemRehash() throws Exception {
        salvarUsuario("senha-legada");

        autenticar("senha-legada")
                .andExpect(status().isOk());

        assertEquals("senha-legada", userRepository.findById(CPF).orElseThrow().getPassword());
    }

    @Test
    void senhaBCryptContinuaAutenticando() throws Exception {
        String hash = passwordEncoder.encode("senha-segura");
        salvarUsuario(hash);

        autenticar("senha-segura")
                .andExpect(status().isOk());

        assertEquals(hash, userRepository.findById(CPF).orElseThrow().getPassword());
    }

    @Test
    void usuarioAutenticadoAcessaEndpointProtegidoSemRegraDePapel() throws Exception {
        salvarUsuario(passwordEncoder.encode("senha-segura"));
        MockHttpSession session = sessionFrom(
                autenticar("senha-segura").andReturn()
        );

        mockMvc.perform(get("/trips").session(session))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions autenticar(String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cpf": "529.982.247-25",
                          "password": "%s"
                        }
                        """.formatted(password)));
    }

    private MockHttpSession sessionFrom(MvcResult result) {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private void salvarUsuario(String storedPassword) {
        User user = new User();
        user.setCpf(CPF);
        user.setName("Maria");
        user.setPassword(storedPassword);
        user.setRole("traveler");
        user.setBirthdate("1990-05-12");
        user.setFirstLogin(false);
        user.setMarried(false);
        user.setSpouseName("");
        user.setHasKids(false);
        user.setKids(new ArrayList<>());
        userRepository.saveAndFlush(user);
    }
}
