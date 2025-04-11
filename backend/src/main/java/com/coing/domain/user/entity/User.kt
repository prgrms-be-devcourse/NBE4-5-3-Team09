package com.coing.domain.user.entity

import com.coing.domain.chat.entity.ChatMessage
import com.coing.global.annotation.NoArg
import com.coing.util.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "member")
@NoArg
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

    // 권한 (값이 없을 경우 prePersist()에서 설정)
    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false)
    var authority: Authority? = null,

    // 이메일 인증 여부 (기본값은 false)
    @Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
    val verified: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: Provider = Provider.EMAIL,

    // 회원이 작성한 채팅 메시지들
    @OneToMany(
        mappedBy = "sender",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val chatMessages: MutableList<ChatMessage> = mutableListOf()
) : BaseEntity() {

    @PrePersist
    fun prePersist() {
        if (authority == null) {
            authority = Authority.ROLE_USER
        }
    }

    fun verifyEmail(): User = this.copy(verified = true)
}
