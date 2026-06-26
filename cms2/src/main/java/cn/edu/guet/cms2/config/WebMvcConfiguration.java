package cn.edu.guet.cms2.config;

import cn.edu.guet.cms2.intercepter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有以 /api 开头的请求
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/news/public/**"   // 必须放行
                        // 如果有附件接口，也要放行 "/api/news/attachments/**"
                ); // 排除登录接口，否则没登录永远进不去登录页
    }
}
