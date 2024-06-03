package com.ck.b1;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

//	@Autowired
	public void configureGlobal(DataSource dataSource, PasswordEncoder passwordEncoder,
			AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource)
		// the lines below generated the USERS and AUTHORITIES tables
//		.withDefaultSchema()
//		.withUser(User.withUsername("user").password(passwordEncoder.encode("password"))
//		.roles("USER"))
		;
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

		http.csrf(csrf->csrf.ignoringRequestMatchers("/create-user"));

		http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated());

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
			http.formLogin((form) -> form.permitAll());
		}

		http.logout((logout) -> logout.permitAll());

		http.httpBasic(Customizer.withDefaults());

		return http.build();
	}

//	@Bean
//	public UserDetailsService userDetailsService() {
//		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER")
//				.build();
//
//		return new InMemoryUserDetailsManager(user);
//	}
}