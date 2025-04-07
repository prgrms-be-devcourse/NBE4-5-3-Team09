package com.coing.domain.coin.market.entity

import com.coing.global.annotation.NoArg
import com.coing.util.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "market")
@NoArg
data class Market(

	@Id
	@Column(name = "market_id")
	val code: String,

	@Column(name = "korean_name", nullable = false)
	val koreanName: String,

	@Column(name = "english_name", nullable = false)
	val englishName: String

) : BaseEntity()