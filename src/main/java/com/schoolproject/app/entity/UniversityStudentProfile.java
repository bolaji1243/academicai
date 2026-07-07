package com.schoolproject.app.entity;

import com.schoolproject.app.enums.Level;
import com.schoolproject.app.enums.Semester;
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

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 80)
    private String matricNumber;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(nullable = false, length = 120)
    private String faculty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Semester semester;

    @Column(nullable = false, length = 20)
    private String session;
}
