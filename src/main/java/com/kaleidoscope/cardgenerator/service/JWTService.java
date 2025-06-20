package com.kaleidoscope.cardgenerator.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private final String secretKey;

    public JWTService() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGenerator.generateKey();
            this.secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        Date dateNow = new Date(System.currentTimeMillis());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(dateNow)
                .expiration(new Date(dateNow.getTime() + jwtExpirationMs))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String requestToken) {
        return extractClaim(requestToken, Claims::getSubject);
    }

    private <T> T extractClaim(String requestToken, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaims(requestToken));
    }

    private Claims extractAllClaims(String requestToken) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(requestToken)
                .getPayload();
    }

    public boolean validateToken(String requestToken, UserDetails userDetails) {
        String username = extractUsername(requestToken);

        return username.equals(userDetails.getUsername()) && !isTokenExpired(requestToken);
    }

    private boolean isTokenExpired(String requestToken) {
        Date tokenExpirationDate = extractClaim(requestToken, Claims::getExpiration);
        return tokenExpirationDate.before(new Date());
    }
}
