package backend.academy.scrapper.service.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonSerializationService {
    private final ObjectMapper mapper;

    public String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public <T> T fromJson(String json, Class<T> cls) throws IOException {
        return mapper.readValue(json, cls);
    }
}
