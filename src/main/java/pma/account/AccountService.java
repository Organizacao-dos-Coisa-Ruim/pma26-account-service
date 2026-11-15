package pma.account;

import java.nio.charset.StandardCharsets;
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

  /** Tamanho das colunas tx_name e tx_email (VARCHAR(256) na migration). */
  static final int MAX_LENGTH = 256;
  /** O BCrypt só considera os primeiros 72 bytes da senha e recusa senhas maiores. */
  static final int MAX_PASSWORD_BYTES = 72;

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  /** Toda conta criada pela API é "user". Admin só nasce pelo AdminBootstrap. */
  @Transactional
  public AccountOut create(AccountIn in) {
    return create(in, AccountOut.ROLE_USER);
  }

  /** Cria o admin inicial se o email ainda não existe. Devolve true se criou. */
  @Transactional
  public boolean createAdminIfMissing(String name, String email, String password) {
    if (accountRepository.existsByEmail(normalize(email))) {
      return false;
    }
    create(new AccountIn(name, email, password), AccountOut.ROLE_ADMIN);
    return true;
  }

  private AccountOut create(AccountIn in, String role) {
    if (in == null || isBlank(in.name()) || isBlank(in.email()) || isBlank(in.password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, email e password são obrigatórios");
    }
    // Achado do scan OWASP ZAP (#78): sem estes limites, valores grandes quebravam no banco
    // (VARCHAR(256)) ou no BCrypt (máximo de 72 bytes) e viravam erro 500 em vez de 400.
    if (in.name().trim().length() > MAX_LENGTH || normalize(in.email()).length() > MAX_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name e email têm no máximo " + MAX_LENGTH + " caracteres");
    }
    if (in.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password tem no máximo " + MAX_PASSWORD_BYTES + " bytes");
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
        .role(role)
        .build();
    return AccountParser.to(accountRepository.save(model));
  }

  /** Só admin (o gateway já bloqueia; aqui é a segunda barreira). */
  public List<AccountOut> findAll(Requester requester) {
    requireAdmin(requester);
    return accountRepository.findAll().stream()
        .map(AccountParser::to)
        .toList();
  }

  /** A própria conta ou admin. */
  public AccountOut findById(String id, Requester requester) {
    if (!requester.isInternal() && !requester.isAdmin() && !requester.id().equals(id)) {
      throw forbidden();
    }
    return accountRepository.findById(id)
        .map(AccountParser::to)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "conta não encontrada"));
  }

  /** Só admin. */
  @Transactional
  public void delete(String id, Requester requester) {
    requireAdmin(requester);
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

  /** Chamadas internas (sem headers do gateway) passam; pelo gateway, só admin. */
  private static void requireAdmin(Requester requester) {
    if (!requester.isInternal() && !requester.isAdmin()) {
      throw forbidden();
    }
  }

  private static ResponseStatusException forbidden() {
    return new ResponseStatusException(HttpStatus.FORBIDDEN, "acesso negado");
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
