package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.dto.RoleAuthorizeDTO;
import cn.edu.guet.cms2.dto.RoleDTO;
import cn.edu.guet.cms2.entity.Permission;
import cn.edu.guet.cms2.entity.Role;
import cn.edu.guet.cms2.util.PageRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface RoleService {
    IPage<Role> getRoleInfoPage(PageRequest pageRequest);

    Role createRole(RoleDTO roleDTO);

    Role updateRole(Integer id, RoleDTO roleDTO);

    void deleteRole(Integer id);

    List<Permission> getPermissionTree();

    List<Integer> getRolePermissionIds(Integer roleId);

    void authorizeRole(RoleAuthorizeDTO roleAuthorizeDTO);
}
