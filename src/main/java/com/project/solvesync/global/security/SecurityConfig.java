package com.project.solvesync.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.solvesync.global.security.jwt.JwtAuthenticationFilter;
import com.project.solvesync.global.security.jwt.JwtProperties;
import com.project.solvesync.global.security.jwt.JwtTokenProvider;
import com.project.solvesync.global.security.jwt.RestAccessDeniedHandler;
import com.project.solvesync.global.security.jwt.RestAuthenticationEntryPoint;
import com.project.solvesync.global.security.oauth2.CustomOAuth2UserService;
import com.project.solvesync.global.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.project.solvesync.global.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.project.solvesync.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.project.solvesync.global.security.oauth2.OAuth2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, OAuth2Properties.class})
public class SecurityConfig {

    /**
     * OAuth2 로그인 과정에서 state/redirectUri 등을 HttpSession 대신 "쿠키"로 유지하기 위한 Repository
     */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2FailureHandler,
            HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, objectMapper);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)

                // ✅ API는 JWT 기반으로 (OAuth2 핸드셰이크도 쿠키 저장소로 처리)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper))
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger/**"
                        ).permitAll()

                        // OAuth2 endpoints
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // 내부 시스템(collector 등) 콜백 - MVP에서는 열어둠
                        .requestMatchers("/internal/**").permitAll()

                        // 공개 룸 목록/상세는 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/api/rooms/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/*").permitAll()

                        // 그 외 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )

                // OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(authRequestRepository)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // JWT Filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
