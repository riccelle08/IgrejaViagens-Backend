package br.com.viagensigreja.repository;

import br.com.viagensigreja.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByUserCpf(String cpf);

}