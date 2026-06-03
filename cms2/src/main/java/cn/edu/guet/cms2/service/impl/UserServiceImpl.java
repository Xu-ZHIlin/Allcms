package cn.edu.guet.cms2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.edu.guet.cms2.exception.BusinessException;
import cn.edu.guet.cms2.util.PageResult;
import cn.edu.guet.cms2.dto.UserCreateDTO;
import cn.edu.guet.cms2.dto.UserPageQueryDTO;
import cn.edu.guet.cms2.dto.UserUpdateDTO;
import cn.edu.guet.cms2.entity.User;
import cn.edu.guet.cms2.mapper.UserMapper;
import cn.edu.guet.cms2.service.UserService;
import cn.edu.guet.cms2.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    // ==================== 分页查询 ====================
    @Override
    public PageResult<UserVO> pageUsers(UserPageQueryDTO dto) {
        // 构造分页对象
        Page<User> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());

        // 构造查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername())
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .like(StrUtil.isNotBlank(dto.getPhone()), User::getPhone, dto.getPhone())
                .orderByDesc(User::getCreateTime);   // 按创建时间倒序

        // 执行分页
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<UserVO> records = userPage.getRecords().stream()
                .map(this::entityToVo)
                .collect(Collectors.toList());

        return new PageResult<>(records, userPage.getTotal());
    }

    // ==================== 新增用户 ====================
    @Override
    @Transactional
    public UserVO createUser(UserCreateDTO dto) {
        // 1. 校验用户名唯一
        checkUsernameUnique(dto.getUsername(), null);

        // 2. 构建实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());  // 明文存储，暂不加密
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        // 3. 插入数据库
        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException("新增用户失败");
        }

        log.info("新增用户成功，id: {}, username: {}", user.getId(), user.getUsername());
        return entityToVo(user);
    }

    // ==================== 更新用户 ====================
    @Override
    @Transactional
    public UserVO updateUser(UserUpdateDTO dto) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新允许修改的字段（不修改用户名和密码）
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException("更新用户失败");
        }

        log.info("更新用户成功，id: {}", user.getId());
        return entityToVo(user);
    }

    // ==================== 删除用户 ====================
    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在，无法删除");
        }

        // 2. 简单删除（物理删除）
        int rows = userMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除用户失败");
        }

        log.info("删除用户成功，id: {}", id);
    }

    // ==================== 重置密码 ====================
    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 明文设置新密码
        user.setPassword(newPassword);
        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException("重置密码失败");
        }

        log.info("重置密码成功，userId: {}", id);
    }

    // ==================== 辅助方法 ====================
    /**
     * 检查用户名是否唯一
     * @param username 新用户名
     * @param excludeId 需要排除的用户ID（更新时排除自身）
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    /**
     * Entity 转 VO（务必排除 password）
     */
    private UserVO entityToVo(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setAvatar(user.getAvatar());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}