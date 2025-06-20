package com.cofeecode.application.powerhauscore.security;

import com.cofeecode.application.powerhauscore.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll()  // Allow public access to file operations
                        .requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/uploads/**"))  // Ignore CSRF for file deletions
                .formLogin(form -> form.defaultSuccessUrl("/transactions", true));

        super.configure(http);
        setLoginView(http, LoginView.class);
    }

}
