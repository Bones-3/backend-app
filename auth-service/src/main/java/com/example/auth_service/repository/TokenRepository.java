package com.example.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.auth_service.model.Token;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long>{
    Optional<Token> findByTokenAndActivoTrue(String token);

    @Modifying
    @Query("update Token t set t.activo = false where t.credencial.idCredencial = :credencialId and t.activo = true")
    int revocarActivosPorCredencial(@Param("credencialId") Long credencialId);
}
