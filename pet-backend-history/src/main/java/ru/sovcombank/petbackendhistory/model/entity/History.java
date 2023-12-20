package ru.sovcombank.petbackendhistory.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity(name = "history")
public class History {

    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String accountNumberFrom;

    @Column(nullable = false)
    private String accountNumberTo;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String cur;

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;
}
