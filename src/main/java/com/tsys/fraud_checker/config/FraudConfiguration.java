package com.tsys.fraud_checker.config;

import com.tsys.fraud_checker.services.*;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Configuration
@PropertySource("classpath:application-${spring.profiles.active:development}.properties")
public class FraudConfiguration {
    private static final Logger LOG = Logger.getLogger(FraudConfiguration.class.getName());

    @Autowired
    private Environment env;

    @Bean
    public Random random() {
        return new Random(4864325435L);
    }

    @Bean("verificationService")
    public VerificationService verificationService(DefaultVerificationService defaultVerificationService,
                                                   @Nullable VerificationServiceRouter router,
                                                   @Nullable VerificationServiceRoutingInterceptor verificationServiceRoutingInterceptor) throws ClassNotFoundException {
        if (isDevelopmentProfile()) {
            LOG.info("Development Profile");
            final ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
            proxyFactoryBean.addAdvice(verificationServiceRoutingInterceptor);
            proxyFactoryBean.setTarget(router);
            return (VerificationService) proxyFactoryBean.getObject();
        }
        return defaultVerificationService;
    }

    private boolean isDevelopmentProfile() {
        return Stream.of(env.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("development"));
    }
}
