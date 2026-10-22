package com.resik.api.repository;

import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.Penjemputan.PenjemputanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PenjemputanRepository extends JpaRepository<Penjemputan, Long> {
    List<Penjemputan> findByUserId(Long userId);
    List<Penjemputan> findByStatus(PenjemputanStatus status);
    List<Penjemputan> findByPetugasId(Long petugasId);
}
