package com.gomoku.service;

import org.springframework.stereotype.Service;

@Service
public class BoardRuleService {

    public static final int BOARD_SIZE = 15;

    public boolean checkWin(int[][] board, int x, int y, int player) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;

            int nx = x + dir[0];
            int ny = y + dir[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && board[nx][ny] == player) {
                count++;
                nx += dir[0];
                ny += dir[1];
            }

            nx = x - dir[0];
            ny = y - dir[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && board[nx][ny] == player) {
                count++;
                nx -= dir[0];
                ny -= dir[1];
            }

            if (count >= 5) return true;
        }

        return false;
    }

    public boolean isBoardFull(int[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean isEmptyCell(int[][] board, int x, int y) {
        return isValidPosition(x, y) && board[x][y] == 0;
    }

    public int colorToValue(String color) {
        return "BLACK".equals(color) ? 1 : 2;
    }

    public String valueToColor(int value) {
        return value == 1 ? "BLACK" : "WHITE";
    }

    public String oppositeColor(String color) {
        return "BLACK".equals(color) ? "WHITE" : "BLACK";
    }
}
