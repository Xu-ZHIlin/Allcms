package cn.edu.guet.cms2.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
