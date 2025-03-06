package com.coing.domain.user.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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

	@PrePersist
	public void prePersist() {
		if (authority == null) {
			authority = Authority.USER;
		}
	}

	// 이메일 인증 코드
	@Column(name = "verification_code")
	private String verificationCode;

	// 이메일 인증 확인
	@Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
	private boolean verified;

	// 비밀번호 재설정 코드
	@Column(name = "reset_password_code")
	private String resetPasswordCode;
}
