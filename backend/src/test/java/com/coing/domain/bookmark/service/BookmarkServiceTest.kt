package com.coing.domain.bookmark.service

import com.coing.domain.bookmark.controller.dto.BookmarkRequest
import com.coing.domain.bookmark.entity.Bookmark
import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.domain.user.entity.Authority
import com.coing.domain.user.entity.Provider.EMAIL
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class BookmarkServiceTest {

    @Mock
    private lateinit var bookmarkRepository: BookmarkRepository

    @Mock
    private lateinit var marketRepository: MarketRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var messageUtil: MessageUtil

    @InjectMocks
    private lateinit var bookmarkService: BookmarkService

    @Test
    fun `addBookmark - 북마크 추가 성공`() {
        // given
        val userId = UUID.randomUUID()
        val marketCode = "BTC-KRW"
        val request = BookmarkRequest(marketCode)
        val principal = CustomUserPrincipal(userId)
        val user = createUser(userId)
        val market = createMarket(marketCode)

        `when`(bookmarkRepository.existsByUserIdAndMarketCode(userId, marketCode)).thenReturn(false)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(marketRepository.findById(marketCode)).thenReturn(Optional.of(market))

        // when
        bookmarkService.addBookmark(request, principal)

        // then
        verify(bookmarkRepository, times(1)).save(any(Bookmark::class.java))
    }

    @Test
    fun `addBookmark - 이미 북마크가 존재하는 경우 예외 발생`() {
        // given
        val userId = UUID.randomUUID()
        val marketCode = "BTC-KRW"
        val principal = CustomUserPrincipal(userId)

        `when`(bookmarkRepository.existsByUserIdAndMarketCode(userId, marketCode)).thenReturn(true)
        `when`(messageUtil.resolveMessage("bookmark.already.exists")).thenReturn("이미 북마크가 존재합니다.")

        // when & then
        val exception = assertThrows<BusinessException> {
            bookmarkService.addBookmark(BookmarkRequest(marketCode), principal)
        }

        assertThat(exception.message).isEqualTo("이미 북마크가 존재합니다.")
    }

    @Test
    fun `deleteBookmark - 본인 북마크 삭제 성공`() {
        // given
        val userId = UUID.randomUUID()
        val marketCode = "BTC-KRW"
        val user = createUser(userId)
        val market = createMarket(marketCode)
        val bookmark = createBookmark(user, market)

        `when`(bookmarkRepository.findByMarketCode(marketCode)).thenReturn(bookmark)

        // when
        bookmarkService.deleteBookmark(userId, marketCode)

        // then
        verify(bookmarkRepository, times(1)).delete(bookmark)
    }

    @Test
    fun `deleteBookmark - 다른 유저의 북마크 삭제 시도시 예외 발생`() {
        // given
        val userId = UUID.randomUUID()
        val otherUser = createUser(UUID.randomUUID(), "다른 테스트 유저", "other@test.com")
        val market = createMarket("BTC-KRW")
        val bookmark = createBookmark(otherUser, market)

        `when`(bookmarkRepository.findByMarketCode("BTC-KRW")).thenReturn(bookmark)
        `when`(messageUtil.resolveMessage("bookmark.access.denied")).thenReturn("접근 권한이 없습니다.")

        // when & then
        val exception = assertThrows<BusinessException> {
            bookmarkService.deleteBookmark(userId, "BTC-KRW")
        }

        assertThat(exception.message).isEqualTo("접근 권한이 없습니다.")
    }

    private fun createUser(
        id: UUID = UUID.randomUUID(),
        name: String = "테스트 유저",
        email: String = "test@test.com"
    ): User {
        return User(
            id = id,
            name = name,
            email = email,
            password = "1234",
            authority = Authority.ROLE_USER,
            verified = true,
            provider = EMAIL
        )
    }

    private fun createMarket(code: String): Market {
        return Market(
            code = code,
            koreanName = "비트코인",
            englishName = "Bitcoin"
        )
    }

    private fun createBookmark(user: User, market: Market): Bookmark {
        return Bookmark(
            user = user,
            market = market
        )
    }
}
