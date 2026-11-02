package com.jnhro1.santadao.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "ID")
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false, comment = "생성 일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, comment = "수정 일시")
    private LocalDateTime updatedAt;
}
