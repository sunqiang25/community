package com.sjl.community.service;

import com.sjl.community.mapper.UserMapper;
import com.sjl.community.model.User;
import com.sjl.community.model.UserExample;
import com.sjl.community.utils.CodecUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author song
 * @create 2020/2/18 22:19
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RegisterService registerService;

    /**
     * 根据user的accountId判断用户是否存在，存在则更新信息，不存在则创建新用户
     * @param user
     */
    public void createOrUpdateUser(User user) {
        //获取数据库里的user
        UserExample userExample = new UserExample();
        userExample.createCriteria().andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);

        if(users.size() == 0){
            //创建，创建时间修改时间均设置为当前时间
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
        }else{
            User dbUser = users.get(0);
            //更新修改时间和头像
            dbUser.setGmtModified(System.currentTimeMillis());
            dbUser.setName(user.getName());
            dbUser.setBio(user.getBio());
            dbUser.setToken(user.getToken());
            dbUser.setAvatarUrl(user.getAvatarUrl());
            UserExample userExample1 = new UserExample();
            userExample1.createCriteria().andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(dbUser, userExample1);
        }
    }

    public User findById(Long id) {
        return this.userMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新密码
     * @param email
     * @param pwd
     */
    public void updatePwd(String email, String pwd) {

        UserExample example = new UserExample();
        example.createCriteria().andAccountIdEqualTo(email);
        List<User> users = userMapper.selectByExample(example);
        if(users != null && users.size() == 1){
            String salt = CodecUtils.generateSalt();
            pwd = CodecUtils.md5Hex(pwd, salt);
            User user = new User();
            user.setSalt(salt);
            user.setId(users.get(0).getId());
            user.setPassword(pwd);
            boolean b = userMapper.updateByPrimaryKeySelective(user) == 1;
            if(b) {
                //删除验证码
                registerService.deleteCode(email);
            }
        }
    }
}
