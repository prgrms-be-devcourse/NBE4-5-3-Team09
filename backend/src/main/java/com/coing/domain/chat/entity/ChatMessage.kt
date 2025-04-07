package com.coing.domain.chat.entity

import com.coing.domain.user.entity.User
import com.coing.global.annotation.NoArg
import com.coing.util.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
@NoArg
open class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    // 이 메시지가 속한 채팅방
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "chat_room_id", nullable = false)
    open var chatRoom: ChatRoom? = null,

    // 메시지를 보낸 유저
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "user_id", nullable = false)
    open var sender: User? = null,

    // 메시지 내용
    @Column(nullable = false, length = 1000)
    open var content: String = "",

    // 메시지 전송 시간
    open var timestamp: LocalDateTime? = null
) : BaseEntity()