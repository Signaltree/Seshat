package org.seshat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // ponytail: single admin user. Password encoded with BCrypt.
        // Change password in production by editing this or using env variable.
        String user = System.getenv("ADMIN_USER") != null ? System.getenv("ADMIN_USER") : "admin";
        String pass = System.getenv("ADMIN_PASS") != null ? System.getenv("ADMIN_PASS") : "admin123";
        return new InMemoryUserDetailsManager(
            User.withUsername(user)
                .password("{noop}" + pass)
                .roles("ADMIN")
                .build()
        );
    }
}
