package com.tingnichui.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tingnichui.pojo.po.User;
import com.tingnichui.security.JwtTokenUtil;
import com.tingnichui.security.SecurityUser;
import com.tingnichui.service.UserService;
import com.tingnichui.mapper.UserMapper;
import com.tingnichui.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
* @author Tingnichui
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2022-09-23 21:22:52
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService, UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String login(String mobile, String password) {

        String token = "";
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile).eq(User::getPassword, password));
        if (Objects.nonNull(user)) {
            redisUtil.setCacheObject(user.getUserName(),user,30L, TimeUnit.MINUTES);
            UserDetails userDetails = this.loadUserByUsername(user.getUserName());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, AuthorityUtils.commaSeparatedStringToAuthorityList("user"));
            //将生成的authentication放入容器中，生成安全的上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtTokenUtil.generateToken(userDetails);
        }

        return token;
    }

    @Override
    public User register(String mobile, String password) {
        User user = new User();
        user.setCreateTime(new Date());
        user.setStatus(1);
        user.setMobile(mobile);
//        user.setUserName(RandomUtil.randomString(16));
        user.setUserName(mobile);
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(password);
        user.setPassword(password);
        userMapper.insert(user);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取用户信息
        User user = redisUtil.getCacheObject(username);
        if (user != null) {
            return new SecurityUser(user, AuthorityUtils.commaSeparatedStringToAuthorityList("user"));
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }
}




