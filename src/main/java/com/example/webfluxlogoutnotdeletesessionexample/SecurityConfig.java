package com.example.webfluxlogoutnotdeletesessionexample;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .logout()
                .and()
                .formLogin()
                .and()
                .csrf().disable()
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER,ADMIN")
                .build());
    }

}
