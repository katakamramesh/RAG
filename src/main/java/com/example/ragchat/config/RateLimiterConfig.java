package com.example.ragchat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterConfig implements Filter {

    @Value("${RATE_LIMIT:10}")
    private int rateLimit;
    private final Map<String, UserRequest> requests = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String clientIp = request.getRemoteAddr();
        UserRequest userReq = requests.getOrDefault(clientIp, new UserRequest());
        long now = Instant.now().getEpochSecond();
        if (now - userReq.lastReset > 60) {
            userReq.lastReset = now;
            userReq.count = 1;
        } else {
            userReq.count++;
        }
        requests.put(clientIp, userReq);
        if (userReq.count > rateLimit) {
            ((HttpServletResponse) res).setStatus(429);
            res.getWriter().write("Too Many Requests");
            return;
        }
        chain.doFilter(req, res);
    }
    static class UserRequest {
        long lastReset = Instant.now().getEpochSecond();
        int count = 0;
    }
}