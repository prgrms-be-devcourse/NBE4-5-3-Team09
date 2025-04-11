package com.coing.domain.notification.entity

import com.coing.domain.user.entity.User
import com.coing.util.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "push_token")
data class PushToken(
    @Id
    @GeneratedValue
    @Column(name = "push_token_id")
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "token", nullable = false, unique = true)
    val token: String
) : BaseEntity()
