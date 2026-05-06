package br.com.viagensigreja.repository;

import br.com.viagensigreja.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    List<Room> findByTripId(String tripId);

}