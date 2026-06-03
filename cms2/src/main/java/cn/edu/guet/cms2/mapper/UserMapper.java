package cn.edu.guet.cms2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.guet.cms2.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
