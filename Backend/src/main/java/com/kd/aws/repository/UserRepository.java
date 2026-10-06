package com.kd.aws.repository;

import com.kd.aws.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.role
            WHERE u.email = :email
            """)
    Optional<User> findByEmail(String email);

    // very useful method for further use
    Optional<User> findByDepartmentIdAndRoleIdAndActiveTrue(
            Long departmentId,
            Long roleId
    );
}
