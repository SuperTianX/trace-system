package com.steel.trace.service;

import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.common.util.JwtUtil;
import com.steel.trace.dto.request.LoginRequest;
import com.steel.trace.entity.User;
import com.steel.trace.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = JwtUtil.generateToken(user.getUsername(), user.getId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("userId", user.getId());
        return result;
    }

    public String refreshToken(String token) {
        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(401, "刷新令牌已过期");
        }
        String username = JwtUtil.getUsername(token);
        return JwtUtil.generateToken(username, JwtUtil.getUserId(token));
    }

    public void register(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
    }

    public User getCurrentUser(String username) {
        return userMapper.selectByUsername(username);
    }
}
