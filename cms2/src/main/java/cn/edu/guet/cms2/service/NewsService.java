package cn.edu.guet.cms2.service;

import cn.edu.guet.cms2.dto.NewsCreateDTO;
import cn.edu.guet.cms2.vo.NewsVO;
import org.springframework.transaction.annotation.Transactional;
import cn.edu.guet.cms2.util.PageRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface NewsService {

    IPage<NewsVO> getNewsPage(PageRequest pageRequest);

    NewsVO getNewsDetail(Long id);

    @Transactional(rollbackFor = Exception.class)
    NewsVO createNews(NewsCreateDTO newsCreateDTO);

    NewsVO updateNews(Long id, NewsCreateDTO newsCreateDTO);

    @Transactional(rollbackFor = Exception.class)
    void deleteNews(Long id);

    // ------ 新增审核方法 ------
    NewsVO approveNews(Long id);

    NewsVO rejectNews(Long id);

    // ------ 新增供首页使用的公开方法 ------
    IPage<NewsVO> getPublicNewsPage(PageRequest pageRequest);

    NewsVO getPublicNewsDetail(Long id);
}