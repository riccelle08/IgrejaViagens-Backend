package br.com.viagensigreja.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    private String id;

    private String tripId;
    private String busId;

    private int floor;
    private int seatNumber;

    private String userCpf;
}
