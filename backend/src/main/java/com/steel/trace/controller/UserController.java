package com.steel.trace.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.entity.Role;
import com.steel.trace.entity.User;
import com.steel.trace.entity.UserRole;
import com.steel.trace.mapper.RoleMapper;
import com.steel.trace.mapper.UserMapper;
import com.steel.trace.mapper.UserRoleMapper;
import com.steel.trace.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @PostMapping
    @OperationLog(module = "系统管理", action = "新增用户", description = "新增系统用户")
    public Result<Void> create(@RequestBody User user) {
        authService.register(user);
        // Insert role associations
        if (user.getRoleIds() != null) {
            for (Long roleId : user.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.like(username != null && !username.isEmpty(), User::getUsername, username);
        qw.like(realName != null && !realName.isEmpty(), User::getRealName, realName);
        qw.orderByDesc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), qw);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // Query roles
        List<UserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        if (!urs.isEmpty()) {
            List<Long> roleIds = urs.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            user.setRoles(roles);
        }
        return Result.success(user);
    }

    @PutMapping("/{id}")
    @OperationLog(module = "系统管理", action = "修改用户", description = "修改用户信息")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // Do not update password through this endpoint
        userMapper.updateById(user);
        // Update role associations
        if (user.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            for (Long roleId : user.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "系统管理", action = "删除用户", description = "删除系统用户")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<User> list = userMapper.selectList(null);
        ExcelUtil.export(response, "用户数据", User.class, list);
    }
}
