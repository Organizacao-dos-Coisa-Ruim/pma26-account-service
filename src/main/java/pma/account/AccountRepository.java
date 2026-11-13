package pma.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountModel, String> {

  Optional<AccountModel> findByEmail(String email);

  boolean existsByEmail(String email);

}
