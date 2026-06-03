package com.schoolproject.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "university_student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityStudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 80)
    private String matricNumber;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(nullable = false, length = 30)
    private String level;

    @Column(nullable = false, length = 120)
    private String faculty;
}
