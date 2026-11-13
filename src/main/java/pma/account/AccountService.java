package pma.account;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/** Regras de negócio do Account. Erros viram status HTTP via ResponseStatusException. */
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public AccountOut create(AccountIn in) {
    if (in == null || isBlank(in.name()) || isBlank(in.email()) || isBlank(in.password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, email e password são obrigatórios");
    }
    String email = normalize(in.email());
    if (accountRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email já cadastrado");
    }
    AccountModel model = AccountModel.builder()
        .id(UUID.randomUUID().toString())
        .name(in.name().trim())
        .email(email)
        .passwordHash(passwordEncoder.encode(in.password()))
        .build();
    return AccountParser.to(accountRepository.save(model));
  }

  public List<AccountOut> findAll() {
    return accountRepository.findAll().stream()
        .map(AccountParser::to)
        .toList();
  }

  public AccountOut findById(String id) {
    return accountRepository.findById(id)
        .map(AccountParser::to)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "conta não encontrada"));
  }

  @Transactional
  public void delete(String id) {
    if (!accountRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conta não encontrada");
    }
    accountRepository.deleteById(id);
  }

  /**
   * Confere email + senha (usado pelo Auth no login).
   * Responde 401 tanto para email inexistente quanto para senha errada,
   * para não revelar quais emails estão cadastrados.
   */
  public AccountOut login(LoginIn in) {
    if (in == null || isBlank(in.email()) || isBlank(in.password())) {
      throw unauthorized();
    }
    return accountRepository.findByEmail(normalize(in.email()))
        .filter(model -> passwordEncoder.matches(in.password(), model.getPasswordHash()))
        .map(AccountParser::to)
        .orElseThrow(AccountService::unauthorized);
  }

  private static ResponseStatusException unauthorized() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciais inválidas");
  }

  private static String normalize(String email) {
    return email.trim().toLowerCase();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

}
