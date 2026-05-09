package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.dto.UserUpdateRequest;
import com.gomoku.entity.User;
import com.gomoku.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("totalGames", user.getTotalGames());
        data.put("wins", user.getWins());
        data.put("losses", user.getLosses());
        data.put("draws", user.getDraws());

        double winRate = user.getTotalGames() > 0 ?
                (double) user.getWins() / user.getTotalGames() * 100 : 0;
        data.put("winRate", Math.round(winRate * 100.0) / 100.0);

        return ApiResponse.success(data);
    }

    @PutMapping("/me")
    public ApiResponse<Map<String, Object>> updateUser(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.updateUser(currentUser.getId(), request);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());
            return ApiResponse.success("更新成功", data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
