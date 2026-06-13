package ru.services.hitssteam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.services.hitssteam.model.SavedCard;
import java.util.List;

public interface CardRepository extends JpaRepository<SavedCard, Long> {
    List<SavedCard> findByUserId(Long userId);
}