package com.tutor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/users/register", "/api/users/login").permitAll()
            
            // 资质证书API安全规则
            .antMatchers(HttpMethod.GET, "/api/qualifications").authenticated()
            .antMatchers(HttpMethod.POST, "/api/qualifications").authenticated()
            .antMatchers(HttpMethod.DELETE, "/api/qualifications/**").authenticated()
            .antMatchers("/api/admin/qualifications/**").hasAnyRole("ADMIN")
            
            // 教师API安全规则
            .antMatchers(HttpMethod.GET, "/api/teachers").permitAll()
            .antMatchers(HttpMethod.GET, "/api/teachers/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/teachers").hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/api/teachers/**").hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/api/teachers/**").hasAnyRole("ADMIN")
            
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/teacher/**").hasRole("TEACHER")
            .antMatchers("/api/user/profile/**").authenticated()
            .antMatchers("/api/user/qualifications/**").authenticated()
            .anyRequest().authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
} 