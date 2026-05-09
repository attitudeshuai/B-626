package com.gomoku.service;

import com.gomoku.entity.User;
import com.gomoku.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingService {

    private final UserMapper userMapper;

    public RankingService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<Map<String, Object>> getWinRateRanking() {
        List<User> users = userMapper.findTopByWinRate();
        List<Map<String, Object>> result = new ArrayList<>();

        int rank = 1;
        for (User user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("userId", user.getId());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("totalGames", user.getTotalGames());
            item.put("wins", user.getWins());
            double winRate = user.getTotalGames() > 0 ?
                    (double) user.getWins() / user.getTotalGames() * 100 : 0;
            item.put("winRate", Math.round(winRate * 100.0) / 100.0);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getWinsRanking() {
        List<User> users = userMapper.findTopByWins();
        List<Map<String, Object>> result = new ArrayList<>();

        int rank = 1;
        for (User user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("userId", user.getId());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("wins", user.getWins());
            result.add(item);
        }

        return result;
    }
}
