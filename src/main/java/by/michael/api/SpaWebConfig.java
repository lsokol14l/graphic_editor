package by.michael.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // Forward known SPA routes to index.html so browser refresh works.
    registry.addViewController("/image-redactor").setViewName("forward:/index.html");
    registry.addViewController("/histogram").setViewName("forward:/index.html");
  }
}
