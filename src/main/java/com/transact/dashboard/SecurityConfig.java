package com.transact.dashboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login", "/css/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("transact-dashboard-login")
                .loginProcessingUrl("/transact-dashboard-login")
                .defaultSuccessUrl("/transact-dashboard", true)
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")          // optional, default is /logout
                .logoutSuccessUrl("/transact-dashboard-login?logout") // redirect to login page after logout
                .invalidateHttpSession(true)   // invalidate session
                .deleteCookies("JSESSIONID")   // clear session cookie
                .permitAll();

        return http.build();
    }


    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("pass")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
