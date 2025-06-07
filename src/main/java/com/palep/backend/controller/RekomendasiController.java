package com.palep.backend.controller;

import com.palep.backend.dto.RekomendasiRequest;
import com.palep.backend.dto.RekomendasiResponse;
import com.palep.backend.service.RekomendasiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
// Menambahkan @CrossOrigin untuk mengizinkan request dari semua sumber.
// Untuk produksi, lebih baik diganti dengan URL frontend Anda secara spesifik.
@CrossOrigin(origins = "*") 
public class RekomendasiController {

    private final RekomendasiService rekomendasiService;

    @Autowired
    public RekomendasiController(RekomendasiService rekomendasiService) {
        this.rekomendasiService = rekomendasiService;
    }

    @PostMapping("/rekomendasi")
    public ResponseEntity<RekomendasiResponse> getRekomendasi(@RequestBody RekomendasiRequest request) {
        // Mencetak data yang diterima untuk debugging
        System.out.println("Menerima data dari frontend: " + request.toString());
        
        // Memanggil service untuk memproses data dan mendapatkan hasil
        RekomendasiResponse response = rekomendasiService.getRecommendation(request);
        
        // Mengirimkan hasil kembali ke frontend
        return ResponseEntity.ok(response);
    }
}
