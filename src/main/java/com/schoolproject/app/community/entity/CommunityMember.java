package com.schoolproject.app.community.entity;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.lecturer.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "community_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_community_user", columnNames = {"community_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CommunityMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private boolean muted;

    @Column(nullable = false)
    private boolean deleted;
}
