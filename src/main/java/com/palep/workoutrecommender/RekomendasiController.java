package com.palep.workoutrecommender;

import com.palep.workoutrecommender.dto.RekomendasiUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RekomendasiController {

    @Autowired
    private RekomendasiService rekomendasiService;

    // Endpoint #1: Membuat Rekomendasi
    @PostMapping("/rekomendasi")
    public ResponseEntity<Rekomendasi> createRekomendasi(@RequestBody RekomendasiRequest request) {
        Rekomendasi hasil = rekomendasiService.createRekomendasi(request);
        return ResponseEntity.ok(hasil);
    }

    // Endpoint #2: Mengambil Semua Riwayat
    @GetMapping("/rekomendasi")
    public ResponseEntity<List<Rekomendasi>> getHistory() {
        List<Rekomendasi> history = rekomendasiService.getAllRekomendasi();
        return ResponseEntity.ok(history);
    }

    // Endpoint #3: Merevisi Rekomendasi (Hanya untuk Pelatih)
    @PutMapping("/rekomendasi/{id}")
    public ResponseEntity<Rekomendasi> reviseRekomendasi(
            @PathVariable Long id,
            @RequestBody RekomendasiUpdateRequest request) {
        Rekomendasi updatedRekomendasi = rekomendasiService.updateRekomendasi(id, request);
        return ResponseEntity.ok(updatedRekomendasi);
    }
}