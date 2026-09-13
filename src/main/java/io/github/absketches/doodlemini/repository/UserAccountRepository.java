package io.github.absketches.doodlemini.repository;

import io.github.absketches.doodlemini.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "calendar")
    Optional<UserAccount> findWithCalendarById(Long id);

    @EntityGraph(attributePaths = "calendar")
    List<UserAccount> findByIdInOrderByIdAsc(Collection<Long> ids);
}
