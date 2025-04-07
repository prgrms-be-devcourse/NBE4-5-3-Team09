package com.coing.domain.coin.market.repository

import com.coing.domain.coin.market.entity.Market
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MarketRepository : JpaRepository<Market, String> {
	fun findByCode(code: String): Market?
}
