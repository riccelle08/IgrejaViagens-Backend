package br.com.viagensigreja.repository;

import br.com.viagensigreja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByCpf(String cpf);

}