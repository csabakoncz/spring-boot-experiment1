package com.ck.b1;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    static class AuthenticationSuccessHandler200 extends SavedRequestAwareAuthenticationSuccessHandler {
        @Override
        protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setStatus(200);
                response.flushBuffer();
            } else {
                super.handle(request, response, authentication);
            }
        }
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (1 != 1) {
            // we do not secure any pages:
            http.authorizeHttpRequests(reqs -> reqs.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(
                (requests) -> requests.requestMatchers("/", "/home", "/h2-console/**", "/actuator/**").permitAll());

        http.authorizeHttpRequests(
                auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll())
                .headers(headers -> headers.frameOptions().disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/create-user"));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/login"));

        http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated());

        var authSuccessHandler = new AuthenticationSuccessHandler200();

        /**
         * Spring Security configures the use of basic auth by default. POST requests to
         * login and logout are handled by the security filter (there are no endpoints)
         */
        if (1 == 2) {
            // this is to use our custom login page. If loginPage is not specified
            // a default one is used
            http.formLogin((form) -> form.loginPage("/login").permitAll());
        } else {
            // A Bootstrap 4 based login page. Nice!
            http.formLogin((form) -> {
                form.permitAll();
                form.successHandler(authSuccessHandler);
            });
        }

        http.logout((logout) -> logout.permitAll());

        http.httpBasic(Customizer.withDefaults());

        http.apply(MyCustomDsl.customDsl(authSuccessHandler));
        return http.build();
    }

    private static class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
        private AuthenticationSuccessHandler200 authenticationSuccessHandler;

        public MyCustomDsl(AuthenticationSuccessHandler200 authenticationSuccessHandler) {
            this.authenticationSuccessHandler = authenticationSuccessHandler;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            var requestCache = http.getSharedObject(RequestCache.class);
            authenticationSuccessHandler.setRequestCache(requestCache);
//            http.addFilter(new CustomFilter(authenticationManager));

        }

        public static MyCustomDsl customDsl(AuthenticationSuccessHandler200 authenticationSuccessHandler) {
            return new MyCustomDsl(authenticationSuccessHandler);
        }
    }
}