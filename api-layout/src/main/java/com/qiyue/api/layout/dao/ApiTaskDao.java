package com.qiyue.api.layout.dao;

import com.qiyue.api.layout.entity.ApiTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiTaskDao extends JpaRepository<ApiTaskEntity, Long> {

    Optional<ApiTaskEntity> findByTaskId(String taskId);
}
