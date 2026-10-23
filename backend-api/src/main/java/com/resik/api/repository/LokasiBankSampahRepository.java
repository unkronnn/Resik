package com.resik.api.repository;

import com.resik.api.entity.LokasiBankSampah;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LokasiBankSampahRepository extends JpaRepository<LokasiBankSampah, Long> {
    List<LokasiBankSampah> findByIsActiveTrue();
}
