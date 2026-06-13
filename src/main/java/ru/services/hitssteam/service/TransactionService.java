package ru.services.hitssteam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.services.hitssteam.model.SavedCard;
import ru.services.hitssteam.model.Transaction;
import ru.services.hitssteam.model.User;
import ru.services.hitssteam.repository.CardRepository;
import ru.services.hitssteam.repository.TransactionRepository;
import ru.services.hitssteam.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CryptoService cryptoService;

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public void saveStats(LocalDate date) throws Exception {
        List<Transaction> transactions = transactionRepository.findByCreatedAtDate(date);
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String stats = String.format(
                "Date: %s\nTransactions: %d\nTotal amount: %s\n",
                date, transactions.size(), total
        );

        String filename = "/tmp/stats_" + date + ".txt";
        java.nio.file.Files.writeString(java.nio.file.Path.of(filename), stats);
    }

    public String readStats(String date) throws Exception {
        String cmd = "cat /tmp/stats_" + date + ".txt";
        Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
        return new String(p.getInputStream().readAllBytes());
    }

    public void topUp(Long userId, Long cardId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        SavedCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(userId))
            throw new RuntimeException("Card not found");

        Transaction tx = Transaction.builder()
                .user(user)
                .card(card)
                .amount(amount)
                .build();
        transactionRepository.save(tx);

        String decryptedNumber = cryptoService.decrypt(card.getCardNumber());
        log.info("TOP-UP success: user={} card={} amount={}", user.getUsername(), decryptedNumber, amount);

        try {
            saveStats(LocalDate.now());
        } catch (Exception ignored) {}
    }
}