package br.com.viagensigreja.controller;

import br.com.viagensigreja.mapper.TravelerResourceMapper;
import br.com.viagensigreja.model.Trip;
import br.com.viagensigreja.repository.TripRepository;
import br.com.viagensigreja.security.ResourceAuthorizationService;
import br.com.viagensigreja.service.TripService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService service;
    private final TripRepository repository;
    private final ResourceAuthorizationService authorization;
    private final TravelerResourceMapper travelerMapper;

    public TripController(
            TripService service,
            TripRepository repository,
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
            return service.listar();
        }

        String cpf = authorization.authenticatedCpf(authentication);
        return authorization.accessibleTrips(authentication).stream()
                .map(trip -> travelerMapper.toTripResponse(trip, cpf))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAVELER')")
    public Object buscar(@PathVariable String id, Authentication authentication) {
        if (authorization.isAdmin(authentication)) {
            return repository.findById(id).orElse(null);
        }

        authorization.requireTripAccess(authentication, id);
        return repository.findById(id)
                .map(trip -> travelerMapper.toTripResponse(
                        trip,
                        authorization.authenticatedCpf(authentication)
                ))
                .orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Trip criar(@RequestBody Trip trip) {
        return service.salvar(trip);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Trip atualizar(@PathVariable String id, @RequestBody Trip trip) {
        trip.setId(id);
        return service.salvar(trip);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Trip> substituirTodos(@RequestBody List<Trip> trips) {
        repository.deleteAll();
        return repository.saveAll(trips);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable String id) {
        repository.deleteById(id);
    }
}
