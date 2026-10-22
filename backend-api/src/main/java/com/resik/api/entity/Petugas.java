package com.resik.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PETUGAS")
public class Petugas extends User {

    @Override
    public String getRole() {
        return "PETUGAS";
    }
}
