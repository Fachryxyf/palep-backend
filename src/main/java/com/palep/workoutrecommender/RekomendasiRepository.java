// File: src/main/java/com/palep/workoutrecommender/RekomendasiRepository.java
package com.palep.workoutrecommender;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RekomendasiRepository extends JpaRepository<Rekomendasi, Long> {
}