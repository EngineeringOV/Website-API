package ventures.of.api.testing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestUtil {

    private TestUtil() {
        // NOOP
    }

    public static <T> T readResource(String resource, Class<T> type) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (Reader reader = new InputStreamReader(TestUtil.class.getClassLoader().getResourceAsStream(resource), UTF_8)) {
            return objectMapper.readValue(reader, type);
        }
    }

}
