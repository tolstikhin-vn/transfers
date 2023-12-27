package ru.sovcombank.petbackendaccounts.repository;

import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.sovcombank.petbackendaccounts.model.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientIdAndCur(Integer clientId, String cur);

    Optional<List<Account>> findByClientId(Integer clientId);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.OPTIMISTIC)
    @Nonnull
    Account save(Account account);
}
