package pma.account;

/** Conversão entre a entidade (AccountModel) e o DTO de saída (AccountOut). A senha nunca sai. */
public class AccountParser {

  private AccountParser() {
  }

  public static AccountOut to(AccountModel model) {
    return AccountOut.builder()
        .id(model.getId())
        .name(model.getName())
        .email(model.getEmail())
        .role(model.getRole())
        .build();
  }

}
