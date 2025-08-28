package se.idpsim.Idpsimulator.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@UtilityClass
public class ObjectUtils {


    public <T> void requireNonNull(T object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    public <T> void requireNonEmpty(T object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(msg);
        }
        if (object instanceof String str && !StringUtils.hasText(str)) {
            throw new IllegalArgumentException(msg);
        }
        if (object instanceof List<?> list && CollectionUtils.isEmpty(list)) {
            throw new IllegalArgumentException(msg);
        }
    }
}
