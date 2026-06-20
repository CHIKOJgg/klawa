package org.example.aiassistantklawa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
  private final JwtAuthenticationFilter jwtAuthnFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
      http
              .csrf(AbstractHttpConfigurer::disable)
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers("/api/v1/auth/**").permitAll()
                      .requestMatchers("/actuator/**").permitAll()//
                      .anyRequest().authenticated()                     // everything else requires JWT
              )
              .sessionManagement(session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
              )
              .authenticationProvider(authenticationProvider)
              .addFilterBefore(jwtAuthnFilter, UsernamePasswordAuthenticationFilter.class);
      return http.build();
  }
}
