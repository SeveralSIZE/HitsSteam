package ru.services.hitssteam.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardRequest {
    private String cardholderName;
    private String cardNumber;
    private String expiry;
    private String cvv;
    private BigDecimal amount;
}