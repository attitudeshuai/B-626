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

    private final AIService aiService;
    private final BoardService boardService;
    private final GameRecordMapper gameRecordMapper;
    private final UserService userService;

    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();

    public GameService(AIService aiService, BoardService boardService, 
                       GameRecordMapper gameRecordMapper, UserService userService) {
        this.aiService = aiService;
        this.boardService = boardService;
        this.gameRecordMapper = gameRecordMapper;
        this.userService = userService;
    }

    public Map<String, Object> startGame(GameStartRequest request) {
        String gameId = UUID.randomUUID().toString();
        int[][] board = boardService.createEmptyBoard();

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

        if ("AI".equals(request.getMode()) && !request.isPlayerFirst()) {
            int[] aiMove = aiService.getAIMove(board, request.getDifficulty(), "BLACK");
            int aiValue = "BLACK".equals("BLACK") ? 1 : 2;
            boardService.placePiece(board, aiMove[0], aiMove[1], aiValue);
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

        if (!boardService.isValidPosition(x, y)) {
            throw new RuntimeException("落子位置无效");
        }

        if (!boardService.isEmpty(board, x, y)) {
            throw new RuntimeException("该位置已有棋子");
        }

        int playerValue = "BLACK".equals(state.getCurrentPlayer()) ? 1 : 2;
        boardService.placePiece(board, x, y, playerValue);

        Map<String, Object> move = new HashMap<>();
        move.put("x", x);
        move.put("y", y);
        move.put("color", state.getCurrentPlayer());
        move.put("step", state.getMoves().size() + 1);
        state.getMoves().add(move);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("board", board);

        if (boardService.checkWin(board, x, y, playerValue)) {
            result.put("gameOver", true);
            result.put("winner", state.getCurrentPlayer());
            activeGames.remove(request.getGameId());
            return result;
        }

        if (boardService.isBoardFull(board)) {
            result.put("gameOver", true);
            result.put("winner", "DRAW");
            activeGames.remove(request.getGameId());
            return result;
        }

        String nextPlayer = "BLACK".equals(state.getCurrentPlayer()) ? "WHITE" : "BLACK";
        state.setCurrentPlayer(nextPlayer);

        result.put("gameOver", false);
        result.put("currentPlayer", nextPlayer);

        if ("AI".equals(state.getMode())) {
            String aiColor = "BLACK".equals(state.getPlayerColor()) ? "WHITE" : "BLACK";
            int[] aiMove = aiService.getAIMove(board, state.getDifficulty(), aiColor);

            int aiValue = "BLACK".equals(aiColor) ? 1 : 2;
            boardService.placePiece(board, aiMove[0], aiMove[1], aiValue);

            Map<String, Object> aiMoveMap = new HashMap<>();
            aiMoveMap.put("x", aiMove[0]);
            aiMoveMap.put("y", aiMove[1]);
            aiMoveMap.put("color", aiColor);
            aiMoveMap.put("step", state.getMoves().size() + 1);
            state.getMoves().add(aiMoveMap);

            result.put("aiMove", Map.of("x", aiMove[0], "y", aiMove[1]));
            result.put("board", board);

            if (boardService.checkWin(board, aiMove[0], aiMove[1], aiValue)) {
                result.put("gameOver", true);
                result.put("winner", aiColor);
                activeGames.remove(request.getGameId());
                return result;
            }

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

        if ("AI".equals(state.getMode()) && moves.size() >= 2) {
            Map<String, Object> lastMove = moves.remove(moves.size() - 1);
            boardService.removePiece(board, (int) lastMove.get("x"), (int) lastMove.get("y"));

            Map<String, Object> playerMove = moves.remove(moves.size() - 1);
            boardService.removePiece(board, (int) playerMove.get("x"), (int) playerMove.get("y"));
        } else if (moves.size() >= 1) {
            Map<String, Object> lastMove = moves.remove(moves.size() - 1);
            boardService.removePiece(board, (int) lastMove.get("x"), (int) lastMove.get("y"));
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
