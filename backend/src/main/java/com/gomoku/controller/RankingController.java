package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/winrate")
    public ApiResponse<List<Map<String, Object>>> getWinRateRanking() {
        List<Map<String, Object>> data = rankingService.getWinRateRanking();
        return ApiResponse.success(data);
    }

    @GetMapping("/wins")
    public ApiResponse<List<Map<String, Object>>> getWinsRanking() {
        List<Map<String, Object>> data = rankingService.getWinsRanking();
        return ApiResponse.success(data);
    }
}
