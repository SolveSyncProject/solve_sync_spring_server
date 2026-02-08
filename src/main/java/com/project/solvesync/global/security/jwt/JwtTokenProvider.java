package com.project.solvesync.global.security.jwt;

import com.project.solvesync.global.security.auth.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_USERNAME = "uname";
    public static final String CLAIM_EMAIL = "email";

    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;

        if (props.secret() == null || props.secret().isBlank()) {
            throw new IllegalArgumentException("solvesync.jwt.secret is required");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(props.secret());
        } catch (Exception e) {
            keyBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(AuthUser user) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(user.userId(), "userId");

        Date now = new Date();
        Date exp = new Date(now.getTime() + props.accessTokenExpiresMinutes() * 60_000L);

        // ✅ Map.of는 null value 금지 → HashMap + null은 아예 넣지 않기
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.userId());
        if (user.username() != null) claims.put(CLAIM_USERNAME, user.username());
        if (user.email() != null) claims.put(CLAIM_EMAIL, user.email());

        return Jwts.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiration(exp)
                .subject(String.valueOf(user.userId()))
                .claims(claims)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token);
    }

    public AuthUser getAuthUser(String token) {
        Claims claims = parseAndValidate(token).getPayload();

        Long userId = null;
        Object uidObj = claims.get(CLAIM_USER_ID);
        if (uidObj instanceof Number n) {
            userId = n.longValue();
        } else if (uidObj instanceof String s && !s.isBlank()) {
            userId = Long.parseLong(s);
        } else if (claims.getSubject() != null && !claims.getSubject().isBlank()) {
            userId = Long.parseLong(claims.getSubject());
        }

        String username = claims.get(CLAIM_USERNAME, String.class); // 없으면 null
        String email = claims.get(CLAIM_EMAIL, String.class);       // 없으면 null

        if (userId == null) throw new JwtException("uid is missing");

        return new AuthUser(userId, username, email);
    }
}
