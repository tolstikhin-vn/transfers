package ru.sovcombank.petbackendaccounts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovcombank.petbackendaccounts.model.Account;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientIdAndCur(Integer clientId, String cur);
}
