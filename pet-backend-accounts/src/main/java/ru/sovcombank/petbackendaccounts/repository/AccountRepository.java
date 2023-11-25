package ru.sovcombank.petbackendaccounts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovcombank.petbackendaccounts.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientIdAndCur(Integer clientId, String cur);
    Optional<List<Account>> findByClientId(Integer clientId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
