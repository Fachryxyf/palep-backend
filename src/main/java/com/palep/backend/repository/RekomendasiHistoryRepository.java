package com.palep.backend.repository;

import com.palep.backend.model.RekomendasiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RekomendasiHistoryRepository extends JpaRepository<RekomendasiHistory, Long> {
    // Spring Data JPA akan secara otomatis menyediakan implementasi untuk
    // metode CRUD dasar (Create, Read, Update, Delete). Kita tidak perlu menulis apa-apa di sini.
}
