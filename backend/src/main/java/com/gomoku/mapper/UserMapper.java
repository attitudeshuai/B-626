package com.gomoku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gomoku.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE total_games >= 5 ORDER BY (wins * 1.0 / total_games) DESC LIMIT 10")
    List<User> findTopByWinRate();

    @Select("SELECT * FROM users ORDER BY wins DESC LIMIT 10")
    List<User> findTopByWins();
}
