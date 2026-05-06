package br.com.viagensigreja.controller;

import br.com.viagensigreja.model.Room;
import br.com.viagensigreja.repository.RoomRepository;
import br.com.viagensigreja.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@CrossOrigin
public class RoomController {

    private final RoomService service;
    private final RoomRepository repository;

    public RoomController(RoomService service, RoomRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Room> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Room criar(@RequestBody Room room) {
        return service.salvar(room);
    }

    @PutMapping("/bulk")
    public List<Room> substituirTodos(@RequestBody List<Room> rooms) {
        repository.deleteAll();
        rooms.forEach(r -> {
            if (r.getId() == null || r.getId().isBlank()) {
                r.setId((r.getTripId() == null ? "room" : r.getTripId()) + "_" + System.currentTimeMillis());
            }
        });
        return repository.saveAll(rooms);
    }

    @GetMapping("/trip/{tripId}")
    public List<Room> listarPorViagem(@PathVariable String tripId) {
        return service.buscarPorTrip(tripId);
    }
}
