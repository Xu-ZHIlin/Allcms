package cn.edu.guet.cms2.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UserUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;
    private String phone;
    private String email;
    private Integer status;
}
