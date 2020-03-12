package com.xcz.controller;

//import com.xcz.controller.interceptor.MiniInterceptor;
import com.xcz.controller.interceptor.MiniInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
		.addResourceLocations("classpath:/META-INF/resources/")
				.addResourceLocations("file:C:/Users/Frankie/Pictures/FrankieTV/");
		//.addResourceLocations("file:/usr/local/frankietv");
	}

	/*@Bean(initMethod="init")
	public ZKCuratorClient zkCuratorClient() {
		return new ZKCuratorClient();
	}*/

	//将拦截器注入spring
	@Bean
	public MiniInterceptor miniInterceptor() {
		return new MiniInterceptor();
	}
	//注册拦截器
	//添加拦截器的拦截的路径
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(miniInterceptor()).addPathPatterns("/user/**")
				       .addPathPatterns("/video/upload", "/video/uploadCover",
				    		   			"/video/userLike", "/video/userUnLike",
				    		   			"/video/saveComment")
												  .addPathPatterns("/bgm/**")
												  .excludePathPatterns("/user/queryPublisher");  //设置不拦截的路径

		super.addInterceptors(registry);
	}
}
