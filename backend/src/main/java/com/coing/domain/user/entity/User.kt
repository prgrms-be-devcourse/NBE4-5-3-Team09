package com.coing.domain.user.entity

import com.coing.util.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "member")
data class User(
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "user_id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "user_name", nullable = false)
    val name: String = "",

    // 이메일
    @Column(name = "email", nullable = false, unique = true)
    val email: String = "",

    // 비밀번호
    @Column(name = "password", nullable = false)
    val password: String = "",

    // 권한 (값이 없을 경우 prePersist()에서 Authority.USER 로 설정)
    @Column(name = "authority", nullable = false)
    var authority: Authority? = null,

    // 이메일 인증 여부 (기본값은 false)
    @Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
    val verified: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: Provider = Provider.EMAIL
) : BaseEntity() {

    @PrePersist
    fun prePersist() {
        if (authority == null) {
            authority = Authority.USER
        }
        // 가입 시각 기록 필드가 필요하다면 추가하세요.
        // if (createdAt == null) {
        //     createdAt = LocalDateTime.now()
        // }
    }

    /**
     * 이메일 인증이 완료되었음을 업데이트합니다.
     * (기존 인스턴스를 복사하여 verified 값을 true로 변경합니다.)
     */
    fun verifyEmail(): User = this.copy(verified = true)
}
