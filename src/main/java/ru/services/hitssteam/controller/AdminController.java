package ru.services.hitssteam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.services.hitssteam.service.CardService;
import ru.services.hitssteam.service.UserService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersIds() {
        return ResponseEntity.ok(userService.getAllUsersIds());
    }

    @GetMapping("/users/{id}/cards")
    public ResponseEntity<?> getUserCards(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getAdminUserCards(id));
    }
}