package com.palep.workoutrecommender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cek jika belum ada data coach sama sekali
        if (coachRepository.count() == 0) {
            Coach adminCoach = new Coach();
            adminCoach.setUsername("pelatih");
            // Enkripsi password sebelum disimpan!
            adminCoach.setPassword(passwordEncoder.encode("password123"));

            coachRepository.save(adminCoach);
            System.out.println(">>> Default coach user 'pelatih' created with password 'password123'");
        }
    }
}