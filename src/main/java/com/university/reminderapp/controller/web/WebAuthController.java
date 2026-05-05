package com.university.reminderapp.controller.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebAuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/auth/jwt-login")
    public String jwtLogin(@RequestParam String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }
}


