package fairteam_backend.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {
    private static final String FIREBASE_JWK_SET_URI = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http,
            @Value("${fairteam.security.auth-required:false}") boolean authRequired,
            @Value("${fairteam.security.firebase-project-id:}") String projectId,
            @Value("${fairteam.security.allowed-teachers:}") String allowedTeachers,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!authRequired) {
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID is required when AUTH_REQUIRED=true");
        }

        Set<String> teacherEmails = Arrays.stream(allowedTeachers.split(","))
                .map(String::trim).filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/api/**").hasRole("TEACHER")
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(teacherConverter(teacherEmails))));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "fairteam.security.auth-required", havingValue = "true")
    JwtDecoder firebaseJwtDecoder(@Value("${fairteam.security.firebase-project-id:}") String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID is required when AUTH_REQUIRED=true");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(FIREBASE_JWK_SET_URI).build();
        decoder.setJwtValidator(token -> {
            var standard = JwtValidators.createDefaultWithIssuer("https://securetoken.google.com/" + projectId).validate(token);
            if (standard.hasErrors()) return standard;
            Object audienceClaim = token.getClaims().get("aud");
            boolean matches = audienceClaim instanceof String audience && projectId.equals(audience)
                    || audienceClaim instanceof Collection<?> audiences && audiences.contains(projectId);
            return matches ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                    : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                            new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "Token audience does not match the Firebase project", null));
        });
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> teacherConverter(Set<String> teacherEmails) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> authoritiesFor(jwt, teacherEmails));
        return converter;
    }

    private Collection<GrantedAuthority> authoritiesFor(Jwt jwt, Set<String> teacherEmails) {
        String email = jwt.getClaimAsString("email");
        Boolean verified = jwt.getClaim("email_verified");
        if (email != null && Boolean.TRUE.equals(verified) && teacherEmails.contains(email.toLowerCase(Locale.ROOT))) {
            return List.of(new SimpleGrantedAuthority("ROLE_TEACHER"));
        }
        return List.of();
    }
}
