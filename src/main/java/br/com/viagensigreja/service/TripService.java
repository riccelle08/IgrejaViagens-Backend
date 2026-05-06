package br.com.viagensigreja.service;

import br.com.viagensigreja.model.Trip;
import br.com.viagensigreja.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    private final TripRepository repository;

    public TripService(TripRepository repository) {
        this.repository = repository;
    }

    public List<Trip> listar() {
        return repository.findAll();
    }

    public Trip salvar(Trip trip) {
        if (trip.getId() == null || trip.getId().isBlank()) {
            trip.setId("trip_" + System.currentTimeMillis());
        }
        return repository.save(trip);
    }
}