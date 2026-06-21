package cn.edu.guet.cms2.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String category;
    private String supplier;
    private String reviewer;
    private String content;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishTime;
}