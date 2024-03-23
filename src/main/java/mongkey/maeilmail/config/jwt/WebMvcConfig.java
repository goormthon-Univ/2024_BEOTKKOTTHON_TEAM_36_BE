package mongkey.maeilmail.config.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    private final BearerAuthInterceptor bearerAuthInterceptor;

    //Bearer 토큰을 검사하고 인증하는 인스턴스 주입
    public WebMvcConfig(BearerAuthInterceptor bearerAuthInterceptor) {
        this.bearerAuthInterceptor = bearerAuthInterceptor;
    }

    public void addInterceptors(InterceptorRegistry registry){
        System.out.println(">>> 인터셉터 호출");

        //어느 엔드포인트까지 인터셉터를 추가할지 생각해야함
        registry.addInterceptor(bearerAuthInterceptor).addPathPatterns("/api/post");
        registry.addInterceptor(bearerAuthInterceptor).addPathPatterns("/api/post/*");
        registry.addInterceptor(bearerAuthInterceptor).addPathPatterns("/api/template");
        registry.addInterceptor(bearerAuthInterceptor).addPathPatterns("/api/template/*");


    }
}
