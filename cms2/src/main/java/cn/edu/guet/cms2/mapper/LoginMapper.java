package cn.edu.guet.cms2.mapper;

import cn.edu.guet.cms2.dto.LoginDTO;
import cn.edu.guet.cms2.vo.UserLoginVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    UserLoginVO login(LoginDTO loginDTO);
}
