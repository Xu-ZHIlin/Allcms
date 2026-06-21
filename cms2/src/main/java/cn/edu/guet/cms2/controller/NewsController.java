package cn.edu.guet.cms2.controller;

import cn.edu.guet.cms2.dto.NewsCreateDTO;
import cn.edu.guet.cms2.service.NewsService;
import cn.edu.guet.cms2.util.Result;
import cn.edu.guet.cms2.vo.NewsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @PostMapping
    public Result<NewsVO> createNews(@RequestBody NewsCreateDTO newsCreateDTO) {
        return Result.success("新闻已提交审核", newsService.createNews(newsCreateDTO));
    }
}