package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.dto.PermissionDTO;
import cn.edu.guet.cms2.entity.Permission;
import cn.edu.guet.cms2.util.PageRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PermissionService {
    IPage<Permission> getPermissionPage(PageRequest pageRequest);
    Permission createPermission(PermissionDTO permissionDTO);
    Permission updatePermission(Integer id,PermissionDTO permissionDTO);
    void deletePermission(Integer id);
    List<Permission> getPermissionTree();
}
