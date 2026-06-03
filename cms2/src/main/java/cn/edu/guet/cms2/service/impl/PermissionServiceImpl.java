package cn.edu.guet.cms2.service.impl;

import cn.edu.guet.cms2.dto.PermissionDTO;
import cn.edu.guet.cms2.entity.Permission;
import cn.edu.guet.cms2.mapper.PermissionMapper;
import cn.edu.guet.cms2.service.PermissionService;
import cn.edu.guet.cms2.util.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public IPage<Permission> getPermissionPage(PageRequest pageRequest) {
        Page<Permission> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();

        String name = pageRequest.getParamValue("name");
        String code = pageRequest.getParamValue("code");
        String menuType = pageRequest.getParamValue("menuType");
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        if (StringUtils.hasText(code)) {
            queryWrapper.like("code", code);
        }
        if (StringUtils.hasText(menuType)) {
            queryWrapper.eq("type", menuType);
        }
        queryWrapper.orderByAsc("parent_id", "sort", "id");

        return permissionMapper.selectPage(page, queryWrapper);
    }
    //新增权限
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Permission createPermission(PermissionDTO permissionDTO) {
        validatePermission(permissionDTO, null);
        ensureCodeAvailable(permissionDTO.getCode(), null);

        Permission permission = new Permission();
        fillPermission(permission, permissionDTO);
        permissionMapper.insert(permission);
        return permissionMapper.selectById(permission.getId());
    }

    //编辑权限
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Permission updatePermission(Integer id, PermissionDTO permissionDTO) {
        if (id == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new IllegalArgumentException("权限不存在");
        }

        validatePermission(permissionDTO, id);
        ensureCodeAvailable(permissionDTO.getCode(), id);
        fillPermission(permission, permissionDTO);
        permissionMapper.updateById(permission);
        return permissionMapper.selectById(id);
    }

    //删除权限
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePermission(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new IllegalArgumentException("权限不存在");
        }

        QueryWrapper<Permission> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", id);
        if (permissionMapper.selectCount(childWrapper) > 0) {
            throw new IllegalArgumentException("当前权限存在子权限，请先删除子权限");
        }

        permissionMapper.deleteById(id);
    }

    //校验逻辑
    private void validatePermission(PermissionDTO permissionDTO, Integer currentPermissionId) {
        if (permissionDTO == null || !StringUtils.hasText(permissionDTO.getName())) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        if (permissionDTO.getMenuType() == null) {
            throw new IllegalArgumentException("权限类型不能为空");
        }

        Integer parentId = permissionDTO.getParentId() == null ? 0 : permissionDTO.getParentId();
        if (currentPermissionId != null && parentId.equals(currentPermissionId)) {
            throw new IllegalArgumentException("父级权限不能选择自己");
        }
        if (parentId > 0) {
            Permission parent = permissionMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("父级权限不存在");
            }
            if (currentPermissionId != null && isDescendant(parentId, currentPermissionId)) {
                throw new IllegalArgumentException("父级权限不能选择自己的子权限");
            }
        }
    }

    //权限树构建
    private List<Permission> buildPermissionTree(List<Permission> permissions) {
        List<Permission> tree = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0) {
                tree.add(findChildren(permission, permissions));
            }
        }
        tree.sort(Comparator.comparing(Permission::getSort, Comparator.nullsLast(Integer::compareTo)));
        return tree;
    }

    private Permission findChildren(Permission parent, List<Permission> permissions) {
        parent.setChildren(permissions.stream()
                .filter(permission -> parent.getId().equals(permission.getParentId()))
                .map(permission -> findChildren(permission, permissions))
                .sorted(Comparator.comparing(Permission::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList()));
        return parent;
    }

    @Override
    public List<Permission> getPermissionTree() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("parent_id", "sort", "id");
        return buildPermissionTree(permissionMapper.selectList(queryWrapper));
    }
    /**
     * 校验权限标识是否唯一
     * @param code 权限标识
     * @param excludeId 需要排除的权限ID（编辑时排除自身）
     */
    private void ensureCodeAvailable(String code, Integer excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        Long count = permissionMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("权限标识已存在，请更换其他标识");
        }
    }

    /**
     * 把DTO的字段填充到Entity里
     * @param permission 权限实体
     * @param permissionDTO 权限DTO
     */
    private void fillPermission(Permission permission, PermissionDTO permissionDTO) {
        permission.setName(permissionDTO.getName());
        permission.setMenuType(permissionDTO.getMenuType());
        permission.setCode(permissionDTO.getCode());
        permission.setPath(permissionDTO.getPath());
        permission.setParentId(permissionDTO.getParentId() == null ? 0 : permissionDTO.getParentId());
        permission.setSort(permissionDTO.getSort() == null ? 0 : permissionDTO.getSort());
    }

    /**
     * 判断目标ID是否是当前权限的后代（防止父级选自己的子权限）
     * @param targetId 目标权限ID
     * @param currentId 当前权限ID
     * @return true=是后代，false=不是
     */
    private boolean isDescendant(Integer targetId, Integer currentId) {
        // 递归查询，判断targetId是否在currentId的子级链条里
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", currentId);
        List<Permission> children = permissionMapper.selectList(queryWrapper);

        for (Permission child : children) {
            if (child.getId().equals(targetId)) {
                return true;
            }
            // 递归查子级的子级
            if (isDescendant(targetId, child.getId())) {
                return true;
            }
        }
        return false;
    }
}