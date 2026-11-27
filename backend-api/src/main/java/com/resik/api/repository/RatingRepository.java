package com.resik.api.repository;

import com.resik.api.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByPenjemputanId(Long penjemputanId);
    Optional<Rating> findByPenjemputanId(Long penjemputanId);
    List<Rating> findByPetugasId(Long petugasId);

    @Query("SELECT COALESCE(AVG(r.nilai), 0) FROM Rating r WHERE r.petugas.id = :petugasId")
    Double averageByPetugasId(Long petugasId);

    long countByPetugasId(Long petugasId);
}
