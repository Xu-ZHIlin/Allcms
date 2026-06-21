package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.dto.NewsCreateDTO;
import cn.edu.guet.cms2.vo.NewsVO;
import org.springframework.transaction.annotation.Transactional;

public interface NewsService {

    NewsVO getNewsDetail(Long id);

    @Transactional(rollbackFor = Exception.class)
    NewsVO createNews(NewsCreateDTO newsCreateDTO);
}