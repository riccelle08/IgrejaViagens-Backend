package br.com.viagensigreja.service;

import br.com.viagensigreja.model.Seat;
import br.com.viagensigreja.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository repository;

    public SeatService(SeatRepository repository) {
        this.repository = repository;
    }

    public Seat salvar(Seat seat) {
        if (seat.getId() == null || seat.getId().isBlank()) {
            seat.setId(seat.getTripId() + "_" + seat.getBusId() + "_" + seat.getUserCpf() + "_" + seat.getSeatNumber());
        }
        return repository.save(seat);
    }

    public List<Seat> buscarPorTrip(String tripId) {
        return repository.findByTripId(tripId);
    }

    public List<Seat> buscarPorUsuario(String cpf) {
        return repository.findByUserCpf(cpf);
    }
}