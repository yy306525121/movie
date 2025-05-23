package cn.codeyang.movie.core.routes;

import cn.codeyang.movie.core.config.YangProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * emby的路由配置
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class EmbyRoute {
    private final YangProperties yangProperties;

    /**
     * 定义路由
     * 定义名为emby的路由，处理GET、POST、HEAD请求，请求将被转发到配置的地址
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator embyRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("emby", r ->r.method(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD).uri(yangProperties.getEmby().getBaseUrl()))
                .build();

    }
}
