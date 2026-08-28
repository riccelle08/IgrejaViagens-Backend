package br.com.viagensigreja.config;

import br.com.viagensigreja.model.User;
import br.com.viagensigreja.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_CPF = "10127544135";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;

    public AdminUserInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsById(ADMIN_CPF)) {
            return;
        }

        User admin = new User();
        admin.setCpf(ADMIN_CPF);
        admin.setName("Joao e Alice");
        admin.setPassword(ADMIN_PASSWORD);
        admin.setRole("admin");
        admin.setFirstLogin(false);
        admin.setMarried(false);
        admin.setHasKids(false);
        admin.setKids(new ArrayList<>());

        userRepository.save(admin);
    }
}
