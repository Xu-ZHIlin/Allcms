package cn.edu.guet.cms2.vo;

import cn.edu.guet.cms2.entity.Permission;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
//Set是不可重复的
@Data
public class UserLoginVO {
    private Integer id;
    private String username;
    // 返回给前端，token要存储起来，每次访问后端某个接口的时候，要携带上token
    private String token;
    private LocalDateTime tokenExpireAt;
    // 原始权限列表（包含按钮，用于权限校验）
    private List<Permission> permissionList;
    // 树形菜单列表（仅包含目录和菜单，用于前端侧边栏展示）
    private List<Permission> menuTree;

    private Set<String> permissions;

    public boolean hasPermission(String permission) {
        if (permissions == null && permissionList != null) {
            // 懒加载：第一次用到时，把 List 转为 Set 提高后续查询效率
            permissions = permissionList.stream()
                    //传统方法
//            for (int i = 0; i < permissions.size(); i++) {
//                String code = permissionList.get(i).getCode();
//                if(code!=null){
//                    permissions.add(code);
//                }
//            }
                     //lamba
                    .map(Permission::getCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return permissions != null && permissions.contains(permission);
    }
}
