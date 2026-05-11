package com.gomoku.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIService {

    private final BoardService boardService;

    private static final int FIVE = 10000000;
    private static final int OPEN_FOUR = 1000000;
    private static final int BLOCKED_FOUR = 100000;
    private static final int OPEN_THREE = 50000;
    private static final int BLOCKED_THREE = 5000;
    private static final int OPEN_TWO = 3000;
    private static final int BLOCKED_TWO = 500;
    private static final int OPEN_ONE = 100;

    private static final int EASY_DEPTH = 1;
    private static final int MEDIUM_DEPTH = 3;
    private static final int HARD_DEPTH = 5;

    public AIService(BoardService boardService) {
        this.boardService = boardService;
    }

    public int[] getAIMove(int[][] board, String difficulty, String aiColor) {
        int aiValue = "BLACK".equals(aiColor) ? 1 : 2;

        return switch (difficulty.toUpperCase()) {
            case "MEDIUM" -> getMediumMove(board, aiValue);
            case "HARD" -> getHardMove(board, aiValue);
            default -> getEasyMove(board, aiValue);
        };
    }

    private int[] getEasyMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null && Math.random() > 0.3) {
            return urgentBlock;
        }

        int[] attackMove = findUrgentMove(board, aiValue);
        if (attackMove != null && Math.random() > 0.4) {
            return attackMove;
        }

        return findScoredMoveWithRandom(board, aiValue, 0.3);
    }

    private int[] getMediumMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null) return urgentBlock;

        int[] attackMove = findUrgentMove(board, aiValue);
        if (attackMove != null) return attackMove;

        return minimaxSearch(board, aiValue, MEDIUM_DEPTH);
    }

    private int[] getHardMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null) return urgentBlock;

        int[] vctMove = findVCTMove(board, aiValue, 4);
        if (vctMove != null) return vctMove;

        return minimaxSearch(board, aiValue, HARD_DEPTH);
    }

    private int[] minimaxSearch(int[][] board, int aiValue, int depth) {
        List<int[]> candidates = getCandidateMoves(board);
        if (candidates.isEmpty()) {
            return new int[]{BoardService.BOARD_SIZE / 2, BoardService.BOARD_SIZE / 2};
        }

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        candidates.sort((a, b) -> {
            int scoreA = evaluatePosition(board, a[0], a[1], aiValue);
            int scoreB = evaluatePosition(board, b[0], b[1], aiValue);
            return scoreB - scoreA;
        });

        int maxCandidates = Math.min(candidates.size(), 20);

        for (int i = 0; i < maxCandidates; i++) {
            int[] move = candidates.get(i);
            int x = move[0], y = move[1];

            boardService.placePiece(board, x, y, aiValue);
            int score = minimax(board, depth - 1, alpha, beta, false, aiValue);
            boardService.removePiece(board, x, y);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }

            alpha = Math.max(alpha, score);
        }

        if (bestMoves.isEmpty()) {
            return candidates.get(0);
        }

        return bestMoves.get(new Random().nextInt(bestMoves.size()));
    }

    private int minimax(int[][] board, int depth, int alpha, int beta, boolean isMaximizing, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        int terminalScore = evaluateTerminal(board, aiValue);
        if (terminalScore != 0) {
            return isMaximizing ? -terminalScore : terminalScore;
        }

        if (depth <= 0) {
            return evaluateBoard(board, aiValue);
        }

        List<int[]> candidates = getCandidateMoves(board);
        if (candidates.isEmpty()) {
            return evaluateBoard(board, aiValue);
        }

        int maxCandidates = Math.min(candidates.size(), 12);

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < maxCandidates; i++) {
                int[] move = candidates.get(i);
                boardService.placePiece(board, move[0], move[1], aiValue);
                int score = minimax(board, depth - 1, alpha, beta, false, aiValue);
                boardService.removePiece(board, move[0], move[1]);

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < maxCandidates; i++) {
                int[] move = candidates.get(i);
                boardService.placePiece(board, move[0], move[1], playerValue);
                int score = minimax(board, depth - 1, alpha, beta, true, aiValue);
                boardService.removePiece(board, move[0], move[1]);

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return minScore;
        }
    }

    private int[] findVCTMove(int[][] board, int aiValue, int maxDepth) {
        int playerValue = aiValue == 1 ? 2 : 1;
        List<int[]> threatMoves = findAllThreatMoves(board, aiValue);

        for (int[] move : threatMoves) {
            boardService.placePiece(board, move[0], move[1], aiValue);
            if (canWinByVCT(board, aiValue, playerValue, maxDepth - 1)) {
                boardService.removePiece(board, move[0], move[1]);
                return move;
            }
            boardService.removePiece(board, move[0], move[1]);
        }
        return null;
    }

    private boolean canWinByVCT(int[][] board, int aiValue, int playerValue, int depth) {
        if (depth <= 0) return false;

        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return true;

        List<int[]> defensePoints = findAllDefensePoints(board, aiValue);
        if (defensePoints.isEmpty()) return false;

        if (defensePoints.size() >= 2) return true;

        int[] defensePoint = defensePoints.get(0);
        boardService.placePiece(board, defensePoint[0], defensePoint[1], playerValue);

        List<int[]> nextThreats = findAllThreatMoves(board, aiValue);
        for (int[] threat : nextThreats) {
            boardService.placePiece(board, threat[0], threat[1], aiValue);
            if (canWinByVCT(board, aiValue, playerValue, depth - 1)) {
                boardService.removePiece(board, threat[0], threat[1]);
                boardService.removePiece(board, defensePoint[0], defensePoint[1]);
                return true;
            }
            boardService.removePiece(board, threat[0], threat[1]);
        }

        boardService.removePiece(board, defensePoint[0], defensePoint[1]);
        return false;
    }

    private List<int[]> findAllThreatMoves(int[][] board, int player) {
        List<int[]> threats = new ArrayList<>();
        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j) && hasNeighbor(board, i, j)) {
                    int score = evaluatePosition(board, i, j, player);
                    if (score >= BLOCKED_THREE) {
                        threats.add(new int[]{i, j, score});
                    }
                }
            }
        }
        threats.sort((a, b) -> b[2] - a[2]);
        return threats.stream().map(t -> new int[]{t[0], t[1]}).toList();
    }

    private List<int[]> findAllDefensePoints(int[][] board, int attacker) {
        List<int[]> points = new ArrayList<>();
        int defender = attacker == 1 ? 2 : 1;

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j)) {
                    boardService.placePiece(board, i, j, attacker);
                    if (boardService.checkWin(board, i, j, attacker)) {
                        boardService.removePiece(board, i, j);
                        points.add(new int[]{i, j});
                    } else {
                        boardService.removePiece(board, i, j);
                    }
                }
            }
        }
        return points;
    }

    private List<int[]> getCandidateMoves(int[][] board) {
        List<int[]> candidates = new ArrayList<>();
        boolean[][] visited = new boolean[BoardService.BOARD_SIZE][BoardService.BOARD_SIZE];

        boolean isEmpty = boardService.countPieces(board) == 0;

        if (isEmpty) {
            candidates.add(new int[]{BoardService.BOARD_SIZE / 2, BoardService.BOARD_SIZE / 2});
            return candidates;
        }

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j) && !visited[i][j] && hasNeighbor(board, i, j)) {
                    candidates.add(new int[]{i, j});
                    visited[i][j] = true;
                }
            }
        }

        return candidates;
    }

    private int evaluateTerminal(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (!boardService.isEmpty(board, i, j)) {
                    if (boardService.checkWin(board, i, j, aiValue)) return FIVE;
                    if (boardService.checkWin(board, i, j, playerValue)) return -FIVE;
                }
            }
        }
        return 0;
    }

    private int evaluateBoard(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;
        int aiScore = 0;
        int playerScore = 0;

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j) && hasNeighbor(board, i, j)) {
                    aiScore += evaluatePosition(board, i, j, aiValue);
                    playerScore += evaluatePosition(board, i, j, playerValue);
                }
            }
        }

        return aiScore - (int)(playerScore * 1.1);
    }

    private int evaluatePosition(int[][] board, int x, int y, int player) {
        int score = 0;
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int[] result = countLine(board, x, y, dir[0], dir[1], player);
            int count = result[0];
            int openEnds = result[1];
            int blocked = result[2];

            score += calculatePatternScore(count, openEnds, blocked);
        }

        return score;
    }

    private int[] countLine(int[][] board, int x, int y, int dx, int dy, int player) {
        int count = 1;
        int openEnds = 0;
        int blocked = 0;
        int opponent = player == 1 ? 2 : 1;

        int nx = x + dx, ny = y + dy;
        int forwardSpace = 0;
        while (boardService.isValidPosition(nx, ny)) {
            if (board[nx][ny] == player) {
                count++;
            } else if (boardService.isEmpty(board, nx, ny)) {
                forwardSpace++;
                if (forwardSpace <= 1) {
                    int nnx = nx + dx, nny = ny + dy;
                    if (boardService.isValidPosition(nnx, nny) && board[nnx][nny] == player) {
                        count++;
                        nx = nnx;
                        ny = nny;
                        continue;
                    }
                }
                openEnds++;
                break;
            } else {
                blocked++;
                break;
            }
            nx += dx;
            ny += dy;
        }
        if (!boardService.isValidPosition(nx, ny)) blocked++;

        nx = x - dx;
        ny = y - dy;
        int backwardSpace = 0;
        while (boardService.isValidPosition(nx, ny)) {
            if (board[nx][ny] == player) {
                count++;
            } else if (boardService.isEmpty(board, nx, ny)) {
                backwardSpace++;
                if (backwardSpace <= 1) {
                    int nnx = nx - dx, nny = ny - dy;
                    if (boardService.isValidPosition(nnx, nny) && board[nnx][nny] == player) {
                        count++;
                        nx = nnx;
                        ny = nny;
                        continue;
                    }
                }
                openEnds++;
                break;
            } else {
                blocked++;
                break;
            }
            nx -= dx;
            ny -= dy;
        }
        if (!boardService.isValidPosition(nx, ny)) blocked++;

        return new int[]{count, openEnds, blocked};
    }

    private int calculatePatternScore(int count, int openEnds, int blocked) {
        if (count >= 5) return FIVE;

        if (count == 4) {
            if (openEnds >= 2) return OPEN_FOUR;
            if (openEnds == 1) return BLOCKED_FOUR;
            return 0;
        }

        if (count == 3) {
            if (openEnds >= 2) return OPEN_THREE;
            if (openEnds == 1) return BLOCKED_THREE;
            return 0;
        }

        if (count == 2) {
            if (openEnds >= 2) return OPEN_TWO;
            if (openEnds == 1) return BLOCKED_TWO;
            return 0;
        }

        if (count == 1) {
            if (openEnds >= 2) return OPEN_ONE;
            return 0;
        }

        return 0;
    }

    private int[] findWinningMove(int[][] board, int player) {
        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j)) {
                    boardService.placePiece(board, i, j, player);
                    if (boardService.checkWin(board, i, j, player)) {
                        boardService.removePiece(board, i, j);
                        return new int[]{i, j};
                    }
                    boardService.removePiece(board, i, j);
                }
            }
        }
        return null;
    }

    private int[] findUrgentMove(int[][] board, int player) {
        int bestScore = 0;
        int[] bestMove = null;

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j) && hasNeighbor(board, i, j)) {
                    int score = evaluatePosition(board, i, j, player);
                    if (score >= BLOCKED_THREE && score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{i, j};
                    }
                }
            }
        }
        return bestMove;
    }

    private int[] findScoredMoveWithRandom(int[][] board, int aiValue, double randomFactor) {
        List<int[]> scoredMoves = new ArrayList<>();

        for (int i = 0; i < BoardService.BOARD_SIZE; i++) {
            for (int j = 0; j < BoardService.BOARD_SIZE; j++) {
                if (boardService.isEmpty(board, i, j) && hasNeighbor(board, i, j)) {
                    int score = evaluatePosition(board, i, j, aiValue);
                    scoredMoves.add(new int[]{i, j, score});
                }
            }
        }

        if (scoredMoves.isEmpty()) {
            return new int[]{BoardService.BOARD_SIZE / 2, BoardService.BOARD_SIZE / 2};
        }

        scoredMoves.sort((a, b) -> b[2] - a[2]);

        int topN = Math.min(5, scoredMoves.size());
        if (Math.random() < randomFactor && topN > 1) {
            int idx = new Random().nextInt(topN);
            int[] chosen = scoredMoves.get(idx);
            return new int[]{chosen[0], chosen[1]};
        }

        int[] best = scoredMoves.get(0);
        return new int[]{best[0], best[1]};
    }

    private boolean hasNeighbor(int[][] board, int x, int y) {
        return boardService.hasNeighbor(board, x, y, 2);
    }
}
