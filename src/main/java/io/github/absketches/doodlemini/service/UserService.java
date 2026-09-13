package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.UserAccount;
import io.github.absketches.doodlemini.exception.ConflictException;
import io.github.absketches.doodlemini.exception.NotFoundException;
import io.github.absketches.doodlemini.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository users;

    public UserService(UserAccountRepository users) {
        this.users = users;
    }

    @Transactional
    public UserAccount create(String name, String email) {
        if (users.existsByEmail(email)) {
            throw new ConflictException("A user with this email already exists.");
        }
        return users.save(new UserAccount(name, email));
    }

    @Transactional(readOnly = true)
    public UserAccount get(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found."));
    }
}
