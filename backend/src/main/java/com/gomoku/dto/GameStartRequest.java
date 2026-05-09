package com.gomoku.dto;

import lombok.Data;

@Data
public class GameStartRequest {
    private String mode; // AI or LOCAL
    private String difficulty; // EASY, MEDIUM, HARD
    private boolean playerFirst; // true if player goes first
}
