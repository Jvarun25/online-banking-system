package com.bankapp.banking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs ONCE for every incoming HTTP request (OncePerRequestFilter guarantees
 * this even if the request gets forwarded/included internally).
 *
 * Flow:
 *  1. Read the "Authorization: Bearer <token>" header.
 *  2. If present, validate the token's signature and expiry via JwtService.
 *  3. If valid, load the UserDetails and manually populate the
 *     SecurityContext - this is what tells the rest of Spring Security
 *     "this request is authenticated as this user with these roles".
 *  4. If anything is missing/invalid, just continue the filter chain with NO
 *     authentication set. The request will then be rejected later by
 *     Spring Security's authorization rules (403) if it hits a protected
 *     endpoint - we deliberately do NOT throw here, since public endpoints
 *     like /api/auth/login must still be reachable without a token.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // strip "Bearer "
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Malformed / expired / tampered token - let it fall through unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // Only set authentication if not already set for this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // credentials not needed post-authentication
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
