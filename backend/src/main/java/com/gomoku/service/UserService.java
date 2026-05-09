package com.gomoku.service;

import com.gomoku.dto.LoginRequest;
import com.gomoku.dto.RegisterRequest;
import com.gomoku.dto.UserUpdateRequest;
import com.gomoku.entity.User;
import com.gomoku.mapper.UserMapper;
import com.gomoku.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public User register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setTotalGames(0);
        user.setWins(0);
        user.setLosses(0);
        user.setDraws(0);

        userMapper.insert(user);
        return user;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        result.put("user", userInfo);

        return result;
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);
        return user;
    }

    public void updateGameStats(Long userId, String result) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalGames(user.getTotalGames() + 1);
            switch (result) {
                case "WIN":
                    user.setWins(user.getWins() + 1);
                    break;
                case "LOSE":
                    user.setLosses(user.getLosses() + 1);
                    break;
                case "DRAW":
                    user.setDraws(user.getDraws() + 1);
                    break;
            }
            userMapper.updateById(user);
        }
    }
}
