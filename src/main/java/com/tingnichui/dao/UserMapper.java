package com.tingnichui.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
