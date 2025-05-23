package cn.codeyang.movie.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties({YangProperties.class})
public class YangAutoConfiguration {
}
