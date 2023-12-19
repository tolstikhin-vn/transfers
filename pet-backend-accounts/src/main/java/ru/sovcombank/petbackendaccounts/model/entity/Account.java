package ru.sovcombank.petbackendaccounts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String cur;

    @Column(nullable = false)
    private BigDecimal balance = new BigDecimal("0.00");

    @Column(nullable = false)
    private LocalDateTime createDateTime = LocalDateTime.now();

    @Column(nullable = false, unique = true)
    private boolean isMain = true;

    @Column(nullable = false, unique = true)
    private boolean isClosed = false;
}
