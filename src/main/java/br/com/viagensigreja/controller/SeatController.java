package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.Seat;
import br.com.viagensigreja.repository.SeatRepository;
import br.com.viagensigreja.service.SeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@CrossOrigin
public class SeatController {

    private final SeatService service;
    private final SeatRepository repository;

    public SeatController(SeatService service, SeatRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Seat> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Seat criar(@RequestBody Seat seat) {
        return service.salvar(seat);
    }

    @PutMapping("/bulk")
    public List<Seat> substituirTodos(@RequestBody List<Seat> seats) {
        repository.deleteAll();
        seats.forEach(s -> {
            if (s.getId() == null || s.getId().isBlank()) {
                s.setId(s.getTripId() + "_" + s.getBusId() + "_" + s.getUserCpf() + "_" + s.getSeatNumber());
            }
        });
        return repository.saveAll(seats);
    }

    @GetMapping("/trip/{tripId}")
    public List<Seat> listarPorViagem(@PathVariable String tripId) {
        return service.buscarPorTrip(tripId);
    }

    @GetMapping("/user/{cpf}")
    public List<Seat> listarPorUsuario(@PathVariable String cpf) {
        return service.buscarPorUsuario(cpf.replaceAll("\\D", ""));
    }
}
