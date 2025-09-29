package com.FVProject.filevault;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
/*
@SpringBootTest
@ActiveProfiles("test") // Load application-test.properties
@AutoConfigureMockMvc
public class WizardControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserService userService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.credentials.location", () -> "classpath:test-key.json");
        registry.add("spring.cloud.gcp.storage.bucket", () -> "fake-bucket"); // prevents placeholder errors
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
    }

    @BeforeEach
    void setup() throws Exception {
        // Mock GCP upload to avoid real cloud call
        when(gcpService.upload(any(), any())).thenReturn("https://fake-gcp-url.com/fakefile.json");

        // Mock UserService if needed for DB endpoint
        when(userService.testConnection(any(), any(), any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    void testDbConnectionSuccess() throws Exception {
        String jsonBody = """
            {
                "host": "localhost",
                "port": "3306",
                "dbName": "testdb",
                "username": "sa",
                "password": ""
            }
            """;

        mockMvc.perform(post("/wizard/db-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGcpKeyUpload() throws Exception {
        mockMvc.perform(multipart("/wizard/upload-gcp-key")
                        .file("file", getClass().getResourceAsStream("/test-key.json").readAllBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}




*/