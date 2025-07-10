package com.palep.workoutrecommender;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    /**
     * Mencari seorang coach berdasarkan username.
     * Menggunakan Optional untuk menangani kasus jika username tidak ditemukan.
     * 
     * @param username username yang dicari
     * @return Optional berisi entitas Coach jika ditemukan
     */
    Optional<Coach> findByUsername(String username);
}