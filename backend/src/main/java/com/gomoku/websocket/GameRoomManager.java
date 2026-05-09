package com.gomoku.websocket;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRoomManager {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final Map<Long, String> userRoomMap = new ConcurrentHashMap<>();
    private final Queue<Long> matchQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Map<Long, String> matchingUsers = new ConcurrentHashMap<>();

    public GameRoom createRoom(String roomName, Long hostId, String hostNickname) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        GameRoom room = new GameRoom(roomId, roomName, hostId, hostNickname);
        rooms.put(roomId, room);
        userRoomMap.put(hostId, roomId);
        return room;
    }

    public GameRoom joinRoom(String roomId, Long guestId, String guestNickname) {
        GameRoom room = rooms.get(roomId);
        if (room != null && !room.isFull() && !room.getHostId().equals(guestId)) {
            room.setGuestId(guestId);
            room.setGuestNickname(guestNickname);
            userRoomMap.put(guestId, roomId);
            return room;
        }
        return null;
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public GameRoom getRoomByUserId(Long userId) {
        String roomId = userRoomMap.get(userId);
        if (roomId != null) {
            return rooms.get(roomId);
        }
        return null;
    }

    public void leaveRoom(Long userId) {
        String roomId = userRoomMap.remove(userId);
        if (roomId != null) {
            GameRoom room = rooms.get(roomId);
            if (room != null) {
                if (room.getHostId().equals(userId)) {
                    if (room.getGuestId() != null) {
                        userRoomMap.remove(room.getGuestId());
                    }
                    rooms.remove(roomId);
                } else if (room.getGuestId() != null && room.getGuestId().equals(userId)) {
                    room.setGuestId(null);
                    room.setGuestNickname(null);
                    if ("PLAYING".equals(room.getStatus())) {
                        room.setStatus("FINISHED");
                        room.setWinner(room.getHostColor());
                    }
                }
            }
        }
    }

    public void removeRoom(String roomId) {
        GameRoom room = rooms.remove(roomId);
        if (room != null) {
            userRoomMap.remove(room.getHostId());
            if (room.getGuestId() != null) {
                userRoomMap.remove(room.getGuestId());
            }
        }
    }

    public List<GameRoom> getWaitingRooms() {
        List<GameRoom> waitingRooms = new ArrayList<>();
        for (GameRoom room : rooms.values()) {
            if ("WAITING".equals(room.getStatus()) && !room.isFull()) {
                waitingRooms.add(room);
            }
        }
        return waitingRooms;
    }

    public void addToMatchQueue(Long userId, String nickname) {
        if (!matchingUsers.containsKey(userId)) {
            matchQueue.add(userId);
            matchingUsers.put(userId, nickname);
        }
    }

    public void removeFromMatchQueue(Long userId) {
        matchQueue.remove(userId);
        matchingUsers.remove(userId);
    }

    public Long[] tryMatch(Long userId) {
        if (matchQueue.size() >= 2) {
            Long player1 = matchQueue.poll();
            Long player2 = matchQueue.poll();
            if (player1 != null && player2 != null) {
                matchingUsers.remove(player1);
                matchingUsers.remove(player2);
                return new Long[]{player1, player2};
            }
        }
        return null;
    }

    public boolean isInMatchQueue(Long userId) {
        return matchingUsers.containsKey(userId);
    }

    public String getMatchingNickname(Long userId) {
        return matchingUsers.get(userId);
    }

    public int getMatchQueueSize() {
        return matchQueue.size();
    }
}
