package com.example.authproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "refresh_token")
@Data
@NoArgsConstructor
public class RefreshToken {
    private static final int EXPIRATION = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private Date expiryDate;

    public RefreshToken(String token, AppUser user, int expiryTimeInMinutes) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(expiryTimeInMinutes);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
