package com.steel.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.trace.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM tr_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
