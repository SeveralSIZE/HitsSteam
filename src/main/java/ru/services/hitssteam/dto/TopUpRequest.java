package ru.services.hitssteam.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpRequest {
    private Long cardId;
    private BigDecimal amount;
}
