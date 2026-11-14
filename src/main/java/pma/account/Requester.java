package pma.account;

/**
 * Quem fez a requisição, segundo os headers que o gateway coloca depois de validar o JWT
 * (id-account e role-account).
 *
 * Sem esses headers, a chamada é interna (ex.: o Auth chamando via Feign pela rede do cluster),
 * porque o gateway sempre remove e reescreve esses headers nas rotas protegidas.
 */
public record Requester(String id, String role) {

  public static final String HEADER_ID = "id-account";
  public static final String HEADER_ROLE = "role-account";

  public boolean isInternal() {
    return id == null || id.isBlank();
  }

  public boolean isAdmin() {
    return AccountOut.ROLE_ADMIN.equalsIgnoreCase(role);
  }

}
