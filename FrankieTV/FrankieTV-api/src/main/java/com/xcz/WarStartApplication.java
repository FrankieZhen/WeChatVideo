package com.xcz;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/*
    继承SpringBootServletInitializer，相当于以web.xml的形式启动部署
 */
public class WarStartApplication extends SpringBootServletInitializer {

    //重写配置
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //使用web.xml运行应用程序，指向Application.class 启动springboot
        return builder.sources(Application.class);
    }
}
