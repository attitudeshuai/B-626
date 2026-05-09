package com.gomoku.service;

import com.gomoku.dto.GameMoveRequest;
import com.gomoku.dto.GameSaveRequest;
import com.gomoku.dto.GameStartRequest;
import com.gomoku.entity.GameRecord;
import com.gomoku.mapper.GameRecordMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final int BOARD_SIZE = 15;

    private final AIService aiService;
    private final GameRecordMapper gameRecordMapper;
    private final UserService userService;

    // 存储进行中的游戏状态
    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();

    public GameService(AIService aiService, GameRecordMapper gameRecordMapper, UserService userService) {
        this.aiService = aiService;
        this.gameRecordMapper = gameRecordMapper;
        this.userService = userService;
    }

    public Map<String, Object> startGame(GameStartRequest request) {
        String gameId = UUID.randomUUID().toString();
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

        GameState state = new GameState();
        state.setBoard(board);
        state.setMode(request.getMode());
        state.setDifficulty(request.getDifficulty());
        state.setPlayerColor(request.isPlayerFirst() ? "BLACK" : "WHITE");
        state.setCurrentPlayer("BLACK");
        state.setMoves(new ArrayList<>());
        state.setUndoCount(0);
        state.setStartTime(System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();
        result.put("gameId", gameId);
        result.put("board", board);
        result.put("currentPlayer", "BLACK");
        result.put("playerColor", state.getPlayerColor());

        // If AI goes first
        if ("AI".equals(request.getMode()) && !request.isPlayerFirst()) {
            int[] aiMove = aiService.getAIMove(board, request.getDifficulty(), "BLACK");
            board[aiMove[0]][aiMove[1]] = 1;
            state.setCurrentPlayer("WHITE");

            Map<String, Object> move = new HashMap<>();
            move.put("x", aiMove[0]);
            move.put("y", aiMove[1]);
            move.put("color", "BLACK");
            move.put("step", 1);
            state.getMoves().add(move);

            result.put("board", board);
            result.put("currentPlayer", "WHITE");
            result.put("aiMove", aiMove);
        }

        activeGames.put(gameId, state);
        return result;
    }

    public Map<String, Object> makeMove(GameMoveRequest request) {
        GameState state = activeGames.get(request.getGameId());
        if (state == null) {
            throw new RuntimeException("游戏不存在");
        }

        int[][] board = state.getBoard();
        int x = request.getX();
        int y = request.getY();

        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            throw new RuntimeException("落子位置无效");
        }

        if (board[x][y] != 0) {
            throw new RuntimeException("该位置已有棋子");
        }

        int playerValue = "BLACK".equals(state.getCurrentPlayer()) ? 1 : 2;
        board[x][y] = playerValue;

        Map<String, Object> move = new HashMap<>();
        move.put("x", x);
        move.put("y", y);
        move.put("color", state.getCurrentPlayer());
        move.put("step", state.getMoves().size() + 1);
        state.getMoves().add(move);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("board", board);

        // Check win
        if (aiService.checkWin(board, x, y, playerValue)) {
            result.put("gameOver", true);
            result.put("winner", state.getCurrentPlayer());
            activeGames.remove(request.getGameId());
            return result;
        }

        // Check draw
        if (isBoardFull(board)) {
            result.put("gameOver", true);
            result.put("winner", "DRAW");
            activeGames.remove(request.getGameId());
            return result;
        }

        // Switch player
        String nextPlayer = "BLACK".equals(state.getCurrentPlayer()) ? "WHITE" : "BLACK";
        state.setCurrentPlayer(nextPlayer);

        result.put("gameOver", false);
        result.put("currentPlayer", nextPlayer);

        // AI move
        if ("AI".equals(state.getMode())) {
            String aiColor = "BLACK".equals(state.getPlayerColor()) ? "WHITE" : "BLACK";
            int[] aiMove = aiService.getAIMove(board, state.getDifficulty(), aiColor);

            int aiValue = "BLACK".equals(aiColor) ? 1 : 2;
            board[aiMove[0]][aiMove[1]] = aiValue;

            Map<String, Object> aiMoveMap = new HashMap<>();
            aiMoveMap.put("x", aiMove[0]);
            aiMoveMap.put("y", aiMove[1]);
            aiMoveMap.put("color", aiColor);
            aiMoveMap.put("step", state.getMoves().size() + 1);
            state.getMoves().add(aiMoveMap);

            result.put("aiMove", Map.of("x", aiMove[0], "y", aiMove[1]));
            result.put("board", board);

            // Check AI win
            if (aiService.checkWin(board, aiMove[0], aiMove[1], aiValue)) {
                result.put("gameOver", true);
                result.put("winner", aiColor);
                activeGames.remove(request.getGameId());
                return result;
            }

            // Switch back to player
            state.setCurrentPlayer(state.getPlayerColor());
            result.put("currentPlayer", state.getPlayerColor());
        }

        return result;
    }

    public Map<String, Object> undoMove(String gameId) {
        GameState state = activeGames.get(gameId);
        if (state == null) {
            throw new RuntimeException("游戏不存在");
        }

        if (state.getUndoCount() >= 3) {
            throw new RuntimeException("悔棋次数已用完");
        }

        List<Map<String, Object>> moves = state.getMoves();
        if (moves.isEmpty()) {
            throw new RuntimeException("没有可以撤销的棋步");
        }

        int[][] board = state.getBoard();

        // In AI mode, undo both player and AI moves
        if ("AI".equals(state.getMode()) && moves.size() >= 2) {
            Map<String, Object> lastMove = moves.remove(moves.size() - 1);
            board[(int) lastMove.get("x")][(int) lastMove.get("y")] = 0;

            Map<String, Object> playerMove = moves.remove(moves.size() - 1);
            board[(int) playerMove.get("x")][(int) playerMove.get("y")] = 0;
        } else if (moves.size() >= 1) {
            Map<String, Object> lastMove = moves.remove(moves.size() - 1);
            board[(int) lastMove.get("x")][(int) lastMove.get("y")] = 0;
            state.setCurrentPlayer("BLACK".equals(state.getCurrentPlayer()) ? "WHITE" : "BLACK");
        }

        state.setUndoCount(state.getUndoCount() + 1);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("board", board);
        result.put("currentPlayer", state.getCurrentPlayer());
        result.put("undoRemaining", 3 - state.getUndoCount());
        return result;
    }

    public Map<String, Object> surrender(String gameId) {
        GameState state = activeGames.get(gameId);
        if (state == null) {
            throw new RuntimeException("游戏不存在");
        }

        String winner = "BLACK".equals(state.getPlayerColor()) ? "WHITE" : "BLACK";
        activeGames.remove(gameId);

        Map<String, Object> result = new HashMap<>();
        result.put("gameOver", true);
        result.put("winner", winner);
        result.put("surrender", true);
        return result;
    }

    public void saveGame(Long userId, GameSaveRequest request) {
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setGameMode(request.getMode());
        record.setDifficulty(request.getDifficulty());
        record.setResult(request.getResult());
        record.setPlayerColor(request.getPlayerColor());
        record.setMoves(request.getMoves());
        record.setDuration(request.getDuration());

        gameRecordMapper.insert(record);
        userService.updateGameStats(userId, request.getResult());
    }

    public List<GameRecord> getHistory(Long userId) {
        return gameRecordMapper.findByUserId(userId);
    }

    public GameRecord getGameDetail(Long gameId) {
        return gameRecordMapper.selectById(gameId);
    }

    private boolean isBoardFull(int[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    // Inner class for game state
    private static class GameState {
        private int[][] board;
        private String mode;
        private String difficulty;
        private String playerColor;
        private String currentPlayer;
        private List<Map<String, Object>> moves;
        private int undoCount;
        private long startTime;

        public int[][] getBoard() { return board; }
        public void setBoard(int[][] board) { this.board = board; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getPlayerColor() { return playerColor; }
        public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }
        public String getCurrentPlayer() { return currentPlayer; }
        public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
        public List<Map<String, Object>> getMoves() { return moves; }
        public void setMoves(List<Map<String, Object>> moves) { this.moves = moves; }
        public int getUndoCount() { return undoCount; }
        public void setUndoCount(int undoCount) { this.undoCount = undoCount; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}
