package com.ecommerce.user_service.repository;

import com.ecommerce.user_service.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserEntity findByUserId(String userId);

    @Query("select u from UserEntity u where u.userId = :userId")
    UserEntity findForTestByUserId(@Param("userId") String userId);

    UserEntity findByEmail(String email);
    Page<UserEntity> findAll(Pageable pageable);
}
