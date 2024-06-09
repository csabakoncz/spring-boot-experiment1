package com.ck.b1;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    static class AuthenticationFailureHandler401 extends SimpleUrlAuthenticationFailureHandler {
        public AuthenticationFailureHandler401(String failureUrl) {
            super(failureUrl);
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception) throws IOException, ServletException {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
                return;
            } else {
                super.onAuthenticationFailure(request, response, exception);
            }
        }
    }

    private static class JSONAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

        protected String obtainPassword(Map<String, String> obj) {
            return obj.get(getPasswordParameter());
        }

        protected String obtainUsername(Map<String, String> obj) {
            return obj.get(getUsernameParameter());
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
                throws AuthenticationException {
            if (!"application/json".equals(request.getContentType())) {
                // be aware that objtainPassword and Username in
                // UsernamePasswordAuthenticationFilter
                // have a different method signature
                return super.attemptAuthentication(request, response);
            }

            try (BufferedReader reader = request.getReader()) {

                var om = new ObjectMapper();
                var obj = om.readValue(reader, Map.class);
                String username = obtainUsername(obj);
                String password = obtainPassword(obj);

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username,
                        password);

                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException ex) {
                throw new AuthenticationServiceException("Parsing Request failed", ex);
            }
        }

    }

    public final class FormLoginConfigurer2<H extends HttpSecurityBuilder<H>> extends
            AbstractAuthenticationFilterConfigurer<H, FormLoginConfigurer<H>, UsernamePasswordAuthenticationFilter> {

        public FormLoginConfigurer2() {
            super(new JSONAuthenticationFilter(), null);
            usernameParameter("username");
            passwordParameter("password");
        }

        @Override
        public FormLoginConfigurer<H> loginPage(String loginPage) {
            return super.loginPage(loginPage);
        }

        public FormLoginConfigurer2<H> usernameParameter(String usernameParameter) {
            getAuthenticationFilter().setUsernameParameter(usernameParameter);
            return this;
        }

        public FormLoginConfigurer2<H> passwordParameter(String passwordParameter) {
            getAuthenticationFilter().setPasswordParameter(passwordParameter);
            return this;
        }

        public FormLoginConfigurer2<H> failureForwardUrl(String forwardUrl) {
            failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
            return this;
        }

        public FormLoginConfigurer2<H> successForwardUrl(String forwardUrl) {
            successHandler(new ForwardAuthenticationSuccessHandler(forwardUrl));
            return this;
        }

        @Override
        public void init(H http) throws Exception {
            super.init(http);
            initDefaultLoginFilter(http);
            failureHandler(new AuthenticationFailureHandler401(getFailureUrl()));
        }

        @Override
        protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
            return new AntPathRequestMatcher(loginProcessingUrl, "POST");
        }

        private String getUsernameParameter() {
            return getAuthenticationFilter().getUsernameParameter();
        }

        private String getPasswordParameter() {
            return getAuthenticationFilter().getPasswordParameter();
        }

        private void initDefaultLoginFilter(H http) {
            DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http
                    .getSharedObject(DefaultLoginPageGeneratingFilter.class);
            if (loginPageGeneratingFilter != null && !isCustomLoginPage()) {
                loginPageGeneratingFilter.setFormLoginEnabled(true);
                loginPageGeneratingFilter.setUsernameParameter(getUsernameParameter());
                loginPageGeneratingFilter.setPasswordParameter(getPasswordParameter());
                loginPageGeneratingFilter.setLoginPageUrl(getLoginPage());
                loginPageGeneratingFilter.setFailureUrl(getFailureUrl());
                loginPageGeneratingFilter.setAuthenticationUrl(getLoginProcessingUrl());
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
        } else if (1 == 3) {
            // A Bootstrap 4 based login page. Nice!
            http.formLogin((form) -> {
                form.permitAll();
                form.successHandler(authSuccessHandler);
            });
        } else {
            http.with(new FormLoginConfigurer2<>(), flc2 -> {
                flc2.permitAll();
                flc2.successHandler(authSuccessHandler);
            });
        }

        http.logout((logout) -> logout.permitAll());

        http.httpBasic(Customizer.withDefaults());

        http.with(MyCustomDsl.customDsl(authSuccessHandler), Customizer.withDefaults());

        return http.build();
    }

    private static class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
        private AuthenticationSuccessHandler200 authenticationSuccessHandler;

        public MyCustomDsl(AuthenticationSuccessHandler200 authenticationSuccessHandler) {
            this.authenticationSuccessHandler = authenticationSuccessHandler;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            var requestCache = http.getSharedObject(RequestCache.class);
            authenticationSuccessHandler.setRequestCache(requestCache);

            if (false) {
                var jsonAuthFilter = new JSONAuthenticationFilter();
                AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
                jsonAuthFilter.setAuthenticationManager(authenticationManager);

                http.addFilterBefore(jsonAuthFilter, UsernamePasswordAuthenticationFilter.class);
                jsonAuthFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
            }

        }

        public static MyCustomDsl customDsl(AuthenticationSuccessHandler200 authenticationSuccessHandler) {
            return new MyCustomDsl(authenticationSuccessHandler);
        }
    }
}