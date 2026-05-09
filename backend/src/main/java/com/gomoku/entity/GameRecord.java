package com.gomoku.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game_records")
public class GameRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String gameMode;
    private String difficulty;
    private String result;
    private String playerColor;
    private String moves;
    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
