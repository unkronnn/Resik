package com.resik.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MASYARAKAT")
public class Masyarakat extends User {

    @Override
    public String getRole() {
        return "MASYARAKAT";
    }
}
