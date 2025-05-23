package cn.codeyang.movie.core.filter;

import cn.codeyang.movie.core.config.YangProperties;
import cn.codeyang.movie.core.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * 该过滤器主要用来添加是否是内网请求的标识
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class NetworkEnvFilter implements WebFilter {
    private final YangProperties yangProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String clientIp = Objects.requireNonNull(remoteAddress).getAddress().getHostAddress();
        for (String lanIp : yangProperties.getLanIp()) {
            if (clientIp.startsWith(lanIp)) {
                exchange.getAttributes().put(Constants.IS_INNER, true);
                log.info("当前请求地址属于内网 ip: {}, url: {}", clientIp, request.getURI());
            }
        }

        return chain.filter(exchange);
    }
}
