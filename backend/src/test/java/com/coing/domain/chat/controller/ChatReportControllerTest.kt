package com.coing.domain.chat.controller

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.service.ChatReportService
import com.coing.domain.chat.service.ChatService
import com.coing.domain.user.entity.Authority
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.domain.user.repository.UserRepository
import com.coing.util.BasicResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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

    @MockBean
    lateinit var userRepository: UserRepository

    // 테스트에서 사용할 User 생성 (id 타입은 UUID)
    private fun createDummyUser(): User {
        return User(
            id = UUID.randomUUID(), // UUID 객체 그대로 사용
            name = "TestUser",
            email = "test@example.com",
            password = "encodedPassword",
            authority = Authority.ROLE_USER,
            verified = true,
            provider = Provider.EMAIL
        )
    }

    // User를 바탕으로 CustomUserPrincipal을 생성하여 인증객체로 사용
    private fun createAuthentication(user: User): UsernamePasswordAuthenticationToken {
        // CustomUserPrincipal 생성 시 UUID (non-null)와 권한 목록을 전달합니다.
        val principal = CustomUserPrincipal(user.id!!, listOf(SimpleGrantedAuthority("ROLE_USER")))
        return UsernamePasswordAuthenticationToken(principal, "password", listOf(SimpleGrantedAuthority("ROLE_USER")))
    }

    @Test
    fun `신고 API - 메시지 없음 - 404 반환`() {
        // 메시지 id는 String 타입 ("1")
        `when`(chatService.findMessageById("1")).thenReturn(null)

        val dummyUser = createDummyUser()
        // 인증된 사용자 조회를 위한 stub 설정
        `when`(userRepository.findById(dummyUser.id!!)).thenReturn(Optional.of(dummyUser))

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
        val userEntity = createDummyUser()
        // 인증된 사용자 조회를 위한 stub 설정
        `when`(userRepository.findById(userEntity.id!!)).thenReturn(Optional.of(userEntity))

        val auth = createAuthentication(userEntity)

        // 신고할 메시지 생성, id는 "1" (String 타입)
        val chatMessage = ChatMessage(
            id = "1",
            chatRoom = null, // 테스트에서는 채팅방 정보는 불필요
            sender = userEntity, // 작성자가 동일한 사용자
            content = "Hello, world!",
            timestamp = null
        )

        `when`(chatService.findMessageById("1")).thenReturn(chatMessage)
        // 신고 성공 시 리턴값은 실제 신고 레코드가 있거나 null로 처리
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
        val userEntity = createDummyUser()
        // 인증된 사용자 조회를 위한 stub 설정
        `when`(userRepository.findById(userEntity.id!!)).thenReturn(Optional.of(userEntity))

        val auth = createAuthentication(userEntity)

        val chatMessage = ChatMessage(
            id = "1",
            chatRoom = null,
            sender = userEntity,
            content = "Hello, world!",
            timestamp = null
        )

        `when`(chatService.findMessageById("1")).thenReturn(chatMessage)
        // 신고 중복 등으로 인한 예외 발생 처리
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
