package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.dto.LoginDTO;
import cn.edu.guet.cms2.vo.UserLoginVO;

public interface LoginService {
    UserLoginVO login(LoginDTO loginDTO);
}
