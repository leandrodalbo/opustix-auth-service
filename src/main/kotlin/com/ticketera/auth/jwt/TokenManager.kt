package com.ticketera.auth.jwt

import com.ticketera.auth.model.User
import com.ticketera.auth.props.JwtProps
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import java.util.Base64

@Component
class TokenManager(private val jwtProps: JwtProps) {

    fun generateToken(user: User) =
        Jwts.builder()
            .setSubject(user.tokenString())
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtProps.expiration))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)), SignatureAlgorithm.HS512)
            .compact()

    fun getUserInfo(token: String) = kotlin.runCatching {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .subject

    }.getOrElse { "invalid-token" }

    fun isAValidToken(token: String) = kotlin.runCatching {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .subject.isNotEmpty()
    }.getOrElse { false }


}
