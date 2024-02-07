package com.datascout.datascout

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtUtil (
        @Value("\${app.jwt-secret}")
        private val jwtSecret: String
) {

    fun generateToken(userId: String): String {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key())
                .compact()
    }

    private fun key(): Key {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    //validate and return the user id from the token if the token is valid
    fun validateAndExtractUserId(token: String?): Long? {
        return if (isTokenExpired(token)) {
            null
        } else {
            extractId(token)?.toLong()
        }
    }
    private fun extractId(token: String?): String? {
        return extractClaim(token, Claims::getSubject)
    }

    private fun extractExpiration(token: String?): Date? {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun <T> extractClaim(token: String?, claimsResolver: (Claims) -> T): T? {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }
    private fun extractAllClaims(token: String?): Claims {
        try {
            return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).body
        } catch (e: Exception) {
            return Jwts.claims()
        }
    }

    private fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token)?.before(Date()) ?: true
    }




}
