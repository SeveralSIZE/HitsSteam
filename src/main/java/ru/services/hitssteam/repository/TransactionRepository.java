package ru.services.hitssteam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.services.hitssteam.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndCreatedAtStartingWith(Long userId, String month);
    @Query("SELECT t FROM Transaction t WHERE CAST(t.createdAt AS date) = :date")
    List<Transaction> findByCreatedAtDate(@Param("date") LocalDate date);
}