package com.xcz.controller;

import com.xcz.pojo.Users;
import com.xcz.pojo.vo.UsersVO;
import com.xcz.service.UserService;
import com.xcz.utils.IMoocJSONResult;
import com.xcz.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Api(value = "用户注册和登录的接口",tags = {"用户注册和登录的Controller"})
public class RegistLoginController extends BasicController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户注册",notes ="用户注册的接口")
    @PostMapping("regist")
    public IMoocJSONResult regist(@RequestBody Users user) throws Exception {

        //先判断用户名密码是否为空
        if(StringUtils.isBlank(user.getUsername())||StringUtils.isBlank(user.getPassword())){
            return IMoocJSONResult.errorMsg("用户名和密码不能为空");
        }
        //判断用户是否存在
        if(userService.queryUsernameIsExist(user.getUsername())){
            return IMoocJSONResult.errorMsg("用户名已被注册");
        }else{
            //注册用户
            user.setNickname(user.getUsername());
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            user.setFansCounts(0);
            user.setFollowCounts(0);
            user.setReceiveLikeCounts(0);
            userService.saveUser(user);
        }
        return IMoocJSONResult.ok();
    }

    @ApiOperation(value = "用户登录",notes ="用户登录的接口")
    @PostMapping("login")
    public IMoocJSONResult login(@RequestBody Users user) throws Exception {
        String username = user.getUsername();
        String password = user.getPassword();
        //先判断用户名密码是否为空
        if(StringUtils.isBlank(username)||StringUtils.isBlank(password)){
            return IMoocJSONResult.errorMsg("用户名和密码不能为空");
        }
        //判断用户是否存在
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(user.getPassword()));
        if(userResult!=null){
            String token = UUID.randomUUID().toString();
            redis.set(USER_REDIS_SESSION + ":" + userResult.getId(), token, 1000 * 60 * 30);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(userResult,userVO);
            userVO.setUserToken(token);
            return IMoocJSONResult.ok(userVO);
        }else{
            return IMoocJSONResult.errorMsg("用户或密码错误");
        }
    }

    @ApiOperation(value="用户注销", notes="用户注销的接口")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    @PostMapping("/logout")
    public IMoocJSONResult logout(String userId) throws Exception {
        redis.del(USER_REDIS_SESSION + ":" + userId);
        return IMoocJSONResult.ok("用户已注销");
    }
}
