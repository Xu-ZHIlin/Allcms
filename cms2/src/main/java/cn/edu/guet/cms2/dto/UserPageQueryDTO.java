package cn.edu.guet.cms2.dto;

import lombok.Data;

@Data
public class UserPageQueryDTO {
    private Integer currentPage = 1;
    private Integer pageSize = 20;
    private String username;
    private Integer status;
    private String phone;
}
