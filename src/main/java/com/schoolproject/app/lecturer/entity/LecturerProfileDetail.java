package com.schoolproject.app.lecturer.entity;

import com.schoolproject.app.entity.LecturerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "profile_details")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LecturerProfileDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_profile_id", nullable = false, unique = true)
    private LecturerProfile lecturerProfile;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_visible", nullable = false)
    private boolean profileVisible;
}
