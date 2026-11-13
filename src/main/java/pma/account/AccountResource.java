package pma.account;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Implementa o contrato AccountController: as rotas vêm das anotações da interface.
 * Só repassa para o AccountService e monta a resposta HTTP.
 */
@RestController
@RequiredArgsConstructor
public class AccountResource implements AccountController {

  private final AccountService accountService;

  @Override
  public ResponseEntity<AccountOut> create(AccountIn in) {
    AccountOut out = accountService.create(in);
    return ResponseEntity.created(URI.create("/accounts/" + out.id())).body(out);
  }

  @Override
  public ResponseEntity<List<AccountOut>> findAll() {
    return ResponseEntity.ok(accountService.findAll());
  }

  @Override
  public ResponseEntity<AccountOut> findById(String id) {
    return ResponseEntity.ok(accountService.findById(id));
  }

  @Override
  public ResponseEntity<Void> delete(String id) {
    accountService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<AccountOut> login(LoginIn in) {
    return ResponseEntity.ok(accountService.login(in));
  }

  @Override
  public ResponseEntity<Void> healthCheck() {
    return ResponseEntity.ok().build();
  }

}
