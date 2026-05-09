package com.gomoku.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String avatar;
}
