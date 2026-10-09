package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 分组
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:10
 **/
public interface FortuneGroupMapper extends BaseMapper<FortuneGroupEntity> {

    @Update("UPDATE fortune_group SET group_id = group_id WHERE group_id = #{groupId} AND deleted = 0")
    int lockGroup(@Param("groupId") Long groupId);
}
