package br.com.viagensigreja.service;

import br.com.viagensigreja.model.Payment;
import br.com.viagensigreja.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public Payment salvar(Payment payment) {
        if (payment.getId() == null || payment.getId().isBlank()) {
            payment.setId(payment.getUserCpf() + "_" + payment.getTripId());
        }
        return repository.save(payment);
    }

    public List<Payment> buscarPorUsuario(String cpf) {
        return repository.findByUserCpf(cpf);
    }
}