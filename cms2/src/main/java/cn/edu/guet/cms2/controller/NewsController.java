package cn.edu.guet.cms2.controller;

import cn.edu.guet.cms2.dto.NewsCreateDTO;
import cn.edu.guet.cms2.service.NewsService;
import cn.edu.guet.cms2.util.PageRequest;
import cn.edu.guet.cms2.util.Result;
import cn.edu.guet.cms2.vo.NewsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/page")
    public Result<IPage<NewsVO>> getNewsPage(@RequestBody PageRequest pageRequest) {
        return Result.success(newsService.getNewsPage(pageRequest));
    }

    @PutMapping("/{id}")
    public Result<NewsVO> updateNews(@PathVariable Long id, @RequestBody NewsCreateDTO newsCreateDTO) {
        return Result.success("新闻已更新并提交审核", newsService.updateNews(id, newsCreateDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return Result.success("新闻已删除");
    }
}