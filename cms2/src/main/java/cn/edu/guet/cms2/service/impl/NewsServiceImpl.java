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

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    private static final String PENDING_REVIEW = "PENDING_REVIEW";

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
        news.setStatus(PENDING_REVIEW);

        newsMapper.insert(news);

        return getNewsDetail(news.getId());
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

    private NewsVO toVO(News news) {
        NewsVO newsVO = new NewsVO();
        BeanUtils.copyProperties(news, newsVO);
        return newsVO;
    }
}