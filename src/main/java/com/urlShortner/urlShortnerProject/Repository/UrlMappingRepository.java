package com.urlShortner.urlShortnerProject.Repository;


import com.urlShortner.urlShortnerProject.Model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface UrlMappingRepository  extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<UrlMapping> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);

    @Query("SELECT u FROM UrlMapping u WHERE u.active = true AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
    List<UrlMapping> findAllActive(@Param("now") LocalDateTime now);
}
