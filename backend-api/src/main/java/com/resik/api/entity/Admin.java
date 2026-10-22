package com.resik.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
