package com.coing.domain.notification.entity

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.user.entity.User
import jakarta.persistence.*

@Entity
class Subscribe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id")
    val market: Market,

    @Enumerated(EnumType.STRING)
    var oneMinuteRate: OneMinuteRate = OneMinuteRate.NONE
)
