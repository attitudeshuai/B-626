package com.gomoku.controller;

import com.gomoku.dto.*;
import com.gomoku.entity.GameRecord;
import com.gomoku.entity.User;
import com.gomoku.service.GameService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ApiResponse<Map<String, Object>> startGame(@RequestBody GameStartRequest request) {
        try {
            Map<String, Object> data = gameService.startGame(request);
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/move")
    public ApiResponse<Map<String, Object>> makeMove(@RequestBody GameMoveRequest request) {
        try {
            Map<String, Object> data = gameService.makeMove(request);
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/undo")
    public ApiResponse<Map<String, Object>> undoMove(@RequestBody Map<String, String> request) {
        try {
            Map<String, Object> data = gameService.undoMove(request.get("gameId"));
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/surrender")
    public ApiResponse<Map<String, Object>> surrender(@RequestBody Map<String, String> request) {
        try {
            Map<String, Object> data = gameService.surrender(request.get("gameId"));
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/save")
    public ApiResponse<String> saveGame(
            @AuthenticationPrincipal User user,
            @RequestBody GameSaveRequest request) {
        try {
            gameService.saveGame(user.getId(), request);
            return ApiResponse.success("保存成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ApiResponse<List<GameRecord>> getHistory(@AuthenticationPrincipal User user) {
        List<GameRecord> records = gameService.getHistory(user.getId());
        return ApiResponse.success(records);
    }

    @GetMapping("/history/{id}")
    public ApiResponse<GameRecord> getGameDetail(@PathVariable Long id) {
        GameRecord record = gameService.getGameDetail(id);
        if (record == null) {
            return ApiResponse.error("对局记录不存在");
        }
        return ApiResponse.success(record);
    }
}
