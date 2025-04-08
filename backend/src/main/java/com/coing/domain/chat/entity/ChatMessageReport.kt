package com.coing.domain.chat.entity

import com.coing.domain.user.entity.User
import com.coing.global.annotation.NoArg
import com.coing.util.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_message_reports",
    uniqueConstraints = [UniqueConstraint(columnNames = ["chat_message_id", "reporter_id"])]
)
@NoArg
data class ChatMessageReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 신고 대상 메시지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    val chatMessage: ChatMessage,

    // 신고한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: User,

    // 신고 일시
    @Column(name = "reported_at", nullable = false)
    val reportedAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity()
