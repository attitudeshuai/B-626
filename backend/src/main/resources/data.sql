-- 初始化测试用户数据（密码都是BCrypt加密后的）
-- 所有测试账号密码统一为: test123456 (使用有效的BCrypt哈希值)

INSERT INTO users (username, password, nickname, avatar, total_games, wins, losses, draws) VALUES
('admin', '$2a$10$qX/eOGJhlV1WG30dpp31m.rIPInNXDZo9QIamPbiRu9055.hClmi.', '系统管理员', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop', 20, 15, 4, 1),
('player1', '$2a$10$qX/eOGJhlV1WG30dpp31m.rIPInNXDZo9QIamPbiRu9055.hClmi.', '玩家一号', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop', 30, 18, 10, 2),
('player2', '$2a$10$qX/eOGJhlV1WG30dpp31m.rIPInNXDZo9QIamPbiRu9055.hClmi.', '玩家二号', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop', 25, 12, 11, 2),
('张三', '$2a$10$qX/eOGJhlV1WG30dpp31m.rIPInNXDZo9QIamPbiRu9055.hClmi.', '棋艺大师张三', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop', 50, 40, 8, 2),
('李四', '$2a$10$qX/eOGJhlV1WG30dpp31m.rIPInNXDZo9QIamPbiRu9055.hClmi.', '五子棋爱好者李四', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop', 15, 8, 6, 1)
ON DUPLICATE KEY UPDATE username=username;

-- 插入历史对局记录
INSERT INTO game_records (user_id, game_mode, difficulty, result, player_color, moves, duration) VALUES
(1, 'AI', 'EASY', 'WIN', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2},{"x":8,"y":7,"color":"BLACK","step":3},{"x":8,"y":8,"color":"WHITE","step":4},{"x":9,"y":7,"color":"BLACK","step":5},{"x":9,"y":8,"color":"WHITE","step":6},{"x":10,"y":7,"color":"BLACK","step":7},{"x":10,"y":8,"color":"WHITE","step":8},{"x":11,"y":7,"color":"BLACK","step":9}]', 120),
(1, 'AI', 'MEDIUM', 'LOSE', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":8,"y":8,"color":"WHITE","step":2}]', 180),
(2, 'AI', 'EASY', 'WIN', 'WHITE', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2}]', 90),
(2, 'LOCAL', NULL, 'WIN', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2}]', 150),
(3, 'AI', 'HARD', 'LOSE', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":8,"y":8,"color":"WHITE","step":2}]', 200),
(4, 'AI', 'EASY', 'WIN', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2},{"x":8,"y":7,"color":"BLACK","step":3},{"x":8,"y":8,"color":"WHITE","step":4},{"x":9,"y":7,"color":"BLACK","step":5},{"x":9,"y":8,"color":"WHITE","step":6},{"x":10,"y":7,"color":"BLACK","step":7},{"x":10,"y":8,"color":"WHITE","step":8},{"x":11,"y":7,"color":"BLACK","step":9}]', 100),
(4, 'AI', 'MEDIUM', 'WIN', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":8,"y":8,"color":"WHITE","step":2}]', 160),
(4, 'AI', 'HARD', 'WIN', 'WHITE', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2}]', 240),
(5, 'LOCAL', NULL, 'WIN', 'BLACK', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":7,"y":8,"color":"WHITE","step":2}]', 110),
(5, 'AI', 'EASY', 'LOSE', 'WHITE', '[{"x":7,"y":7,"color":"BLACK","step":1},{"x":8,"y":8,"color":"WHITE","step":2}]', 130);
