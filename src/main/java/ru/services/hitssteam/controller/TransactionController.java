package ru.services.hitssteam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.services.hitssteam.dto.TopUpRequest;
import ru.services.hitssteam.repository.UserRepository;
import ru.services.hitssteam.service.TransactionService;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> topUp(@RequestBody TopUpRequest req, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        transactionService.topUp(userId, req.getCardId(), req.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(transactionService.getUserTransactions(userId));
    }
}
