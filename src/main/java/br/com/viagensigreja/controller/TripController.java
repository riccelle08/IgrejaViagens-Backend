package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.Trip;
import br.com.viagensigreja.repository.TripRepository;
import br.com.viagensigreja.service.TripService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@CrossOrigin
public class TripController {

    private final TripService service;
    private final TripRepository repository;

    public TripController(TripService service, TripRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Trip> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Trip buscar(@PathVariable String id) {
        return repository.findById(id).orElse(null);
    }

    @PostMapping
    public Trip criar(@RequestBody Trip trip) {
        return service.salvar(trip);
    }

    @PutMapping("/{id}")
    public Trip atualizar(@PathVariable String id, @RequestBody Trip trip) {
        trip.setId(id);
        return service.salvar(trip);
    }

    @PutMapping("/bulk")
    public List<Trip> substituirTodos(@RequestBody List<Trip> trips) {
        repository.deleteAll();
        return repository.saveAll(trips);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable String id) {
        repository.deleteById(id);
    }
}
