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

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    private static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";

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
}