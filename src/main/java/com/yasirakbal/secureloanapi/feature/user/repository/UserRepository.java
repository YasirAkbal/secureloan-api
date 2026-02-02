package com.yasirakbal.secureloanapi.feature.user.repository;

import com.yasirakbal.secureloanapi.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String username);
    boolean existsUserByIdentityNumber(String identityNumber);
}
