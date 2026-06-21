package cn.edu.guet.cms2.dto;

import lombok.Data;

@Data
public class NewsCreateDTO {
    private String title;
    private String category;
    private String supplier;
    private String reviewer;
    private String content;
}