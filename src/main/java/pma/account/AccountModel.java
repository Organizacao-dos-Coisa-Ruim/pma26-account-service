package pma.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidade JPA da tabela "account". Não é exposta na API: a conversão para DTO fica no AccountParser. */
@Entity
@Table(name = "account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {

  @Id
  @Column(name = "id_account")
  private String id;

  @Column(name = "tx_name", nullable = false)
  private String name;

  @Column(name = "tx_email", nullable = false, unique = true)
  private String email;

  /** Hash BCrypt da senha (nunca a senha em texto puro). */
  @Column(name = "tx_password_hash", nullable = false)
  private String passwordHash;

  /** "user" ou "admin" (ver AccountOut.ROLE_*). */
  @Column(name = "tx_role", nullable = false)
  private String role;

}
