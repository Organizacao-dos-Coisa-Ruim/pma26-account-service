package pma.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Cria o primeiro admin na subida do serviço, a partir de ADMIN_EMAIL e ADMIN_PASSWORD.
 *
 * - Fica no código (e não numa migration) porque a senha precisa passar pelo BCrypt.
 * - Se as variáveis não estiverem definidas, não faz nada.
 * - Se o email já existe, não mexe na conta.
 */
@Slf4j
@Component
public class AdminBootstrap implements ApplicationRunner {

  private final AccountService accountService;
  private final String email;
  private final String password;

  public AdminBootstrap(
      AccountService accountService,
      @Value("${pma.admin.email:}") String email,
      @Value("${pma.admin.password:}") String password) {
    this.accountService = accountService;
    this.email = email;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (email.isBlank() || password.isBlank()) {
      log.info("ADMIN_EMAIL/ADMIN_PASSWORD não definidos: nenhum admin criado");
      return;
    }
    if (accountService.createAdminIfMissing("Admin", email, password)) {
      log.info("Admin inicial criado: {}", email);
    }
  }

}
