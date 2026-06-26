package cn.edu.guet.cms2.controller;

import cn.edu.guet.cms2.annotation.RequiresPermission;
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

    // 注意：需要导入 import cn.edu.guet.cms2.annotation.RequiresPermission;

    /**
     * 审核通过
     */
    @RequiresPermission("content:news:audit")
    @PutMapping("/{id}/approve")
    public Result<NewsVO> approveNews(@PathVariable Long id) {
        return Result.success("新闻审核已通过", newsService.approveNews(id));
    }

    /**
     * 审核驳回
     */
    @RequiresPermission("content:news:audit")
    @PutMapping("/{id}/reject")
    public Result<NewsVO> rejectNews(@PathVariable Long id) {
        return Result.success("新闻已驳回", newsService.rejectNews(id));
    }

    /**
     * 首页公开列表（不校验登录）
     */
    @PostMapping("/public/page")
    public Result<IPage<NewsVO>> getPublicNewsPage(@RequestBody(required = false) PageRequest pageRequest) {
        return Result.success(newsService.getPublicNewsPage(pageRequest));
    }

    /**
     * 首页公开详情（不校验登录）
     */
    @GetMapping("/public/{id}")
    public Result<NewsVO> getPublicNewsDetail(@PathVariable Long id) {
        return Result.success(newsService.getPublicNewsDetail(id));
    }
}