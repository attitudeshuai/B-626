package com.gomoku.dto;

import lombok.Data;
import java.util.List;

@Data
public class GameSaveRequest {
    private String gameId;
    private String mode;
    private String difficulty;
    private String result;
    private String playerColor;
    private String moves;
    private Integer duration;
}
