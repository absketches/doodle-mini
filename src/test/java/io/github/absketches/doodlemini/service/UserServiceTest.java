package io.github.absketches.doodlemini.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.absketches.doodlemini.entity.UserAccount;
import io.github.absketches.doodlemini.exception.ConflictException;
import io.github.absketches.doodlemini.exception.NotFoundException;
import io.github.absketches.doodlemini.repository.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final UserService service = new UserService(users);

    @Test
    void createSavesUserWithCalendar() {
        when(users.existsByEmail("abhi@example.test")).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount user = service.create("Abhi Basu", "abhi@example.test");

        assertThat(user.getName()).isEqualTo("Abhi Basu");
        assertThat(user.getEmail()).isEqualTo("abhi@example.test");
        assertThat(user.getCalendar()).isNotNull();
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(users.existsByEmail("abhi@example.test")).thenReturn(true);

        assertThatThrownBy(() -> service.create("Abhi Basu", "abhi@example.test"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("A user with this email already exists.");

        verify(users, never()).save(any());
    }

    @Test
    void getRejectsMissingUser() {
        when(users.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User was not found.");
    }
}
