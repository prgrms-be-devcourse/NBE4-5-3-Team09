package com.coing.domain.bookmark.service

import com.coing.domain.bookmark.controller.dto.BookmarkRequest
import com.coing.domain.bookmark.controller.dto.BookmarkResponse
import com.coing.domain.bookmark.entity.Bookmark
import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.coing.util.PageUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val marketRepository: MarketRepository,
    private val userRepository: UserRepository,
    private val messageUtil: MessageUtil
) {

    @Transactional
    fun addBookmark(request: BookmarkRequest, principal: CustomUserPrincipal?) {
        if (principal == null) {
            throw BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND)
        }

        val userId = principal.id
        val coinCode = request.coinCode

        // 북마크가 이미 존재하는지 확인
        if (bookmarkRepository.existsByUserIdAndMarketCode(userId, coinCode)) {
            throw BusinessException(messageUtil.resolveMessage("bookmark.already.exists"), HttpStatus.BAD_REQUEST)
        }

        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND)
        }

        val market = marketRepository.findById(coinCode).orElseThrow {
            BusinessException(messageUtil.resolveMessage("market.not.found"), HttpStatus.NOT_FOUND)
        }

        val bookmark = Bookmark(
            user = user,
            market = market
        )

        bookmarkRepository.save(bookmark)
    }

    @Transactional(readOnly = true)
    fun getBookmarksByQuote(
        principal: CustomUserPrincipal?,
        quote: String,
        pageable: Pageable
    ): Page<BookmarkResponse> {
        if (principal == null) {
            throw BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND)
        }

        val userId = principal.id
        val bookmarks = bookmarkRepository.findByUserIdAndQuote(userId, quote)

        val responses = bookmarks.map { BookmarkResponse.of(it) }

        return PageUtil.paginate(responses, pageable)
    }

    @Transactional
    fun deleteBookmark(userId: UUID, marketCode: String) {
        val bookmark = bookmarkRepository.findByMarketCode(marketCode)
            ?: throw BusinessException(messageUtil.resolveMessage("bookmark.not.found"), HttpStatus.NOT_FOUND)

        // 요청한 유저가 소유자인지 검증
        if (bookmark.user.id != userId) {
            throw BusinessException(messageUtil.resolveMessage("bookmark.access.denied"), HttpStatus.FORBIDDEN)
        }

        bookmarkRepository.delete(bookmark)
    }
}
