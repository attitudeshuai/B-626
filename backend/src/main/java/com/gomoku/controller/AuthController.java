package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.dto.LoginRequest;
import com.gomoku.dto.RegisterRequest;
import com.gomoku.entity.User;
import com.gomoku.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            return ApiResponse.success("注册成功", data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Map<String, Object> data = userService.login(request);
            return ApiResponse.success("登录成功", data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
