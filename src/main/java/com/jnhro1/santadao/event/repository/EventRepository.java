package com.jnhro1.santadao.event.repository;

import com.jnhro1.santadao.event.entity.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select distinct e from Event e left join fetch e.participants where e.publicCode = :publicCode")
    Optional<Event> findWithParticipantsByPublicCode(@Param("publicCode") String publicCode);

    @Query("select distinct e from Event e left join fetch e.participants where e.adminToken = :adminToken")
    Optional<Event> findWithParticipantsByAdminToken(@Param("adminToken") String adminToken);

    boolean existsByPublicCode(String publicCode);

    boolean existsByAdminToken(String adminToken);
}
