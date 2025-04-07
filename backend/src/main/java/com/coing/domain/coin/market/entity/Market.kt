package com.coing.domain.coin.market.entity

import com.coing.util.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Market(

    @Id
    @Column(name = "market_id")
    val code: String,

    @Column(name = "korean_name", nullable = false)
    val koreanName: String,

    @Column(name = "english_name", nullable = false)
    val englishName: String

) : BaseEntity()
