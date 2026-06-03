package com.schoolproject.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecturer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(nullable = false, length = 120)
    private String faculty;

    @Column(nullable = false, unique = true, length = 80)
    private String staffId;
}
