package com.dusanbranovic.bookme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

//    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(cors()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/api/health").permitAll()

                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/properties/**").hasAnyAuthority("ADMIN","OWNER","USER")


                        .requestMatchers(HttpMethod.POST,"/api/properties").hasAnyAuthority("ADMIN","OWNER")

                        .requestMatchers(HttpMethod.GET,"/api/property-type").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST,"/api/property-type").hasAnyAuthority("ADMIN","OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/properties/*/add-unit").hasAnyAuthority("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/properties/*/images").hasAnyAuthority("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/properties/*/thumbnail").hasAnyAuthority("ADMIN", "OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/properties/*/images").hasAnyAuthority("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/properties/*/reviews").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/properties/*/reviews").hasAnyAuthority("USER")

                        .requestMatchers(HttpMethod.GET, "/api/units/*").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.GET, "/api/units/search").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.GET, "/api/units/*/addons").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/units/*/addons/*/billing-type").hasAnyAuthority("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/units/**").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/addons/*/add-price").hasAnyAuthority("ADMIN","OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/add-unit-facilities").hasAnyAuthority("ADMIN","OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/images").hasAnyAuthority("ADMIN","OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/add-price").hasAnyAuthority("ADMIN","OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/addons").hasAnyAuthority("ADMIN","OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/units/*/book").hasAnyAuthority("USER")

                        .requestMatchers(HttpMethod.GET, "/api/fascilities").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/fascilities").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/unit-fascilities").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/unit-fascilities").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/unit-fascilities").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/addons").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.POST, "/api/addons").hasAnyAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/users/*/properties").hasAnyAuthority("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/users/*/bookings").hasAnyAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").hasAnyAuthority("USER")

                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*").hasAnyAuthority("USER")

                        .requestMatchers(HttpMethod.POST, "/api/reviews/bookings/*/reviews").hasAnyAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/units/*/reviews").hasAnyAuthority("ADMIN","OWNER","USER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/properties/*/reviews").hasAnyAuthority("ADMIN","OWNER","USER")

                )





                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
//                .authenticationProvider(authenticationProvider)
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource cors() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
