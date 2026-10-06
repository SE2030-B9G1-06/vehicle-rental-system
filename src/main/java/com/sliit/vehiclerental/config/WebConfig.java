package com.sliit.vehiclerental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final AccessInterceptor access;

  public WebConfig(AccessInterceptor a) {
    access = a;
  }

  @Override
  public void addInterceptors(InterceptorRegistry r) {
    r.addInterceptor(access).addPathPatterns("/api/**", "/pages/**");
  }
}
