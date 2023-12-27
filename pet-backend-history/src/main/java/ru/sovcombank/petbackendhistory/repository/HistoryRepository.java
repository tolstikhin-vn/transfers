package ru.sovcombank.petbackendhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sovcombank.petbackendhistory.model.entity.History;

import java.util.List;
import java.util.UUID;

public interface HistoryRepository extends JpaRepository<History, UUID> {

    @Query("SELECT h FROM history h WHERE h.clientIdFrom = :clientId OR h.clientIdTo = :clientId")
    List<History> findByClientId(@Param("clientId") String clientId);
}
