package cn.codeyang.movie.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties("yang")
public class YangProperties {
    /**
     * emby配置
     */
    private Emby emby = new Emby();
    /**
     * 内网地址匹配
     */
    private List<String> lanIp = new ArrayList<>();

    @Data
    public static class Emby {
        private String baseUrl;
        private String apiKey;
    }
}
