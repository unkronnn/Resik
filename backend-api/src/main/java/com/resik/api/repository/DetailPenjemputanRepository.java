package com.resik.api.repository;

import com.resik.api.entity.DetailPenjemputan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailPenjemputanRepository extends JpaRepository<DetailPenjemputan, Long> {
}
