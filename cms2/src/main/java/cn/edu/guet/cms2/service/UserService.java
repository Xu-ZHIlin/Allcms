package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.util.PageResult;
import cn.edu.guet.cms2.dto.UserCreateDTO;
import cn.edu.guet.cms2.dto.UserPageQueryDTO;
import cn.edu.guet.cms2.dto.UserUpdateDTO;
import cn.edu.guet.cms2.vo.UserVO;

public interface UserService {
    /**
     * 分页查询用户列表
     */
    PageResult<UserVO> pageUsers(UserPageQueryDTO dto);

    /**
     * 新增用户
     * @return 创建后的用户视图
     */
    UserVO createUser(UserCreateDTO dto);

    /**
     * 更新用户资料
     * @return 更新后的用户视图
     */
    UserVO updateUser(UserUpdateDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
}
