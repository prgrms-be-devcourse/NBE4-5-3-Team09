package com.coing.domain.bookmark.entity

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.user.entity.User
import com.coing.util.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(
    name = "bookmark",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "market_id"])]
)
class Bookmark(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    val id: Long? = null,

    // 북마크를 등록한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val user: User,

    // 북마크 대상 마켓
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    val market: Market

) : BaseEntity()
