package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.entity.User;
import com.starrating.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    public User login(String username, String password) {
        return this.query()
                .eq("username", username)
                .eq("password", password) // 后续集成 Security 后更换为加密验证
                .one();
    }
}
