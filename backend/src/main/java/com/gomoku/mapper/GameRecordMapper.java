package com.gomoku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gomoku.entity.GameRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {

    @Select("SELECT * FROM game_records WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<GameRecord> findByUserId(Long userId);
}
