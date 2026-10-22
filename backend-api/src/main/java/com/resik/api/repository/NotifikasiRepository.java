package com.resik.api.repository;

import com.resik.api.entity.Notifikasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    List<Notifikasi> findByUserIdOrderByCreatedAtDesc(Long userId);
}
