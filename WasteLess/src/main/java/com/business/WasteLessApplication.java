package com.business;

import org.springframework.beans.factory.BeanFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.StyleId;
import com.alee.managers.style.StyleManager;
import com.alee.skin.dark.WebDarkSkin;
import com.data.Item;
import com.data.ItemRepository;
import com.data.Notification;
import com.data.NotificationRepository;
import com.presentation.MainWindow;

import java.util.Optional;

import org.slf4j.*;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author imssbora
 */
@SpringBootApplication(scanBasePackages= "com")
@EntityScan("com.data")
@EnableJpaRepositories("com.data")

public class WasteLessApplication implements CommandLineRunner{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    NotificationRepository notRep;
    
    @Autowired
    ItemRepository itemRep;
     
    public static void main(String[] args) {
    	SpringApplicationBuilder builder = new SpringApplicationBuilder(WasteLessApplication.class);
    	builder.headless(false);
    	ConfigurableApplicationContext context = builder.run(args);
    }
 
    public void run(String... args) throws Exception 
    {       
    	
    	WebLookAndFeel.install(WebDarkSkin.class);
        
        
        MainWindow mw = new MainWindow();
        Utils utils = new Utils(mw, itemRep, notRep);
    }
}