package com.kd.aws.repository;

import com.kd.aws.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    // very useful method for further use
    Optional<User> findByDepartmentIdAndRoleIdAndActiveTrue(
            Long departmentId,
            Long roleId
    );
}
