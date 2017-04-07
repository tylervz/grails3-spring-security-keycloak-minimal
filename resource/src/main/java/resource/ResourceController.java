package resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

@RestController
@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
public class ResourceController {

    @Autowired
    private ResourceLoader resourceLoader;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping("/")
    public Principal getPrincipal(Principal principal) {
        return principal;
    }

    @RequestMapping("/data")
    public JsonNode getData() throws IOException {
        return load("data.json", JsonNode.class);
    }
    
    private <T> T load(String filename, Class<T> type) throws IOException {
        InputStream json = resourceLoader.getResource("classpath:" + filename).getInputStream();
        return objectMapper.readValue(json, type);
    }

}
