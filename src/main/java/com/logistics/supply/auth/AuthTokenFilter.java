package com.logistics.supply.auth;


import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

  @Autowired AppUserDetailsService userDetailsService;
  @Autowired JwtServiceImpl jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    try {

      String jwt = parseJwt(httpServletRequest);
      if (StringUtils.hasText(jwt) && this.jwtService.validateToken(jwt)) {
        String username = null;
        try {
          username = jwtService.getUserNameFromJwtToken(jwt);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      filterChain.doFilter(httpServletRequest, httpServletResponse);

      this.resetAuthenticationAfterRequest();
    } catch (ExpiredJwtException eje) {
      log.info(
          "Security exception for user {} - {}", eje.getClaims().getSubject(), eje.getMessage());
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      log.debug("Exception " + eje.getMessage(), eje);
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);

    }
    return null;
  }

  private void resetAuthenticationAfterRequest() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
