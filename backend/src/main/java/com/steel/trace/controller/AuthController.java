package com.steel.trace.controller;

import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.JwtUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.LoginRequest;
import com.steel.trace.entity.Menu;
import com.steel.trace.entity.RoleMenu;
import com.steel.trace.entity.User;
import com.steel.trace.entity.UserRole;
import com.steel.trace.mapper.MenuMapper;
import com.steel.trace.mapper.RoleMenuMapper;
import com.steel.trace.mapper.UserRoleMapper;
import com.steel.trace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    @PostMapping("/login")
    @OperationLog(module = "认证", action = "登录", description = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    @OperationLog(module = "认证", action = "注册", description = "用户注册")
    public Result<Void> register(@Valid @RequestBody User user) {
        authService.register(user);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<User> me(@RequestHeader("Authorization") String token) {
        String username = JwtUtil.getUsername(token.replace("Bearer ", ""));
        return Result.success(authService.getCurrentUser(username));
    }

    /**
     * 获取当前用户的菜单权限码列表
     */
    @GetMapping("/permissions")
    public Result<List<String>> permissions(@RequestHeader("Authorization") String token) {
        String username = JwtUtil.getUsername(token.replace("Bearer ", ""));
        User user = authService.getCurrentUser(username);
        if (user == null) return Result.success(List.of());

        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId()));

        // 查询角色菜单权限
        List<String> permissionCodes = new java.util.ArrayList<>();
        for (UserRole ur : userRoles) {
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleMenu>()
                            .eq(RoleMenu::getRoleId, ur.getRoleId()));
            List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
            if (!menuIds.isEmpty()) {
                List<Menu> menus = menuMapper.selectBatchIds(menuIds);
                for (Menu m : menus) {
                    if (m.getPermissionCode() != null && !m.getPermissionCode().isEmpty()) {
                        permissionCodes.add(m.getPermissionCode());
                    }
                }
            }
        }
        return Result.success(permissionCodes.stream().distinct().collect(Collectors.toList()));
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String newToken = authService.refreshToken(body.get("refreshToken"));
        return Result.success(Map.of("token", newToken));
    }
}
