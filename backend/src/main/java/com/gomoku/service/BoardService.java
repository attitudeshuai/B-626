package com.gomoku.service;

import org.springframework.stereotype.Service;

@Service
public class BoardService {

    public static final int BOARD_SIZE = 15;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    private static final int[][] DIRECTIONS = {
        {1, 0},   // 水平
        {0, 1},   // 垂直
        {1, 1},   // 主对角线
        {1, -1}   // 反对角线
    };

    public int[][] createEmptyBoard() {
        return new int[BOARD_SIZE][BOARD_SIZE];
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean canPlace(int[][] board, int x, int y) {
        return isValidPosition(x, y) && board[x][y] == EMPTY;
    }

    public void placePiece(int[][] board, int x, int y, int player) {
        board[x][y] = player;
    }

    public void removePiece(int[][] board, int x, int y) {
        board[x][y] = EMPTY;
    }

    public boolean checkWin(int[][] board, int x, int y, int player) {
        for (int[] dir : DIRECTIONS) {
            if (countConsecutive(board, x, y, dir[0], dir[1], player) >= 5) {
                return true;
            }
        }
        return false;
    }

    public boolean isBoardFull(int[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getNextPlayer(int currentPlayer) {
        return currentPlayer == BLACK ? WHITE : BLACK;
    }

    public String colorToValue(String color) {
        return "BLACK".equals(color) ? "BLACK" : "WHITE";
    }

    public int stringToValue(String color) {
        return "BLACK".equals(color) ? BLACK : WHITE;
    }

    public String valueToString(int value) {
        return value == BLACK ? "BLACK" : "WHITE";
    }

    private int countConsecutive(int[][] board, int x, int y, int dx, int dy, int player) {
        int count = 1;
        count += countDirection(board, x, y, dx, dy, player);
        count += countDirection(board, x, y, -dx, -dy, player);
        return count;
    }

    private int countDirection(int[][] board, int x, int y, int dx, int dy, int player) {
        int count = 0;
        int nx = x + dx;
        int ny = y + dy;
        while (isValidPosition(nx, ny) && board[nx][ny] == player) {
            count++;
            nx += dx;
            ny += dy;
        }
        return count;
    }
}
