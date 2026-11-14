package pma.account;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Implementa o contrato AccountController: as rotas vêm das anotações da interface.
 * Só repassa para o AccountService e monta a resposta HTTP.
 *
 * Quem fez a chamada vem dos headers id-account / role-account que o gateway coloca.
 * Eles são lidos aqui (e não declarados na interface) para o cliente Feign do Auth
 * não precisar enviá-los.
 */
@RestController
@RequiredArgsConstructor
public class AccountResource implements AccountController {

  private final AccountService accountService;
  /** Proxy do Spring: aponta sempre para a requisição atual. */
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<AccountOut> create(AccountIn in) {
    AccountOut out = accountService.create(in);
    return ResponseEntity.created(URI.create("/accounts/" + out.id())).body(out);
  }

  @Override
  public ResponseEntity<List<AccountOut>> findAll() {
    return ResponseEntity.ok(accountService.findAll(requester()));
  }

  @Override
  public ResponseEntity<AccountOut> findById(String id) {
    return ResponseEntity.ok(accountService.findById(id, requester()));
  }

  @Override
  public ResponseEntity<Void> delete(String id) {
    accountService.delete(id, requester());
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

  private Requester requester() {
    return new Requester(request.getHeader(Requester.HEADER_ID), request.getHeader(Requester.HEADER_ROLE));
  }

}
