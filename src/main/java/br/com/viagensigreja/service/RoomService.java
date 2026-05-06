package br.com.viagensigreja.service;

import br.com.viagensigreja.model.Room;
import br.com.viagensigreja.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public Room salvar(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId((room.getTripId() == null ? "room" : room.getTripId()) + "_" + System.currentTimeMillis());
        }
        return repository.save(room);
    }

    public List<Room> buscarPorTrip(String tripId) {
        return repository.findByTripId(tripId);
    }
}