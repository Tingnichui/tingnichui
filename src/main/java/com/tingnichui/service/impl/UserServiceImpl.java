package com.tingnichui.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tingnichui.pojo.po.User;
import com.tingnichui.service.UserService;
import com.tingnichui.dao.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author Tingnichui
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2022-09-23 21:22:52
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    public String login(String mobile, String password) {

        String token = "";
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile).eq(User::getPassword, password));
        if (Objects.nonNull(user)) {
            return "success";
        }

        return token;
    }

    @Override
    public User register(String mobile, String password) {
        User user = new User();
        user.setCreateTime(new Date());
        user.setStatus(1);
        user.setMobile(mobile);
        user.setUserName(RandomUtil.randomString(16));
        //将密码进行加密操作
//        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        userMapper.insert(user);
        return user;
    }
}




