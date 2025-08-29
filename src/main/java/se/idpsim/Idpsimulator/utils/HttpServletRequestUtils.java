package se.idpsim.Idpsimulator.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@UtilityClass
public class HttpServletRequestUtils {

    public String getHostUrl() {
        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .build()
            .toUriString();
    }
}
