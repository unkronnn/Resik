package com.resik.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Long id;
    private String nama;
    private String email;
    private String role;
    private String alamat;
    private String noTelp;
    private BigDecimal saldoDompet;
    private Integer poinReward;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }
    public BigDecimal getSaldoDompet() { return saldoDompet; }
    public void setSaldoDompet(BigDecimal saldoDompet) { this.saldoDompet = saldoDompet; }
    public Integer getPoinReward() { return poinReward; }
    public void setPoinReward(Integer poinReward) { this.poinReward = poinReward; }
}
