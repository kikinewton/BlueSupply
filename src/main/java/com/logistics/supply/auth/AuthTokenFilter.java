package com.logistics.supply.auth;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AuthTokenFilter extends GenericFilterBean {

//   private final AppUserDetailsService userDetailsService;

   @Autowired
   TokenProvider tokenProvider;





//  @Override
//  protected void doFilterInternal(
//      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//      throws ServletException, IOException {
//    try {
//      String jwt = parseJwt(request);
//      if (Objects.nonNull(jwt) && jwtService.validateToken(jwt)) {
//        String username = jwtService.getUserNameFromJwtToken(jwt);
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//        UsernamePasswordAuthenticationToken authentication =
//            new UsernamePasswordAuthenticationToken(
//                userDetails, null, userDetails.getAuthorities());
//        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//      }
//    } catch (Exception e) {
//      log.error("Cannot set user authentication: {}", e);
//    }
//
//    filterChain.doFilter(request, response);
//  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
          ServletException {

    try {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      String jwt = this.resolveToken(httpServletRequest);
      if (StringUtils.hasText(jwt)) {
        if (this.tokenProvider.validateToken(jwt)) {
          Authentication authentication = this.tokenProvider.getAuthentication(jwt);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
      filterChain.doFilter(servletRequest, servletResponse);

      this.resetAuthenticationAfterRequest();
    } catch (ExpiredJwtException eje) {
      log.info("Security exception for user {} - {}", eje.getClaims().getSubject(), eje.getMessage());
      ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      log.debug("Exception " + eje.getMessage(), eje);
    }
  }

  private String resolveToken(HttpServletRequest request) {

    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      String jwt = bearerToken.substring(7);
      return jwt;
    }
    return null;
  }

  private void resetAuthenticationAfterRequest() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7, headerAuth.length());
    }

    return null;
  }
}
