package ru.services.hitssteam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.services.hitssteam.dto.UserSearchResponse;
import ru.services.hitssteam.model.User;
import ru.services.hitssteam.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public List<UserSearchResponse> search(String query) {
        String sql = "SELECT id, username, email FROM users WHERE username LIKE '%" + query + "%'";
        return jdbcTemplate.query(sql, (rs, rn) -> new UserSearchResponse(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email")
        ));
    }

    public List<Long> getAllUsersIds() {
        return userRepository.findAll().stream().map(User::getId).toList();
    }
}
