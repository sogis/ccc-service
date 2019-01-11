package ch.so.agi.cccservice;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProbeServletConfig {
    @Bean
    public ServletRegistrationBean exampleServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(
          new ProbeServlet(), "/probe/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
