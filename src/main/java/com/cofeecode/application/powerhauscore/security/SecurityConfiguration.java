package com.cofeecode.application.powerhauscore.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true) // To enable @RolesAllowed, @Secured
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // UserDetailsServiceImpl is expected to be a @Service and autowired by Spring elsewhere if needed,
    // or directly used by the AuthenticationManagerBuilder if configuring that way.
    // For this setup, it's primarily used by JwtAuthenticationFilter which is a @Component.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/api/v1/auth/**"),
                    new AntPathRequestMatcher("/uploads/**"),
                    new AntPathRequestMatcher("/images/*.png"),
                    new AntPathRequestMatcher("/line-awesome/**/*.svg"),
                    // OpenAPI & Swagger UI paths
                    new AntPathRequestMatcher("/api/v1/api-docs"), // Path for the api-docs
                    new AntPathRequestMatcher("/api/v1/api-docs/**"), // Path for the api-docs and its subpaths
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    // Vaadin public resources (if keeping Vaadin UI accessible)
                    AntPathRequestMatcher.antMatcher("/VAADIN/**"),
                    AntPathRequestMatcher.antMatcher("/login"), // Vaadin login page
                    AntPathRequestMatcher.antMatcher("/") // Allow access to root for Vaadin
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/**")).authenticated()
                .anyRequest().authenticated()
            )
            // If Vaadin's form login is to be preserved during transition:
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/transactions", true) // Vaadin's default success redirect
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
