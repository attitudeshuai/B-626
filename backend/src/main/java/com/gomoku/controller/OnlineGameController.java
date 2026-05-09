package com.gomoku.controller;

import com.gomoku.websocket.GameRoom;
import com.gomoku.websocket.GameRoomManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/online")
public class OnlineGameController {

    private final GameRoomManager roomManager;

    public OnlineGameController(GameRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getRooms() {
        List<GameRoom> rooms = roomManager.getWaitingRooms();
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (GameRoom room : rooms) {
            roomList.add(Map.of(
                "roomId", room.getRoomId(),
                "roomName", room.getRoomName(),
                "hostNickname", room.getHostNickname(),
                "createTime", room.getCreateTime()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "code", 200,
            "data", Map.of("rooms", roomList)
        ));
    }
}
