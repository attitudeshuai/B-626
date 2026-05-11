package com.gomoku.websocket;

import com.gomoku.service.BoardService;
import lombok.Data;

import java.util.*;

@Data
public class GameRoom {
    private String roomId;
    private String roomName;
    private Long hostId;
    private String hostNickname;
    private Long guestId;
    private String guestNickname;
    private int[][] board;
    private String currentPlayer; // BLACK or WHITE
    private String hostColor; // BLACK or WHITE
    private List<Map<String, Object>> moves;
    private String status; // WAITING, PLAYING, FINISHED
    private String winner;
    private long createTime;
    private long startTime;
    private Map<Long, Integer> undoCount;

    public GameRoom(String roomId, String roomName, Long hostId, String hostNickname) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostId = hostId;
        this.hostNickname = hostNickname;
        this.board = new int[BoardService.BOARD_SIZE][BoardService.BOARD_SIZE];
        this.moves = new ArrayList<>();
        this.status = "WAITING";
        this.createTime = System.currentTimeMillis();
        this.undoCount = new HashMap<>();
    }

    public void startGame() {
        this.status = "PLAYING";
        this.currentPlayer = "BLACK";
        this.hostColor = new Random().nextBoolean() ? "BLACK" : "WHITE";
        this.board = new int[BoardService.BOARD_SIZE][BoardService.BOARD_SIZE];
        this.moves = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.undoCount.put(hostId, 0);
        this.undoCount.put(guestId, 0);
    }

    public boolean isFull() {
        return guestId != null;
    }

    public boolean isPlayerInRoom(Long userId) {
        return hostId.equals(userId) || (guestId != null && guestId.equals(userId));
    }

    public Long getOpponentId(Long userId) {
        if (hostId.equals(userId)) {
            return guestId;
        } else if (guestId != null && guestId.equals(userId)) {
            return hostId;
        }
        return null;
    }

    public String getPlayerColor(Long userId) {
        if (hostId.equals(userId)) {
            return hostColor;
        } else {
            return "BLACK".equals(hostColor) ? "WHITE" : "BLACK";
        }
    }

    public boolean isPlayerTurn(Long userId) {
        String playerColor = getPlayerColor(userId);
        return playerColor.equals(currentPlayer);
    }

    public void switchPlayer() {
        currentPlayer = "BLACK".equals(currentPlayer) ? "WHITE" : "BLACK";
    }

    public int getUndoRemaining(Long userId) {
        return 3 - undoCount.getOrDefault(userId, 0);
    }

    public void incrementUndoCount(Long userId) {
        undoCount.put(userId, undoCount.getOrDefault(userId, 0) + 1);
    }
}
