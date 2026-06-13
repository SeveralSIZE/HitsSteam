package ru.services.hitssteam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import ru.services.hitssteam.dto.CardRequest;
import ru.services.hitssteam.model.*;
import ru.services.hitssteam.repository.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    public SavedCard addCard(CardRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!cardRepository.findByUserId(userId).isEmpty()) {
            throw new RuntimeException("Card already added");
        }

        SavedCard card = SavedCard.builder()
                .user(user)
                .cardholderName(cryptoService.encrypt(req.getCardholderName()))
                .cardNumber(cryptoService.encrypt(req.getCardNumber()))
                .expiry(cryptoService.encrypt(req.getExpiry()))
                .cvv(cryptoService.encrypt(req.getCvv()))
                .build();
        cardRepository.save(card);

        return card;
    }

    public List<SavedCard> getUserCards(Long userId) {
        List<SavedCard> cards = cardRepository.findByUserId(userId);
        cards.forEach(c -> {
            c.setCardNumber(cryptoService.decrypt(c.getCardNumber()));
            c.setCardholderName(cryptoService.decrypt(c.getCardholderName()));
            c.setExpiry(cryptoService.decrypt(c.getExpiry()));
        });
        return cards;
    }

    public List<SavedCard> getAdminUserCards(Long userId) {
        List<SavedCard> cards = cardRepository.findByUserId(userId);
        cards.forEach(c -> {
            c.setCardNumber(cryptoService.decrypt(c.getCardNumber()));
            c.setCardholderName(cryptoService.decrypt(c.getCardholderName()));
            c.setExpiry(cryptoService.decrypt(c.getExpiry()));
            c.setCvv(cryptoService.decrypt(c.getCvv()));
        });
        return cards;
    }
}