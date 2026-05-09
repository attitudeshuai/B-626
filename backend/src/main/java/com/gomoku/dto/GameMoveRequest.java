package com.gomoku.dto;

import lombok.Data;

@Data
public class GameMoveRequest {
    private String gameId;
    private int x;
    private int y;
}
