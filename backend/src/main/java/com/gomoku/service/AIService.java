package com.gomoku.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIService {

    private final BoardService boardService;

    public AIService(BoardService boardService) {
        this.boardService = boardService;
    }

    private static final int BOARD_SIZE = BoardService.BOARD_SIZE;

    // 棋型分数常量 - 大幅提升
    private static final int FIVE = 10000000;           // 连五
    private static final int OPEN_FOUR = 1000000;       // 活四
    private static final int BLOCKED_FOUR = 100000;     // 冲四
    private static final int OPEN_THREE = 50000;        // 活三
    private static final int BLOCKED_THREE = 5000;      // 眠三
    private static final int OPEN_TWO = 3000;           // 活二
    private static final int BLOCKED_TWO = 500;         // 眠二
    private static final int OPEN_ONE = 100;            // 活一

    // 难度对应的搜索深度
    private static final int EASY_DEPTH = 1;
    private static final int MEDIUM_DEPTH = 3;
    private static final int HARD_DEPTH = 5;

    public int[] getAIMove(int[][] board, String difficulty, String aiColor) {
        int aiValue = "BLACK".equals(aiColor) ? 1 : 2;

        return switch (difficulty.toUpperCase()) {
            case "MEDIUM" -> getMediumMove(board, aiValue);
            case "HARD" -> getHardMove(board, aiValue);
            default -> getEasyMove(board, aiValue);
        };
    }

    // ==================== Easy 难度 ====================
    // 使用简单评分 + 一定随机性，但会正确防守和进攻
    private int[] getEasyMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        // 必须赢的棋
        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        // 必须防守的棋
        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        // 检查对方活三/冲四，必须防守
        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null && Math.random() > 0.3) {
            return urgentBlock;
        }

        // 找自己的进攻点
        int[] attackMove = findUrgentMove(board, aiValue);
        if (attackMove != null && Math.random() > 0.4) {
            return attackMove;
        }

        // 使用评分找最佳位置，加入随机性
        return findScoredMoveWithRandom(board, aiValue, 0.3);
    }

    // ==================== Medium 难度 ====================
    // 使用浅层Minimax + 更好的评估
    private int[] getMediumMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        // 必须赢的棋
        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        // 必须防守的棋
        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        // 检查双方活三/冲四
        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null) return urgentBlock;

        int[] attackMove = findUrgentMove(board, aiValue);
        if (attackMove != null) return attackMove;

        // 使用Minimax
        return minimaxSearch(board, aiValue, MEDIUM_DEPTH);
    }

    // ==================== Hard 难度 ====================
    // 深层Minimax + VCT威胁搜索
    private int[] getHardMove(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        // 必须赢的棋
        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return winMove;

        // 必须防守的棋
        int[] blockMove = findWinningMove(board, playerValue);
        if (blockMove != null) return blockMove;

        // 检查冲四/活三威胁
        int[] urgentBlock = findUrgentMove(board, playerValue);
        if (urgentBlock != null) return urgentBlock;

        // VCT威胁搜索 - 寻找连续进攻序列
        int[] vctMove = findVCTMove(board, aiValue, 4);
        if (vctMove != null) return vctMove;

        // 深层Minimax搜索
        return minimaxSearch(board, aiValue, HARD_DEPTH);
    }

    // ==================== Minimax + Alpha-Beta ====================
    private int[] minimaxSearch(int[][] board, int aiValue, int depth) {
        List<int[]> candidates = getCandidateMoves(board);
        if (candidates.isEmpty()) {
            return new int[]{BOARD_SIZE / 2, BOARD_SIZE / 2};
        }

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // 对候选点排序，优先搜索高分点（提高剪枝效率）
        candidates.sort((a, b) -> {
            int scoreA = evaluatePosition(board, a[0], a[1], aiValue);
            int scoreB = evaluatePosition(board, b[0], b[1], aiValue);
            return scoreB - scoreA;
        });

        // 限制候选数量，提高效率
        int maxCandidates = Math.min(candidates.size(), 20);

        for (int i = 0; i < maxCandidates; i++) {
            int[] move = candidates.get(i);
            int x = move[0], y = move[1];

            board[x][y] = aiValue;
            int score = minimax(board, depth - 1, alpha, beta, false, aiValue);
            board[x][y] = 0;

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

        // 检查终止条件
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

        // 限制候选数量
        int maxCandidates = Math.min(candidates.size(), 12);

        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < maxCandidates; i++) {
                int[] move = candidates.get(i);
                board[move[0]][move[1]] = aiValue;
                int score = minimax(board, depth - 1, alpha, beta, false, aiValue);
                board[move[0]][move[1]] = 0;

                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < maxCandidates; i++) {
                int[] move = candidates.get(i);
                board[move[0]][move[1]] = playerValue;
                int score = minimax(board, depth - 1, alpha, beta, true, aiValue);
                board[move[0]][move[1]] = 0;

                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return minScore;
        }
    }

    // ==================== VCT威胁搜索 ====================
    // 寻找连续进攻获胜的序列
    private int[] findVCTMove(int[][] board, int aiValue, int maxDepth) {
        int playerValue = aiValue == 1 ? 2 : 1;
        List<int[]> threatMoves = findAllThreatMoves(board, aiValue);

        for (int[] move : threatMoves) {
            board[move[0]][move[1]] = aiValue;
            if (canWinByVCT(board, aiValue, playerValue, maxDepth - 1)) {
                board[move[0]][move[1]] = 0;
                return move;
            }
            board[move[0]][move[1]] = 0;
        }
        return null;
    }

    private boolean canWinByVCT(int[][] board, int aiValue, int playerValue, int depth) {
        if (depth <= 0) return false;

        // 检查是否有必胜
        int[] winMove = findWinningMove(board, aiValue);
        if (winMove != null) return true;

        // 对方必须防守的点
        List<int[]> defensePoints = findAllDefensePoints(board, aiValue);
        if (defensePoints.isEmpty()) return false;

        // 如果有多个必防点，对方无法全部防守
        if (defensePoints.size() >= 2) return true;

        // 对方防守后，继续寻找威胁
        int[] defensePoint = defensePoints.get(0);
        board[defensePoint[0]][defensePoint[1]] = playerValue;

        List<int[]> nextThreats = findAllThreatMoves(board, aiValue);
        for (int[] threat : nextThreats) {
            board[threat[0]][threat[1]] = aiValue;
            if (canWinByVCT(board, aiValue, playerValue, depth - 1)) {
                board[threat[0]][threat[1]] = 0;
                board[defensePoint[0]][defensePoint[1]] = 0;
                return true;
            }
            board[threat[0]][threat[1]] = 0;
        }

        board[defensePoint[0]][defensePoint[1]] = 0;
        return false;
    }

    private List<int[]> findAllThreatMoves(int[][] board, int player) {
        List<int[]> threats = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0 && hasNeighbor(board, i, j)) {
                    int score = evaluatePosition(board, i, j, player);
                    if (score >= BLOCKED_THREE) {
                        threats.add(new int[]{i, j, score});
                    }
                }
            }
        }
        // 按分数排序
        threats.sort((a, b) -> b[2] - a[2]);
        return threats.stream().map(t -> new int[]{t[0], t[1]}).toList();
    }

    private List<int[]> findAllDefensePoints(int[][] board, int attacker) {
        List<int[]> points = new ArrayList<>();
        int defender = attacker == 1 ? 2 : 1;

        // 寻找攻击方的冲四点
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = attacker;
                    if (boardService.checkWin(board, i, j, attacker)) {
                        board[i][j] = 0;
                        points.add(new int[]{i, j});
                    } else {
                        board[i][j] = 0;
                    }
                }
            }
        }
        return points;
    }

    // ==================== 候选点生成 ====================
    private List<int[]> getCandidateMoves(int[][] board) {
        List<int[]> candidates = new ArrayList<>();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];

        // 检查是否为空棋盘
        boolean isEmpty = true;
        for (int i = 0; i < BOARD_SIZE && isEmpty; i++) {
            for (int j = 0; j < BOARD_SIZE && isEmpty; j++) {
                if (board[i][j] != 0) isEmpty = false;
            }
        }

        if (isEmpty) {
            candidates.add(new int[]{BOARD_SIZE / 2, BOARD_SIZE / 2});
            return candidates;
        }

        // 找到所有有邻居的空位
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0 && !visited[i][j] && hasNeighbor(board, i, j)) {
                    candidates.add(new int[]{i, j});
                    visited[i][j] = true;
                }
            }
        }

        return candidates;
    }

    // ==================== 评估函数 ====================
    private int evaluateTerminal(int[][] board, int aiValue) {
        int playerValue = aiValue == 1 ? 2 : 1;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != 0) {
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

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0 && hasNeighbor(board, i, j)) {
                    aiScore += evaluatePosition(board, i, j, aiValue);
                    playerScore += evaluatePosition(board, i, j, playerValue);
                }
            }
        }

        // AI得分 - 对手得分 * 1.1（稍微偏向防守）
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

        // 正方向
        int nx = x + dx, ny = y + dy;
        int forwardSpace = 0;
        while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
            if (board[nx][ny] == player) {
                count++;
            } else if (board[nx][ny] == 0) {
                forwardSpace++;
                if (forwardSpace <= 1) {
                    // 检查跳连
                    int nnx = nx + dx, nny = ny + dy;
                    if (nnx >= 0 && nnx < BOARD_SIZE && nny >= 0 && nny < BOARD_SIZE && board[nnx][nny] == player) {
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
        if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE) blocked++;

        // 反方向
        nx = x - dx;
        ny = y - dy;
        int backwardSpace = 0;
        while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
            if (board[nx][ny] == player) {
                count++;
            } else if (board[nx][ny] == 0) {
                backwardSpace++;
                if (backwardSpace <= 1) {
                    int nnx = nx - dx, nny = ny - dy;
                    if (nnx >= 0 && nnx < BOARD_SIZE && nny >= 0 && nny < BOARD_SIZE && board[nnx][nny] == player) {
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
        if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE) blocked++;

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

    // ==================== 辅助方法 ====================
    private int[] findWinningMove(int[][] board, int player) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = player;
                    if (boardService.checkWin(board, i, j, player)) {
                        board[i][j] = 0;
                        return new int[]{i, j};
                    }
                    board[i][j] = 0;
                }
            }
        }
        return null;
    }

    private int[] findUrgentMove(int[][] board, int player) {
        int bestScore = 0;
        int[] bestMove = null;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0 && hasNeighbor(board, i, j)) {
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

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0 && hasNeighbor(board, i, j)) {
                    int score = evaluatePosition(board, i, j, aiValue);
                    scoredMoves.add(new int[]{i, j, score});
                }
            }
        }

        if (scoredMoves.isEmpty()) {
            return new int[]{BOARD_SIZE / 2, BOARD_SIZE / 2};
        }

        // 排序
        scoredMoves.sort((a, b) -> b[2] - a[2]);

        // 有一定概率选择非最佳点
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
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < BoardService.BOARD_SIZE && ny >= 0 && ny < BoardService.BOARD_SIZE && board[nx][ny] != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
