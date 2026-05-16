package com.steel.trace.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.entity.Menu;
import com.steel.trace.entity.Role;
import com.steel.trace.entity.RoleMenu;
import com.steel.trace.mapper.MenuMapper;
import com.steel.trace.mapper.RoleMapper;
import com.steel.trace.mapper.RoleMenuMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    @PostMapping
    @OperationLog(module = "系统管理", action = "新增角色", description = "新增系统角色")
    public Result<Void> create(@RequestBody Role role) {
        roleMapper.insert(role);
        return Result.success();
    }

    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(roleMapper.selectList(null));
    }

    @GetMapping("/page")
    public Result<PageResult<Role>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String roleName) {
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
        qw.like(roleName != null && !roleName.isEmpty(), Role::getRoleName, roleName);
        qw.orderByAsc(Role::getId);
        Page<Role> p = roleMapper.selectPage(new Page<>(page, size), qw);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "系统管理", action = "修改角色", description = "修改角色信息")
    public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "系统管理", action = "删除角色", description = "删除系统角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Role> list = roleMapper.selectList(null);
        ExcelUtil.export(response, "角色数据", Role.class, list);
    }

    /**
     * 获取角色的菜单权限ID列表
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        List<RoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        List<Long> menuIds = list.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    /**
     * 分配角色的菜单权限
     */
    @PutMapping("/{id}/menus")
    @OperationLog(module = "系统管理", action = "分配权限", description = "分配角色菜单权限")
    public Result<Void> assignRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        // 清除原有权限
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        // 插入新权限
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        return Result.success();
    }

    /**
     * 获取所有菜单（树形结构数据供前端权限树使用）
     */
    @GetMapping("/menus/tree")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> list = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSortOrder));
        return Result.success(list);
    }
}
