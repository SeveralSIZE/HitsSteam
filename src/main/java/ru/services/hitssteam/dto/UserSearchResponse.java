package ru.services.hitssteam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String username;
    private String email;
}
