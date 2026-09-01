package ch.zhaw.www;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Keeps the committed {@code openapi.json} (repo root) in sync with the live API.
 *
 * <p>The frontend generates its DTOs and HTTP services from that file
 * (see {@code frontend/openapi-generator-config.json}), so it is the single source of truth
 * for the API contract. This test regenerates the document from the running application and
 * compares it against the committed copy:
 * <ul>
 *   <li>normal run &mdash; fails if the committed file is stale or missing;</li>
 *   <li>run with {@code -Dopenapi.generate=true} &mdash; (re)writes the committed file and passes.</li>
 * </ul>
 *
 * <p>Regenerate with:
 * <pre>cd backend &amp;&amp; mvn -Dopenapi.generate=true test -Dtest=OpenApiSpecTest</pre>
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebAppConfiguration
class OpenApiSpecTest {

    private static final String API_DOCS_PATH = "/api-docs";
    private static final String SPEC_FILE_NAME = "openapi.json";
    private static final String GENERATE_PROPERTY = "openapi.generate";

    @Autowired
    private MockMvc mvc;

    @Test
    void committedOpenApiSpecIsUpToDate() throws Exception {
        String generated = normalise(fetchSpec());
        Path specFile = repoRoot().resolve(SPEC_FILE_NAME);

        if (Boolean.getBoolean(GENERATE_PROPERTY)) {
            Files.writeString(specFile, generated, StandardCharsets.UTF_8);
            return;
        }

        String committed = Files.exists(specFile)
                ? Files.readString(specFile, StandardCharsets.UTF_8)
                : "";
        assertEquals(generated, committed,
                "The committed " + specFile + " is out of date. Regenerate it with:\n"
                        + "  cd backend && mvn -Dopenapi.generate=true test -Dtest=OpenApiSpecTest\n"
                        + "and commit the result so the frontend picks up the new contract.");
    }

    private String fetchSpec() throws Exception {
        return mvc.perform(get(API_DOCS_PATH).accept("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Re-serialises the document with recursively sorted keys and a trailing newline so the
     * committed file has a stable, diff-friendly representation independent of springdoc's
     * internal ordering.
     */
    private static String normalise(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .configure(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        Object tree = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree) + "\n";
    }

    private static Path repoRoot() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (Files.isDirectory(dir.resolve("backend")) && Files.exists(dir.resolve("pom.xml"))) {
            return dir;
        }
        return dir.getParent();
    }
}
