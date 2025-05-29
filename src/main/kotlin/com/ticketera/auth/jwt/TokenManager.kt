package com.ticketera.auth.jwt

import com.ticketera.auth.errors.Message
import com.ticketera.auth.model.User
import com.ticketera.auth.props.JwtProps
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.Base64


@Component
class TokenManager(private val jwtProps: JwtProps) {

    fun generateToken(user: User) =
        Jwts.builder()
            .setSubject(user.encoded())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date(System.currentTimeMillis() + jwtProps.expiration))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)), SignatureAlgorithm.HS512)
            .compact()

    fun getUserInfo(token: String): String = kotlin.runCatching {
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .subject

    }.getOrElse { Message.INVALID_TOKEN.text }

    fun isAValidToken(token: String) = kotlin.runCatching {
        val expiration = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProps.secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .expiration.toInstant()

        return expiration.minusMillis(Instant.now().toEpochMilli()).toEpochMilli() > 0


    }.getOrElse { false }

    fun getUserEmailFromToken(token: String) = kotlin.runCatching {
        User.decode(getUserInfo(token)).email
    }.getOrElse { Message.INVALID_TOKEN.text }

    fun getEncodedUserEmail(userData: String) = kotlin.runCatching {
        User.decode(userData).email
    }.getOrElse { Message.INVALID_TOKEN.text }
}
