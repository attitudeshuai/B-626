package com.gomoku.service;

import org.springframework.stereotype.Service;

@Service
public class BoardService {

    public static final int BOARD_SIZE = 15;

    public int[][] createEmptyBoard() {
        return new int[BOARD_SIZE][BOARD_SIZE];
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public boolean isEmpty(int[][] board, int x, int y) {
        return board[x][y] == 0;
    }

    public void placePiece(int[][] board, int x, int y, int playerValue) {
        board[x][y] = playerValue;
    }

    public void removePiece(int[][] board, int x, int y) {
        board[x][y] = 0;
    }

    public boolean checkWin(int[][] board, int x, int y, int player) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;

            int nx = x + dir[0];
            int ny = y + dir[1];
            while (isValidPosition(nx, ny) && board[nx][ny] == player) {
                count++;
                nx += dir[0];
                ny += dir[1];
            }

            nx = x - dir[0];
            ny = y - dir[1];
            while (isValidPosition(nx, ny) && board[nx][ny] == player) {
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

    public boolean hasNeighbor(int[][] board, int x, int y, int range) {
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (isValidPosition(nx, ny) && board[nx][ny] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countPieces(int[][] board) {
        int count = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != 0) count++;
            }
        }
        return count;
    }

    public int[][] copyBoard(int[][] board) {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }
}
