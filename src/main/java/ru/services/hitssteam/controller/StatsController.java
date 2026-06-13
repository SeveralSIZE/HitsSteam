package ru.services.hitssteam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.services.hitssteam.service.TransactionService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getStats(@RequestParam String date) throws Exception {
        try {
            transactionService.saveStats(LocalDate.parse(date));
        } catch (Exception ignored) {}

        String content = transactionService.readStats(date);
        return ResponseEntity.ok(Map.of("stats", content));
    }
}