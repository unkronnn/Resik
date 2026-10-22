package com.resik.api.repository;

import com.resik.api.entity.Sampah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SampahRepository extends JpaRepository<Sampah, Long> {
    List<Sampah> findByIsActiveTrue();
    List<Sampah> findByKategori(String kategori);
}
