package com.steel.trace.service;

import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.LoginRequest;
import com.steel.trace.entity.User;
import com.steel.trace.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userMapper, passwordEncoder);
    }

    @Test
    void login_shouldSucceed() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setStatus(1);
        user.setRealName("管理员");

        when(userMapper.selectByUsername(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        var result = authService.login(req);
        assertNotNull(result);
        assertEquals("admin", result.get("username"));
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setStatus(1);

        when(userMapper.selectByUsername(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void login_shouldFailWithUnknownUser() {
        when(userMapper.selectByUsername(anyString())).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setUsername("unknown");
        req.setPassword("123456");

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void login_shouldFailWhenDisabled() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setStatus(0); // disabled

        when(userMapper.selectByUsername(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void register_shouldSucceed() {
        when(userMapper.selectByUsername(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userMapper.insert(any())).thenReturn(1);

        User user = new User();
        user.setUsername("newuser");
        user.setPassword("rawPassword");

        assertDoesNotThrow(() -> authService.register(user));
        verify(userMapper).insert(user);
    }

    @Test
    void register_shouldFailWhenUsernameExists() {
        when(userMapper.selectByUsername(anyString())).thenReturn(new User());

        User user = new User();
        user.setUsername("existing");
        user.setPassword("rawPassword");

        assertThrows(BusinessException.class, () -> authService.register(user));
    }

    @Test
    void getCurrentUser_shouldReturnUser() {
        User user = new User();
        user.setUsername("admin");
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        User result = authService.getCurrentUser("admin");
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }
}
