package vn.studentmanager;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
class Security {
  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetails(Db db) {
    return username -> {
      var rows = db.list("SELECT * FROM users WHERE username=?", username.toLowerCase(Locale.ROOT));
      if (rows.isEmpty()) throw new UsernameNotFoundException("Không tìm thấy tài khoản");
      var r = rows.get(0);
      return new LoginUser(
          Db.id(r.get("id")),
          (String) r.get("username"),
          (String) r.get("password_hash"),
          (String) r.get("role"),
          Db.bool(r.get("enabled")),
          Db.num(r.get("auth_version")));
    };
  }

  @Bean
  SecurityFilterChain chain(HttpSecurity http, Db db) throws Exception {
    http.authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/health", "/api/auth/csrf", "/api/auth/login", "/error")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .formLogin(
            f ->
                f.loginProcessingUrl("/api/auth/login")
                    .successHandler(
                        (q, s, a) -> {
                          s.setContentType("application/json;charset=UTF-8");
                          s.getWriter().write("{\"ok\":true}");
                        })
                    .failureHandler(
                        (q, s, e) ->
                            json(
                                s,
                                401,
                                "Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản đã bị"
                                    + " khóa.")))
        .logout(
            l ->
                l.logoutUrl("/api/auth/logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler((q, s, a) -> s.setStatus(204)))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((q, s, x) -> json(s, 401, "Vui lòng đăng nhập lại."))
                    .accessDeniedHandler(
                        (q, s, x) ->
                            json(
                                s,
                                403,
                                "Bạn không có quyền hoặc phiên bảo mật đã hết hạn. Hãy tải lại"
                                    + " trang.")))
        .addFilterBefore(
            new OncePerRequestFilter() {
              protected void doFilterInternal(
                  HttpServletRequest q, HttpServletResponse s, FilterChain chain)
                  throws ServletException, IOException {
                var a = SecurityContextHolder.getContext().getAuthentication();
                if (a != null && a.getPrincipal() instanceof LoginUser u) {
                  var rows =
                      db.list(
                          "SELECT enabled,auth_version,must_change FROM users WHERE id=?", u.id());
                  if (rows.isEmpty()
                      || !Db.bool(rows.get(0).get("enabled"))
                      || Db.num(rows.get(0).get("auth_version")) != u.version()) {
                    if (q.getSession(false) != null) q.getSession(false).invalidate();
                    SecurityContextHolder.clearContext();
                    json(s, 401, "Tài khoản hoặc mật khẩu đã thay đổi. Vui lòng đăng nhập lại.");
                    return;
                  }
                  if (Db.bool(rows.get(0).get("must_change"))
                      && q.getRequestURI().startsWith("/api/")
                      && !Set.of(
                              "/api/auth/me",
                              "/api/auth/csrf",
                              "/api/auth/password",
                              "/api/auth/logout")
                          .contains(q.getRequestURI())) {
                    json(s, 403, "Bạn cần đổi mật khẩu trước khi sử dụng ứng dụng.");
                    return;
                  }
                }
                chain.doFilter(q, s);
              }
            },
            AuthorizationFilter.class);
    return http.build();
  }

  static void json(HttpServletResponse r, int code, String msg) throws IOException {
    r.setStatus(code);
    r.setContentType("application/json;charset=UTF-8");
    r.getWriter().write("{\"message\":\"" + msg + "\"}");
  }
}

record LoginUser(
    long id, String username, String password, String role, boolean enabled, int version)
    implements UserDetails, Serializable {
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
