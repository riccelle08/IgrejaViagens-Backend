package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.Payment;
import br.com.viagensigreja.repository.PaymentRepository;
import br.com.viagensigreja.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService service;
    private final PaymentRepository repository;

    public PaymentController(PaymentService service, PaymentRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Payment> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Payment criar(@RequestBody Payment payment) {
        return service.salvar(payment);
    }

    @PutMapping("/bulk")
    public List<Payment> substituirTodos(@RequestBody List<Payment> payments) {
        repository.deleteAll();
        payments.forEach(p -> {
            if (p.getId() == null || p.getId().isBlank()) {
                p.setId(p.getUserCpf() + "_" + p.getTripId());
            }
        });
        return repository.saveAll(payments);
    }

    @GetMapping("/user/{cpf}")
    public List<Payment> listarPorUsuario(@PathVariable String cpf) {
        return service.buscarPorUsuario(cpf.replaceAll("\\D", ""));
    }
}
