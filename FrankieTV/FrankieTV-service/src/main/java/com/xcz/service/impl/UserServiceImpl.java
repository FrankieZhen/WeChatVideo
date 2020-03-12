package com.xcz.service.impl;

import com.xcz.mapper.UsersFansMapper;
import com.xcz.mapper.UsersLikeVideosMapper;
import com.xcz.mapper.UsersMapper;
import com.xcz.mapper.UsersReportMapper;
import com.xcz.pojo.Users;
import com.xcz.pojo.UsersFans;
import com.xcz.pojo.UsersLikeVideos;
import com.xcz.pojo.UsersReport;
import com.xcz.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl  implements UserService {
    @Autowired
    private UsersMapper userMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;
    @Autowired
    private UsersFansMapper usersFansMapper;
    @Autowired
    private UsersReportMapper usersReportMapper;
    /**
     * @Description: 判断用户名是否存在
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username){
        Users user = new Users();
        user.setUsername(username);
        Users users = userMapper.selectOne(user);

        return users == null?false:true;
    }

    /**
     * @Description: 保存用户（注册用户）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUser(Users user) {
        user.setId(sid.nextShort()); //设置全局唯一的id
        userMapper.insert(user);
    }

    /**
     * @Description: 登录验证密码
     */
    /*public boolean queryPasswordForLogin(String username,String password){
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        Users result = userMapper.selectOneByExample(userExample);
        return result.getPassword().equals(password)?true:false;
    }*/

    /**
     * @Description: 验证用户是否存在
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {

        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        Users result = userMapper.selectOneByExample(userExample);

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserInfo(Users user) {

        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", user.getId());
        userMapper.updateByExampleSelective(user, userExample);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfo(String userId) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", userId);
        Users user = userMapper.selectOneByExample(userExample);
        return user;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean isUserLikeVideo(String userId, String videoId) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(videoId)) {
            return false;
        }

        Example example = new Example(UsersLikeVideos.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("videoId", videoId);

        List<UsersLikeVideos> list = usersLikeVideosMapper.selectByExample(example);

        if (list != null && list.size() >0) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserFanRelation(String userId, String fanId) {

        String relId = sid.nextShort();

        UsersFans userFan = new UsersFans();
        userFan.setId(relId);
        userFan.setUserId(userId);
        userFan.setFanId(fanId);

        usersFansMapper.insert(userFan);

        userMapper.addFansCount(userId);
        userMapper.addFollersCount(fanId);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserFanRelation(String userId, String fanId) {

        Example example = new Example(UsersFans.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("fanId", fanId);

        usersFansMapper.deleteByExample(example);

        userMapper.reduceFansCount(userId);
        userMapper.reduceFollersCount(fanId);

    }

    @Override
    public boolean queryIfFollow(String userId, String fanId) {

        Example example = new Example(UsersFans.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("fanId", fanId);

        List<UsersFans> list = usersFansMapper.selectByExample(example);

        if (list != null && !list.isEmpty() && list.size() > 0) {
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void reportUser(UsersReport userReport) {

        String urId = sid.nextShort();
        userReport.setId(urId);
        userReport.setCreateDate(new Date());

        usersReportMapper.insert(userReport);
    }
}
