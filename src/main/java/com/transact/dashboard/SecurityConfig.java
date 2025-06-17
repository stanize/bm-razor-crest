package com.transact.dashboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TransactAuthenticationProvider transactAuthenticationProvider;

    public SecurityConfig(TransactAuthenticationProvider transactAuthenticationProvider) {
        this.transactAuthenticationProvider = transactAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(transactAuthenticationProvider)
                .authorizeRequests()
                .antMatchers("/transact-dashboard-login", "/css/**").permitAll()
                .antMatchers(HttpMethod.POST, "/jboss/restart").authenticated() // ✅ Allow POST to restart
                .antMatchers("/jboss/**").hasRole("ADMIN")                       // Protect all other /jboss/**
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/transact-dashboard-login")
                .loginProcessingUrl("/transact-dashboard-login")
                .defaultSuccessUrl("/transact-dashboard", true)
                .permitAll()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/transact-dashboard-login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
