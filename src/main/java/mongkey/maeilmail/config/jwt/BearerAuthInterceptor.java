package mongkey.maeilmail.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BearerAuthInterceptor implements HandlerInterceptor {
    private AuthorizationExtractor authExtractor;
    private JwtTokenProvider jwtTokenProvider;

    public BearerAuthInterceptor(AuthorizationExtractor authExtractor, JwtTokenProvider jwtTokenProvider) {
        this.authExtractor = authExtractor;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        System.out.println(">>> interceptor.preHandle 호출");

        /*Header의 Authorization에서 Bearer 토큰 추출*/
        String token = authExtractor.extract(request, "Bearer");
        System.out.println("token = " + token);
        if (StringUtils.isEmpty(token)) {
            System.out.println("test1");
            throw new IllegalArgumentException("토큰을 발급받지 않았습니다");
//            return false;
        }

        /*토큰 유효성 검증*/
        if (!jwtTokenProvider.validateToken(token)) {
            System.out.println("test2");
            throw new IllegalArgumentException("유효하지 않은 토큰");
        }

        /*클라이언트 정보 추가*/
        String name = jwtTokenProvider.getSubject(token);
        System.out.println("name = " + name);
        request.setAttribute("name", name);
        return true;
    }
}
