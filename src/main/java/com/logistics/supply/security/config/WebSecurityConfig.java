package com.logistics.supply.security.config;

import com.logistics.supply.auth.AppUserDetailsService;
import com.logistics.supply.auth.AuthEntryPointJwt;
import com.logistics.supply.auth.AuthTokenFilter;
//import com.logistics.supply.auth.AuthenticationSuccessHandlerImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;


import java.util.Arrays;

@AllArgsConstructor
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AppUserDetailsService appUserDetailsService;

  private String[] AUTH_LIST = {
          "/v2/api-docs",
          "**/swagger-resources/**",
          "/swagger-ui.html",
          "/**",
          "/notification/**",
          "/v2/api-docs",
          "/webjars/**"
  };

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

//  @Autowired
//  private AuthenticationSuccessHandlerImpl successHandler;


  @Bean
  public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
    return new SecurityEvaluationContextExtension();
  }


  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
            User.withUsername("admin@mail.com").password("password@!.com").roles("ADMIN").build()
    );
  }

  @Autowired private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Override
  public void configure(AuthenticationManagerBuilder authenticationManagerBuilder)
      throws Exception {
    authenticationManagerBuilder
        .userDetailsService(appUserDetailsService)
        .passwordEncoder(bCryptPasswordEncoder);
  }

  /**
   * For authorization
   *
   * @param http
   * @throws Exception
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .configurationSource(corsConfigurationSource())
        .and()
        .csrf()
        .disable()
        .authorizeRequests()
        .antMatchers("/**")
        .permitAll()
//        .antMatchers("/api/auth/**")
//        .hasRole(EmployeeLevel.REGULAR.name())
        .anyRequest()
        .authenticated();

//    http.addFilterBefore(successHandler(successHandler));
  }

  //  @Bean
  //  public WebMvcConfigurer corsConfigurer() {
  //    return new WebMvcConfigurerAdapter() {
  //      @Override
  //      public void addCorsMappings(CorsRegistry registry) {
  //        registry
  //            .addMapping("/**")
  //            .allowedMethods("GET", "POST", "PUT", "DELETE")
  //            .allowedOrigins("*")
  //            .allowedHeaders("*");
  //      }
  //    };
  //  }

  //  @Bean
  //  CorsConfigurationSource corsConfigurationSource()
  //  {
  //    CorsConfiguration configuration = new CorsConfiguration();
  //    configuration.setAllowedOrigins(Arrays.asList("https://localhost:4200"));
  //    configuration.setAllowedMethods(Arrays.asList("GET","POST"));
  //    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  //    source.registerCorsConfiguration("/**", configuration);
  //    return source;
  //  }

  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedMethods(
        Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.PUT.name(),
            HttpMethod.POST.name(),
            HttpMethod.DELETE.name()));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues());
    return source;
  }

  //  @Bean
  //  public DaoAuthenticationProvider daoAuthenticationProvider() {
  //    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
  //    provider.setPasswordEncoder(bCryptPasswordEncoder);
  //    provider.setUserDetailsService(appUserDetailsService);
  //    return provider;
  //  }
}
