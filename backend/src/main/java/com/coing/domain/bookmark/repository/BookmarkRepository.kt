package com.coing.domain.bookmark.repository

import com.coing.domain.bookmark.entity.Bookmark
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Long> {

    fun existsByUserIdAndMarketCode(userId: UUID, coinCode: String): Boolean

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.market.code LIKE CONCAT(:quote, '%')")
    fun findByUserIdAndQuote(
        @Param("userId") userId: UUID,
        @Param("quote") quote: String
    ): List<Bookmark>

    fun findByMarketCode(code: String): Bookmark?

    fun findByUserId(userId: UUID): List<Bookmark>
}
