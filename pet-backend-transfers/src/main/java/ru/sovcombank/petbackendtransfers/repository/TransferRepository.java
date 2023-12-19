package ru.sovcombank.petbackendtransfers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByUuid(UUID uuid);
}
