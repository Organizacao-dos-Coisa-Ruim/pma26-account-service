package pma.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** Testes unitários do AccountService: repositório mockado, BCrypt real. */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private AccountService accountService;

  @BeforeEach
  void setUp() {
    accountService = new AccountService(accountRepository, passwordEncoder);
  }

  @Test
  void createSalvaComSenhaHasheadaEEmailNormalizado() {
    when(accountRepository.existsByEmail("ana@insper.edu.br")).thenReturn(false);
    when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AccountOut out = accountService.create(new AccountIn("Ana", " Ana@Insper.edu.br ", "123456"));

    ArgumentCaptor<AccountModel> saved = ArgumentCaptor.forClass(AccountModel.class);
    verify(accountRepository).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo("ana@insper.edu.br");
    assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("123456");
    assertThat(passwordEncoder.matches("123456", saved.getValue().getPasswordHash())).isTrue();
    assertThat(out.id()).isNotBlank();
    assertThat(out.email()).isEqualTo("ana@insper.edu.br");
  }

  @Test
  void createComEmailRepetidoDa409() {
    when(accountRepository.existsByEmail("ana@insper.edu.br")).thenReturn(true);

    assertThatThrownBy(() -> accountService.create(new AccountIn("Ana", "ana@insper.edu.br", "123456")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    verify(accountRepository, never()).save(any());
  }

  @Test
  void createSemSenhaDa400() {
    assertThatThrownBy(() -> accountService.create(new AccountIn("Ana", "ana@insper.edu.br", " ")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  @Test
  void findByIdInexistenteDa404() {
    when(accountRepository.findById("x")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.findById("x"))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void deleteInexistenteDa404() {
    when(accountRepository.existsById("x")).thenReturn(false);

    assertThatThrownBy(() -> accountService.delete("x"))
        .isInstanceOf(ResponseStatusException.class);
    verify(accountRepository, never()).deleteById(any());
  }

  @Test
  void loginComSenhaCorretaDevolveConta() {
    when(accountRepository.findByEmail("ana@insper.edu.br")).thenReturn(Optional.of(model("123456")));

    AccountOut out = accountService.login(new LoginIn("ANA@insper.edu.br", "123456"));

    assertThat(out.id()).isEqualTo("1");
  }

  @Test
  void loginComSenhaErradaDa401() {
    when(accountRepository.findByEmail("ana@insper.edu.br")).thenReturn(Optional.of(model("123456")));

    assertThatThrownBy(() -> accountService.login(new LoginIn("ana@insper.edu.br", "errada")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  @Test
  void loginComEmailInexistenteDa401() {
    when(accountRepository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.login(new LoginIn("nao@existe.com", "123456")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  private AccountModel model(String password) {
    return AccountModel.builder()
        .id("1")
        .name("Ana")
        .email("ana@insper.edu.br")
        .passwordHash(passwordEncoder.encode(password))
        .build();
  }

}
