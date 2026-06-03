package cn.edu.guet.cms2.controller;

import cn.edu.guet.cms2.dto.LoginDTO;
import cn.edu.guet.cms2.service.LoginService;
import cn.edu.guet.cms2.util.Result;
import cn.edu.guet.cms2.vo.UserLoginVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginCotroller {

    @PostConstruct
    public void init() {

    }
    @Resource(name="test")
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    private  LoginService loginService;


    @PostMapping("/login")
    public Result<Object> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户名：{}，密码： {}", loginDTO.getUsername(), loginDTO.getPassword());
        UserLoginVO loginVO = loginService.login(loginDTO);
        if (loginVO != null) {
            return Result.success("登录成功",loginVO);
        }
        return Result.fail(401, "登录失败");
    }
}
