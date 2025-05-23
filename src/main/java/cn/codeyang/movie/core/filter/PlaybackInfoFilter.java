package cn.codeyang.movie.core.filter;

import cn.codeyang.movie.core.config.YangProperties;
import cn.codeyang.movie.core.constant.Constants;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * 拦截emby的获取项目实时播放媒体信息接口
 */
@Slf4j
@Order
@Component
@RequiredArgsConstructor
public class PlaybackInfoFilter implements WebFilter {
    private final YangProperties yangProperties;

    AntPathMatcher pathMatcher = new AntPathMatcher();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        Boolean isInner = exchange.getAttribute(Constants.IS_INNER);

        // 如果是内网或者不是目标地址就放行
        if (!pathMatcher.match(Constants.PLAY_BACK_INFO_PATH_PATTERN, uri.getPath())) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> operationExchange(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        DataBufferFactory bufferFactory = response.bufferFactory();
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (Objects.equals(getStatusCode(), HttpStatus.OK) && body instanceof Flux) {
                    // 获取响应ContentType
                    String responseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                    if (StrUtil.isNotEmpty(responseContentType) && StrUtil.containsAny(responseContentType, MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE)) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                            DataBuffer join = dataBufferFactory.join(dataBuffers);
                            byte[] content = new byte[join.readableByteCount()];
                            join.read(content);
                            DataBufferUtils.release(join);
                            String responseData = new String(content, StandardCharsets.UTF_8);

                            // 修改响应内容
                            responseData = modifyResponseData(request.getURI(), responseData);

                            //重新计算内容长度
                            int newContentLength = responseData.getBytes(StandardCharsets.UTF_8).length;
                            getDelegate().getHeaders().setContentLength(newContentLength);

                            return bufferFactory.wrap(responseData.getBytes());
                        }));
                    }

                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }


}
