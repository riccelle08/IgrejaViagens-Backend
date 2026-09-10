package br.com.viagensigreja.controller;

import br.com.viagensigreja.mapper.TravelerResourceMapper;
import br.com.viagensigreja.model.Room;
import br.com.viagensigreja.repository.RoomRepository;
import br.com.viagensigreja.security.ResourceAuthorizationService;
import br.com.viagensigreja.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService service;
    private final RoomRepository repository;
    private final ResourceAuthorizationService authorization;
    private final TravelerResourceMapper travelerMapper;

    public RoomController(
            RoomService service,
            RoomRepository repository,
            ResourceAuthorizationService authorization,
            TravelerResourceMapper travelerMapper
    ) {
        this.service = service;
        this.repository = repository;
        this.authorization = authorization;
        this.travelerMapper = travelerMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAVELER')")
    public List<?> listar(Authentication authentication) {
        if (authorization.isAdmin(authentication)) {
            return repository.findAll();
        }

        String cpf = authorization.authenticatedCpf(authentication);
        var tripIds = authorization.accessibleTripIds(authentication);
        return repository.findAll().stream()
                .filter(room -> tripIds.contains(room.getTripId()))
                .filter(room -> room.getOccupants() != null
                        && room.getOccupants().stream()
                        .anyMatch(occupant -> authorization.belongsTo(occupant, cpf)))
                .map(room -> travelerMapper.toRoomResponse(room, cpf))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Room criar(@RequestBody Room room) {
        return service.salvar(room);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAVELER')")
    public List<?> listarPorViagem(
            @PathVariable String tripId,
            Authentication authentication
    ) {
        if (authorization.isAdmin(authentication)) {
            return service.buscarPorTrip(tripId);
        }

        authorization.requireTripAccess(authentication, tripId);
        String cpf = authorization.authenticatedCpf(authentication);
        return service.buscarPorTrip(tripId).stream()
                .filter(room -> room.getOccupants() != null
                        && room.getOccupants().stream()
                        .anyMatch(occupant -> authorization.belongsTo(occupant, cpf)))
                .map(room -> travelerMapper.toRoomResponse(room, cpf))
                .toList();
    }
}
