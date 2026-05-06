package br.com.viagensigreja.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id;

    private String userCpf;
    private String tripId;

    private int totalInstallments;
    private int paidInstallments;

    private int dueDay;

    private boolean locked;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String receiptsJson;
}
