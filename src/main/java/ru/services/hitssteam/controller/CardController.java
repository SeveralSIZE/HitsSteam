package ru.services.hitssteam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.services.hitssteam.dto.CardRequest;
import ru.services.hitssteam.repository.UserRepository;
import ru.services.hitssteam.service.CardService;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addCard(@RequestBody CardRequest req, Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        var card = cardService.addCard(req, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getCards(Authentication auth) {
        Long userId = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(cardService.getUserCards(userId));
    }
}
