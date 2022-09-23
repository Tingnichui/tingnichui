package com.tingnichui.controller;

import com.tingnichui.pojo.po.User;
import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.UserService;
import com.tingnichui.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Geng Hui
 * @date 2022/9/23 21:45
 */
@Api(tags = "用户")
@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Result register(@RequestParam String mobile,@RequestParam String password) {
        User user = userService.register(mobile,password);
        if (user == null) {
            return ResultGenerator.fail("注册失败");
        }
        return ResultGenerator.success("注册成功");
    }


    @ApiOperation("用户登录")
    @PostMapping("/login/{mobile}/{password}")
    public Result login(@PathVariable String mobile,@PathVariable String password) {
        String token = userService.login(mobile, password);

        if (StringUtils.isBlank(token)) {
            return ResultGenerator.fail("用户名或密码错误");
        }

        return ResultGenerator.success(token);
    }


}
