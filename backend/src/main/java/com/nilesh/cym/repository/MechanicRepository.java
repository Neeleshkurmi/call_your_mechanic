package com.nilesh.cym.repository;

import com.nilesh.cym.entity.MechanicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository extends JpaRepository<MechanicEntity, Long> {
    Optional<MechanicEntity> findByUser_Id(Long userId);

    @Query("""
            select m
            from MechanicEntity m
            join fetch m.user u
            where m.available = true
            """)
    List<MechanicEntity> findAvailableMechanics();

    @Query("""
            select m
            from MechanicEntity m
            join fetch m.user u
            where m.id = :mechanicId
            """)
    Optional<MechanicEntity> findWithUserById(Long mechanicId);
}
