package com.tao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tao.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
