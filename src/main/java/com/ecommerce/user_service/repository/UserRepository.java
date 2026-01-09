package com.ecommerce.user_service.repository;

import com.ecommerce.user_service.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
}
