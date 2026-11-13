package pma.account;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/** Sobe a aplicação inteira (H2 + Flyway) e testa os endpoints via HTTP simulado. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountResourceIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AccountRepository accountRepository;

  @BeforeEach
  void cleanDatabase() {
    accountRepository.deleteAll();
  }

  @Test
  void healthCheck() throws Exception {
    mockMvc.perform(get("/accounts/health-check"))
        .andExpect(status().isOk());
  }

  @Test
  void criaBuscaListaEApaga() throws Exception {
    String id = createAccount("Ana", "ana@insper.edu.br", "123456");

    mockMvc.perform(get("/accounts/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Ana"))
        .andExpect(jsonPath("$.email").value("ana@insper.edu.br"))
        .andExpect(jsonPath("$.password").doesNotExist());

    mockMvc.perform(get("/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));

    mockMvc.perform(delete("/accounts/" + id))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/accounts/" + id))
        .andExpect(status().isNotFound());
  }

  @Test
  void senhaNaoFicaEmTextoPuroNoBanco() throws Exception {
    String id = createAccount("Ana", "ana@insper.edu.br", "123456");

    String hash = accountRepository.findById(id).orElseThrow().getPasswordHash();
    org.assertj.core.api.Assertions.assertThat(hash).isNotEqualTo("123456").startsWith("$2");
  }

  @Test
  void emailRepetidoDa409() throws Exception {
    createAccount("Ana", "ana@insper.edu.br", "123456");

    mockMvc.perform(post("/accounts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json("Outra", "ana@insper.edu.br", "abc")))
        .andExpect(status().isConflict());
  }

  @Test
  void camposFaltandoDa400() throws Exception {
    mockMvc.perform(post("/accounts")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"Ana\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginComCredenciaisCorretas() throws Exception {
    String id = createAccount("Ana", "ana@insper.edu.br", "123456");

    mockMvc.perform(post("/accounts/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"ana@insper.edu.br\",\"password\":\"123456\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(content().string(not(containsString("123456"))));
  }

  @Test
  void loginComSenhaErradaDa401() throws Exception {
    createAccount("Ana", "ana@insper.edu.br", "123456");

    mockMvc.perform(post("/accounts/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"ana@insper.edu.br\",\"password\":\"errada\"}"))
        .andExpect(status().isUnauthorized());
  }

  private String createAccount(String name, String email, String password) throws Exception {
    MvcResult result = mockMvc.perform(post("/accounts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(name, email, password)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
  }

  private static String json(String name, String email, String password) {
    return "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(name, email, password);
  }

}
