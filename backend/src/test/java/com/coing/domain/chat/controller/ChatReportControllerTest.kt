package com.coing.domain.chat.controller

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.service.ChatReportService
import com.coing.domain.chat.service.ChatService
import com.coing.domain.user.entity.Authority
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.util.BasicResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(ChatReportController::class)
@MockBean(JpaMetamodelMappingContext::class) // JPA 메타모델 초기화 오류 방지
class ChatReportControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var chatService: ChatService

    @MockBean
    lateinit var chatReportService: ChatReportService

    // 테스트에서 사용할 도메인 User를 하나 생성하고 재사용합니다.
    private fun createDummyUser(): User {
        return User(
            id = UUID.randomUUID(),
            name = "TestUser",
            email = "test@example.com",
            password = "encodedPassword",
            authority = Authority.ROLE_USER,
            verified = true,
            provider = Provider.EMAIL
        )
    }

    // 생성한 User 객체를 기반으로 인증 객체를 생성합니다.
    private fun createAuthentication(user: User): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken(
            user,
            "password",
            listOf(SimpleGrantedAuthority("USER"))
        )
    }

    @Test
    fun `신고 API - 메시지 없음 - 404 반환`() {
        `when`(chatService.findMessageById(1L)).thenReturn(null)

        // 별도 User 객체는 사용하지 않아도 되며, 여기서는 임의 인증 객체 생성
        val dummyUser = createDummyUser()
        val auth = createAuthentication(dummyUser)

        mockMvc.perform(
            post("/api/chat/messages/1/report")
                .with(authentication(auth))
                .with(csrf())
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("메시지를 찾을 수 없습니다."))
    }

    @Test
    fun `신고 API - 신고 성공 - OK 반환`() {
        // 신고 성공 케이스도 동일한 User 객체를 사용합니다.
        val userEntity = createDummyUser()
        val auth = createAuthentication(userEntity)

        val chatMessage = ChatMessage(
            id = 1L,
            chatRoom = null, // 테스트에서는 채팅방 정보는 불필요
            sender = userEntity,  // 여기서 동일한 userEntity 사용
            content = "Hello, world!",
            timestamp = null
        )

        `when`(chatService.findMessageById(1L)).thenReturn(chatMessage)
        // 신고 성공 시 리턴값은 사용하지 않으므로 null 반환 (또는 실제 ChatMessageReport 인스턴스 가능)
        `when`(chatReportService.reportMessage(chatMessage, userEntity)).thenReturn(null)

        mockMvc.perform(
            post("/api/chat/messages/1/report")
                .with(authentication(auth))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("메시지가 신고되었습니다."))
    }

    @Test
    fun `신고 API - 신고 중복 등 예외 발생 - 400 반환`() {
        // 신고 중복 예외 발생 케이스에서도 동일한 User 객체 사용
        val userEntity = createDummyUser()
        val auth = createAuthentication(userEntity)

        val chatMessage = ChatMessage(
            id = 1L,
            chatRoom = null,
            sender = userEntity,  // 동일한 userEntity
            content = "Hello, world!",
            timestamp = null
        )

        `when`(chatService.findMessageById(1L)).thenReturn(chatMessage)
        `when`(chatReportService.reportMessage(chatMessage, userEntity))
            .thenThrow(RuntimeException("이미 신고한 메시지입니다."))

        mockMvc.perform(
            post("/api/chat/messages/1/report")
                .with(authentication(auth))
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("이미 신고한 메시지입니다."))
    }
}
