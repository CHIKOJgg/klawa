package org.example.aiassistantklawa.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.aiassistantklawa.user.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET = "0ca64563e7825247168414b71b95ce211774ea56b1adbfbffe89a597ed407e73";
    public String extractUsername(String token) {
        return extractClaimsJWT(token, Claims::getSubject );
    }
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    public boolean isTokenValid(String token,UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token));

    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date() );
    }

    private Date extractExpiration(String token) {
        return extractClaimsJWT(token, Claims::getExpiration);
    }

    private Claims extractClaimsJWT(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public <T> T extractClaimsJWT(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaimsJWT(token);
        return claimsResolver.apply(claims);
    }

    private Key getSigningKey() {
        byte [] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
