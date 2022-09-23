package com.tingnichui.service;

import com.tingnichui.pojo.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Tingnichui
* @description 针对表【t_user】的数据库操作Service
* @createDate 2022-09-23 21:22:52
*/
public interface UserService extends IService<User> {

    /**
     * 登录功能
     * @param mobile 手机号
     * @param password 密码
     * @return 生成的JWT的token
     */
    String login(String mobile,String password);

    User register(String mobile, String password);
}
