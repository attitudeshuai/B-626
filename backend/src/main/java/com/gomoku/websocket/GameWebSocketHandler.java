package com.gomoku.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomoku.service.AIService;
import com.gomoku.service.BoardService;
import com.gomoku.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameRoomManager roomManager;
    private final AIService aiService;
    private final BoardService boardService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameRoomManager roomManager, AIService aiService, 
                                BoardService boardService, UserService userService) {
        this.roomManager = roomManager;
        this.aiService = aiService;
        this.boardService = boardService;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            sendMessage(session, createMessage("connected", Map.of("userId", userId)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            roomManager.removeFromMatchQueue(userId);
            handlePlayerDisconnect(userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String nickname = (String) session.getAttributes().get("nickname");

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        switch (type) {
            case "getRooms" -> handleGetRooms(session);
            case "createRoom" -> handleCreateRoom(session, userId, nickname, payload);
            case "joinRoom" -> handleJoinRoom(session, userId, nickname, payload);
            case "leaveRoom" -> handleLeaveRoom(session, userId);
            case "startMatch" -> handleStartMatch(session, userId, nickname);
            case "cancelMatch" -> handleCancelMatch(session, userId);
            case "ready" -> handleReady(session, userId);
            case "move" -> handleMove(session, userId, payload);
            case "requestUndo" -> handleRequestUndo(session, userId);
            case "respondUndo" -> handleRespondUndo(session, userId, payload);
            case "surrender" -> handleSurrender(session, userId);
            case "chat" -> handleChat(session, userId, nickname, payload);
        }
    }

    private void handleGetRooms(WebSocketSession session) {
        List<GameRoom> rooms = roomManager.getWaitingRooms();
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (GameRoom room : rooms) {
            roomList.add(Map.of(
                "roomId", room.getRoomId(),
                "roomName", room.getRoomName(),
                "hostNickname", room.getHostNickname()
            ));
        }
        sendMessage(session, createMessage("roomList", Map.of("rooms", roomList)));
    }

    private void handleCreateRoom(WebSocketSession session, Long userId, String nickname, Map<String, Object> payload) {
        String roomName = (String) payload.getOrDefault("roomName", nickname + "的房间");
        GameRoom room = roomManager.createRoom(roomName, userId, nickname);
        sendMessage(session, createMessage("roomCreated", Map.of(
            "roomId", room.getRoomId(),
            "roomName", room.getRoomName()
        )));
    }

    private void handleJoinRoom(WebSocketSession session, Long userId, String nickname, Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        GameRoom room = roomManager.joinRoom(roomId, userId, nickname);
        if (room != null) {
            sendMessage(session, createMessage("roomJoined", Map.of(
                "roomId", room.getRoomId(),
                "roomName", room.getRoomName(),
                "hostNickname", room.getHostNickname()
            )));
            WebSocketSession hostSession = sessions.get(room.getHostId());
            if (hostSession != null) {
                sendMessage(hostSession, createMessage("playerJoined", Map.of(
                    "guestNickname", nickname
                )));
            }
        } else {
            sendMessage(session, createMessage("error", Map.of("message", "无法加入房间")));
        }
    }

    private void handleLeaveRoom(WebSocketSession session, Long userId) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room != null) {
            Long opponentId = room.getOpponentId(userId);
            boolean wasHost = room.getHostId().equals(userId);
            roomManager.leaveRoom(userId);

            if (opponentId != null) {
                WebSocketSession opponentSession = sessions.get(opponentId);
                if (opponentSession != null) {
                    if (wasHost) {
                        sendMessage(opponentSession, createMessage("roomClosed", Map.of("message", "房主已离开")));
                    } else {
                        sendMessage(opponentSession, createMessage("playerLeft", Map.of()));
                    }
                }
            }
        }
        sendMessage(session, createMessage("leftRoom", Map.of()));
    }

    private void handleStartMatch(WebSocketSession session, Long userId, String nickname) {
        roomManager.addToMatchQueue(userId, nickname);
        sendMessage(session, createMessage("matchStarted", Map.of("queueSize", roomManager.getMatchQueueSize())));
        tryMatch();
    }

    private void handleCancelMatch(WebSocketSession session, Long userId) {
        roomManager.removeFromMatchQueue(userId);
        sendMessage(session, createMessage("matchCancelled", Map.of()));
    }

    private void tryMatch() {
        while (roomManager.getMatchQueueSize() >= 2) {
            Iterator<Long> iter = new ArrayList<>(sessions.keySet()).iterator();
            Long player1 = null, player2 = null;
            String nick1 = null, nick2 = null;

            for (Long userId : sessions.keySet()) {
                if (roomManager.isInMatchQueue(userId)) {
                    if (player1 == null) {
                        player1 = userId;
                        nick1 = roomManager.getMatchingNickname(userId);
                    } else {
                        player2 = userId;
                        nick2 = roomManager.getMatchingNickname(userId);
                        break;
                    }
                }
            }

            if (player1 != null && player2 != null) {
                roomManager.removeFromMatchQueue(player1);
                roomManager.removeFromMatchQueue(player2);

                GameRoom room = roomManager.createRoom("匹配对局", player1, nick1);
                roomManager.joinRoom(room.getRoomId(), player2, nick2);
                room.startGame();

                WebSocketSession session1 = sessions.get(player1);
                WebSocketSession session2 = sessions.get(player2);

                Map<String, Object> gameInfo1 = createGameInfo(room, player1);
                Map<String, Object> gameInfo2 = createGameInfo(room, player2);

                if (session1 != null) {
                    sendMessage(session1, createMessage("matchFound", gameInfo1));
                }
                if (session2 != null) {
                    sendMessage(session2, createMessage("matchFound", gameInfo2));
                }
            } else {
                break;
            }
        }
    }

    private void handleReady(WebSocketSession session, Long userId) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room != null && room.isFull() && "WAITING".equals(room.getStatus())) {
            room.startGame();

            WebSocketSession hostSession = sessions.get(room.getHostId());
            WebSocketSession guestSession = sessions.get(room.getGuestId());

            if (hostSession != null) {
                sendMessage(hostSession, createMessage("gameStart", createGameInfo(room, room.getHostId())));
            }
            if (guestSession != null) {
                sendMessage(guestSession, createMessage("gameStart", createGameInfo(room, room.getGuestId())));
            }
        }
    }

    private void handleMove(WebSocketSession session, Long userId, Map<String, Object> payload) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room == null || !"PLAYING".equals(room.getStatus())) {
            sendMessage(session, createMessage("error", Map.of("message", "游戏未开始")));
            return;
        }

        if (!room.isPlayerTurn(userId)) {
            sendMessage(session, createMessage("error", Map.of("message", "不是你的回合")));
            return;
        }

        int x = (int) payload.get("x");
        int y = (int) payload.get("y");

        if (!boardService.isValidPosition(x, y) || !boardService.isEmpty(room.getBoard(), x, y)) {
            sendMessage(session, createMessage("error", Map.of("message", "无效的落子位置")));
            return;
        }

        int playerValue = "BLACK".equals(room.getCurrentPlayer()) ? 1 : 2;
        boardService.placePiece(room.getBoard(), x, y, playerValue);

        Map<String, Object> move = new HashMap<>();
        move.put("x", x);
        move.put("y", y);
        move.put("color", room.getCurrentPlayer());
        move.put("step", room.getMoves().size() + 1);
        room.getMoves().add(move);

        Map<String, Object> moveData = Map.of(
            "x", x,
            "y", y,
            "color", room.getCurrentPlayer(),
            "step", room.getMoves().size(),
            "board", room.getBoard()
        );

        if (boardService.checkWin(room.getBoard(), x, y, playerValue)) {
            room.setStatus("FINISHED");
            room.setWinner(room.getCurrentPlayer());

            String winnerColor = room.getCurrentPlayer();
            Long winnerId = room.getPlayerColor(room.getHostId()).equals(winnerColor)
                ? room.getHostId() : room.getGuestId();
            Long loserId = winnerId.equals(room.getHostId()) ? room.getGuestId() : room.getHostId();

            userService.updateGameStats(winnerId, "WIN");
            userService.updateGameStats(loserId, "LOSE");

            broadcastToRoom(room, createMessage("gameOver", Map.of(
                "winner", winnerColor,
                "move", moveData
            )));
        } else if (boardService.isBoardFull(room.getBoard())) {
            room.setStatus("FINISHED");
            room.setWinner("DRAW");

            userService.updateGameStats(room.getHostId(), "DRAW");
            userService.updateGameStats(room.getGuestId(), "DRAW");

            broadcastToRoom(room, createMessage("gameOver", Map.of(
                "winner", "DRAW",
                "move", moveData
            )));
        } else {
            room.switchPlayer();
            broadcastToRoom(room, createMessage("moveMade", Map.of(
                "move", moveData,
                "currentPlayer", room.getCurrentPlayer()
            )));
        }
    }

    private void handleRequestUndo(WebSocketSession session, Long userId) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room == null || !"PLAYING".equals(room.getStatus())) {
            return;
        }

        if (room.getUndoRemaining(userId) <= 0) {
            sendMessage(session, createMessage("error", Map.of("message", "悔棋次数已用完")));
            return;
        }

        Long opponentId = room.getOpponentId(userId);
        WebSocketSession opponentSession = sessions.get(opponentId);
        if (opponentSession != null) {
            sendMessage(opponentSession, createMessage("undoRequest", Map.of("fromUserId", userId)));
        }
    }

    private void handleRespondUndo(WebSocketSession session, Long userId, Map<String, Object> payload) {
        boolean accept = (boolean) payload.get("accept");
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room == null) return;

        Long requesterId = room.getOpponentId(userId);
        WebSocketSession requesterSession = sessions.get(requesterId);

        if (accept) {
            if (room.getMoves().size() >= 2) {
                room.getMoves().remove(room.getMoves().size() - 1);
                Map<String, Object> lastMove = room.getMoves().remove(room.getMoves().size() - 1);
                int x1 = (int) lastMove.get("x");
                int y1 = (int) lastMove.get("y");
                boardService.removePiece(room.getBoard(), x1, y1);

                if (room.getMoves().size() > 0) {
                    Map<String, Object> prevMove = room.getMoves().get(room.getMoves().size() - 1);
                    int x2 = (int) prevMove.get("x");
                    int y2 = (int) prevMove.get("y");
                    boardService.removePiece(room.getBoard(), x2, y2);
                    room.getMoves().remove(room.getMoves().size() - 1);
                }

                room.incrementUndoCount(requesterId);
                room.switchPlayer();
                room.switchPlayer();

                broadcastToRoom(room, createMessage("undoAccepted", Map.of(
                    "board", room.getBoard(),
                    "currentPlayer", room.getCurrentPlayer(),
                    "undoRemaining", room.getUndoRemaining(requesterId)
                )));
            }
        } else {
            if (requesterSession != null) {
                sendMessage(requesterSession, createMessage("undoRejected", Map.of()));
            }
        }
    }

    private void handleSurrender(WebSocketSession session, Long userId) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room == null || !"PLAYING".equals(room.getStatus())) {
            return;
        }

        String playerColor = room.getPlayerColor(userId);
        String winnerColor = "BLACK".equals(playerColor) ? "WHITE" : "BLACK";
        room.setStatus("FINISHED");
        room.setWinner(winnerColor);

        Long winnerId = room.getOpponentId(userId);
        userService.updateGameStats(winnerId, "WIN");
        userService.updateGameStats(userId, "LOSE");

        broadcastToRoom(room, createMessage("gameOver", Map.of(
            "winner", winnerColor,
            "surrender", true,
            "surrenderBy", playerColor
        )));
    }

    private void handleChat(WebSocketSession session, Long userId, String nickname, Map<String, Object> payload) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room != null) {
            String message = (String) payload.get("message");
            broadcastToRoom(room, createMessage("chat", Map.of(
                "userId", userId,
                "nickname", nickname,
                "message", message,
                "timestamp", System.currentTimeMillis()
            )));
        }
    }

    private void handlePlayerDisconnect(Long userId) {
        GameRoom room = roomManager.getRoomByUserId(userId);
        if (room != null && "PLAYING".equals(room.getStatus())) {
            String playerColor = room.getPlayerColor(userId);
            String winnerColor = "BLACK".equals(playerColor) ? "WHITE" : "BLACK";
            room.setStatus("FINISHED");
            room.setWinner(winnerColor);

            Long winnerId = room.getOpponentId(userId);
            if (winnerId != null) {
                userService.updateGameStats(winnerId, "WIN");
                userService.updateGameStats(userId, "LOSE");

                WebSocketSession winnerSession = sessions.get(winnerId);
                if (winnerSession != null) {
                    sendMessage(winnerSession, createMessage("gameOver", Map.of(
                        "winner", winnerColor,
                        "disconnect", true
                    )));
                }
            }
        }
        roomManager.leaveRoom(userId);
    }

    private Map<String, Object> createGameInfo(GameRoom room, Long userId) {
        Map<String, Object> info = new HashMap<>();
        info.put("roomId", room.getRoomId());
        info.put("board", room.getBoard());
        info.put("currentPlayer", room.getCurrentPlayer());
        info.put("playerColor", room.getPlayerColor(userId));
        info.put("hostNickname", room.getHostNickname());
        info.put("guestNickname", room.getGuestNickname());
        info.put("isHost", room.getHostId().equals(userId));
        info.put("undoRemaining", room.getUndoRemaining(userId));
        return info;
    }

    private void broadcastToRoom(GameRoom room, Map<String, Object> message) {
        WebSocketSession hostSession = sessions.get(room.getHostId());
        WebSocketSession guestSession = sessions.get(room.getGuestId());

        if (hostSession != null) {
            sendMessage(hostSession, message);
        }
        if (guestSession != null) {
            sendMessage(guestSession, message);
        }
    }

    private Map<String, Object> createMessage(String type, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.putAll(data);
        return message;
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
