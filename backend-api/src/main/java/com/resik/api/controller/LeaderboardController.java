package com.resik.api.controller;

import com.resik.api.entity.User;
import com.resik.api.repository.UserRepository;
import com.resik.api.repository.TransaksiRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepo;

    public LeaderboardController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<Map<String, Object>> getLeaderboard() {
        List<User> users = userRepo.findAll().stream()
                .filter(u -> "MASYARAKAT".equals(u.getRole()))
                .sorted((a, b) -> Integer.compare(
                        b.getPoinReward() != null ? b.getPoinReward() : 0,
                        a.getPoinReward() != null ? a.getPoinReward() : 0))
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (User u : users) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("userId", u.getId());
            m.put("nama", u.getNama());
            m.put("poin", u.getPoinReward());
            m.put("saldo", u.getSaldoDompet());
            result.add(m);
        }
        return result;
    }
}
