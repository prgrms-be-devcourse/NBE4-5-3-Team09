package com.coing.domain.chat.entity

import com.coing.domain.coin.market.entity.Market
import com.coing.util.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_rooms")
open class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    // 해당 채팅방이 연결된 마켓 (예: "KRW-BTC")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    open var market: Market? = null,

    // 채팅방 이름 (마켓의 한글 이름 등으로 지정 가능)
    @Column(nullable = false)
    open var name: String = "",

    // 채팅방 생성 시간
    open var createdAt: LocalDateTime? = null
) : BaseEntity() {
    // JPA용 기본 생성자
    constructor() : this(
        id = null,
        market = null,
        name = "",
        createdAt = null
    )
}
