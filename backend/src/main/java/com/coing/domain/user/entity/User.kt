package com.coing.domain.user.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.coing.util.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class User extends BaseEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(name = "user_id", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "user_name", nullable = false)
	private String name;

	// 이메일
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	// 비밀번호
	@Column(name = "password", nullable = false)
	private String password;

	// 권한
	@Column(name = "authority", nullable = false)
	private Authority authority;

	// 회원 가입 시각 기록
	// @Column(name = "created_at", nullable = false, updatable = false)
	// private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (authority == null) {
			authority = Authority.USER;
		}
		/*if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}*/
	}

	// 이메일 인증 여부
	@Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
	private boolean verified;

	/**
	 * 이메일 인증이 완료되었음을 업데이트합니다.
	 */
	public User verifyEmail() {
		return this.withVerified(true);
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false)
	private Provider provider;
}
