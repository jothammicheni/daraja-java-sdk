package com.github.jothammicheni.daraja_springboot_starter_jdk;

import com.github.jothammicheni.daraja_springboot_starter_jdk.core.config.MpesaAutoConfiguration;
import com.github.jothammicheni.daraja_springboot_starter_jdk.core.config.MpesaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootApplication

//@Import(MpesaAutoConfiguration.class) // Manually imports your clean starter configuration entrypoint
public class DarajaSpringbootStarterJdkApplication {

//	public static void main(String[] args) {
//		SpringApplication.run(DarajaSpringbootStarterJdkApplication.class, args);
//	}
}
