package se.idpsim.Idpsimulator.service.session;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
public class UserSessionService {

    public void createSession() {
        log.info("Creating user session");
        getCurrentRequest().getSession(true);
    }

    public void invalidateSession() {
        log.info("Invalidating user session");
        HttpServletRequest request = getCurrentRequest();
        Optional.ofNullable(request.getSession(false))
            .ifPresent(session -> {
                session.invalidate();
                log.info("User session invalidated");
            });
    }

    public void addAttribute(String key, Object value) {
        log.debug("Adding attribute to user session: {}={}", key, value);
        getCurrentRequest().getSession(true)
            .setAttribute(key, value);
    }

    public Optional<Object> getAttribute(String key) {
        log.debug("Getting attribute from user session: {}", key);
        return Optional.ofNullable(getCurrentRequest().getSession(false))
            .map(session -> session.getAttribute(key));
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        throw new IllegalStateException("No current HTTP request found");
    }
}
