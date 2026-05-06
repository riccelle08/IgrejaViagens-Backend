package br.com.viagensigreja.repository;

import br.com.viagensigreja.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, String> {

    List<Seat> findByTripId(String tripId);

    List<Seat> findByUserCpf(String cpf);

}