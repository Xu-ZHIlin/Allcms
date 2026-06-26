package cn.edu.guet.cms2.service.impl;

import cn.edu.guet.cms2.dto.NewsCreateDTO;
import cn.edu.guet.cms2.entity.News;
import cn.edu.guet.cms2.mapper.NewsMapper;
import cn.edu.guet.cms2.service.NewsService;
import cn.edu.guet.cms2.vo.NewsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.edu.guet.cms2.util.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {
    // --- 补全状态常量 ---
    private static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_REJECTED = "REJECTED";
    @Autowired
    private NewsMapper newsMapper;

    @Override
    public NewsVO getNewsDetail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new IllegalArgumentException("新闻不存在");
        }
        return toVO(news);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public NewsVO createNews(NewsCreateDTO newsCreateDTO) {
        validateNews(newsCreateDTO);

        News news = new News();
        news.setTitle(newsCreateDTO.getTitle());
        news.setCategory(newsCreateDTO.getCategory());
        news.setSupplier(newsCreateDTO.getSupplier());
        news.setReviewer(newsCreateDTO.getReviewer());
        news.setContent(newsCreateDTO.getContent());
        // 修复点：补全常量前缀 STATUS_
        news.setStatus(STATUS_PENDING_REVIEW);

        newsMapper.insert(news);

        return getNewsDetail(news.getId());
    }

    @Override
    public IPage<NewsVO> getNewsPage(PageRequest pageRequest) {
        Page<News> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();

        String keyword = pageRequest.getParamValue("keyword");
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("supplier", keyword)
                    .or()
                    .like("reviewer", keyword));
        }

        String category = pageRequest.getParamValue("category");
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }

        String status = pageRequest.getParamValue("status");
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("update_time", "id");

        return newsMapper.selectPage(page, queryWrapper).convert(this::toVO);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public NewsVO updateNews(Long id, NewsCreateDTO newsCreateDTO) {
        if (id == null) {
            throw new IllegalArgumentException("新闻ID不能为空");
        }

        validateNews(newsCreateDTO);

        News existingNews = newsMapper.selectById(id);
        if (existingNews == null) {
            throw new IllegalArgumentException("新闻不存在");
        }

        existingNews.setTitle(newsCreateDTO.getTitle());
        existingNews.setCategory(newsCreateDTO.getCategory());
        existingNews.setSupplier(newsCreateDTO.getSupplier());
        existingNews.setReviewer(newsCreateDTO.getReviewer());
        existingNews.setContent(newsCreateDTO.getContent());
        existingNews.setStatus(STATUS_PENDING_REVIEW);
        existingNews.setPublishTime(null);
        newsMapper.updateById(existingNews);

        return toVO(newsMapper.selectById(id));
    }

    private void validateNews(NewsCreateDTO newsCreateDTO) {
        if (newsCreateDTO == null || !StringUtils.hasText(newsCreateDTO.getTitle())) {
            throw new IllegalArgumentException("新闻标题不能为空");
        }
        if (!StringUtils.hasText(newsCreateDTO.getCategory())) {
            throw new IllegalArgumentException("栏目不能为空");
        }
        if (!StringUtils.hasText(newsCreateDTO.getContent())) {
            throw new IllegalArgumentException("正文内容不能为空");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteNews(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new IllegalArgumentException("新闻不存在");
        }
        newsMapper.deleteById(id);
    }
    private NewsVO toVO(News news) {
        NewsVO newsVO = new NewsVO();
        BeanUtils.copyProperties(news, newsVO);
        return newsVO;
    }

    // --- 新增：审核通过 ---
    @Transactional(rollbackFor = Exception.class)
    @Override
    public NewsVO approveNews(Long id) {
        News news = getReviewableNews(id);
        news.setStatus(STATUS_PUBLISHED);
        news.setPublishTime(LocalDateTime.now());
        newsMapper.updateById(news);
        return getNewsDetail(id);
    }

    // --- 新增：审核驳回 ---
    @Transactional(rollbackFor = Exception.class)
    @Override
    public NewsVO rejectNews(Long id) {
        News news = getReviewableNews(id);
        news.setStatus(STATUS_REJECTED);
        news.setPublishTime(null);
        newsMapper.updateById(news);
        return getNewsDetail(id);
    }

    // --- 新增：公开分页查询（只查已发布） ---
    @Override
    public IPage<NewsVO> getPublicNewsPage(PageRequest pageRequest) {
        PageRequest safePageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        Page<News> page = new Page<>(safePageRequest.getCurrentPage(), safePageRequest.getPageSize());
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();

        // 核心条件：只能查已发布的新闻
        queryWrapper.eq("status", STATUS_PUBLISHED);

        String keyword = safePageRequest.getParamValue("keyword");
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like("title", keyword).or().like("supplier", keyword).or().like("reviewer", keyword));
        }
        String category = safePageRequest.getParamValue("category");
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }
        // 按发布时间倒序
        queryWrapper.orderByDesc("publish_time", "update_time", "id");

        return newsMapper.selectPage(page, queryWrapper).convert(this::toVO);
    }

    // --- 新增：公开详情（校验是否已发布） ---
    @Override
    public NewsVO getPublicNewsDetail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null || !STATUS_PUBLISHED.equals(news.getStatus())) {
            throw new IllegalArgumentException("新闻不存在或尚未发布");
        }
        return toVO(news);
    }

    // --- 新增：复用校验逻辑 ---
    private News getReviewableNews(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new IllegalArgumentException("新闻不存在");
        }
        if (!STATUS_PENDING_REVIEW.equals(news.getStatus())) {
            throw new IllegalArgumentException("只有待审核的新闻可以审核");
        }
        return news;
    }
}