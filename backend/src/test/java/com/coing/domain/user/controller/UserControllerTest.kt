package com.coing.domain.user.controller

import com.coing.CoingApplication
import com.coing.domain.user.controller.dto.*
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.domain.user.service.AuthTokenService
import com.coing.domain.user.service.UserService
import com.coing.domain.user.email.service.EmailVerificationService
import com.coing.domain.user.email.service.PasswordResetService
import com.coing.util.MessageUtil
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@SpringBootTest(classes = [CoingApplication::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var userService: UserService

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var authTokenService: AuthTokenService

    @MockBean
    lateinit var emailVerificationService: EmailVerificationService

    @MockBean
    lateinit var messageUtil: MessageUtil

    @MockBean
    lateinit var passwordResetService: PasswordResetService

    // 회원가입 통합 테스트
//    @Test
//    fun `회원가입 - 성공`() {
//        val signUpRequest = UserSignUpRequest(
//            name = "테스트",
//            email = "integration@test.com",
//            password = "pass1234!",
//            passwordConfirm = "pass1234!"
//        )
//        val userResponse = UserResponse(
//            id = UUID.randomUUID(),
//            name = "테스트",
//            email = "integration@test.com",
//            verified = false
//        )
//        given(userService.join(any())).willReturn(userResponse)
//
//        mockMvc.perform(
//            post("/api/auth/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signUpRequest))
//        )
//            .andExpect(status().isCreated)
//            .andExpect(jsonPath("$.email", `is`("integration@test.com")))
//            .andExpect(jsonPath("$.userId", not(emptyString())))
//    }

    // 이메일 인증 - 토큰 유효하지 않음 통합 테스트
    @Test
    fun `이메일 인증 - 토큰 유효하지 않음`() {
        given(authTokenService.parseId("invalidToken")).willReturn(null)

        mockMvc.perform(get("/api/auth/verify-email").param("token", "invalidToken"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status", `is`("error")))
            .andExpect(jsonPath("$.message", `is`("유효하지 않은 토큰입니다.")))
    }

    // 이메일 인증 - 성공 (미인증 사용자) 통합 테스트
    @Test
    fun `이메일 인증 - 성공`() {
        val userId = UUID.randomUUID()
        given(authTokenService.parseId("validToken")).willReturn(userId)
        val user = User(
            id = userId,
            name = "테스트",
            email = "integration@test.com",
            password = "encodedPassword",
            provider = Provider.EMAIL,
            verified = false
        )
        given(userRepository.findById(userId)).willReturn(Optional.of(user))

        mockMvc.perform(get("/api/auth/verify-email").param("token", "validToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("success")))
            .andExpect(jsonPath("$.message", `is`("이메일 인증이 완료되었습니다.")))
    }

    // 이메일 인증 - 이미 인증된 사용자 통합 테스트
    @Test
    fun `이메일 인증 - 이미 인증됨`() {
        val userId = UUID.randomUUID()
        given(authTokenService.parseId("validToken")).willReturn(userId)
        val user = User(
            id = userId,
            name = "테스트",
            email = "integration@test.com",
            password = "encodedPassword",
            provider = Provider.EMAIL,
            verified = true
        )
        given(userRepository.findById(userId)).willReturn(Optional.of(user))

        mockMvc.perform(get("/api/auth/verify-email").param("token", "validToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("already")))
            .andExpect(jsonPath("$.message", `is`("이미 인증되었습니다.")))
    }

    // 일반 유저 로그인 통합 테스트
    @Test
    fun `일반 유저 로그인 - 성공`() {
        val loginRequest = UserLoginRequest(
            email = "integration@test.com",
            password = "pass1234!"
        )
        val userResponse = UserResponse(
            id = UUID.randomUUID(),
            name = "테스트",
            email = "integration@test.com",
            verified = true
        )
        given(userService.login("integration@test.com", "pass1234!")).willReturn(userResponse)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", `is`("로그인 성공")))
    }

    // 토큰 재발급 - 실패 (쿠키 없음) 통합 테스트
    @Test
    fun `토큰 재발급 - 실패 (쿠키 없음)`() {
        mockMvc.perform(post("/api/auth/refresh"))
            .andExpect(status().isForbidden)
    }

    // 로그아웃 통합 테스트
    @Test
    fun `회원 로그아웃 - 성공`() {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", containsString("로그아웃 성공")))
    }

    // 회원 탈퇴 통합 테스트
//    @Test
//    fun `회원 탈퇴 - 성공`() {
//        val signOutRequest = SignOutRequest(password = "pass1234!")
//        // 실제 서비스에서는 회원 탈퇴 로직이 실행된다고 가정
//        mockMvc.perform(
//            delete("/api/auth/signout")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signOutRequest))
//        )
//            .andExpect(status().isOk)
//            .andExpect(content().string(containsString("회원 탈퇴 성공")))
//    }

    // 비밀번호 재설정 요청 통합 테스트
    @Test
    fun `비밀번호 재설정 요청 - 성공`() {
        val resetRequest = PasswordResetRequest(email = "integration@test.com")
        val user = User(
            id = UUID.randomUUID(),
            name = "테스트",
            email = "integration@test.com",
            password = "encodedPassword",
            provider = Provider.EMAIL,
            verified = true
        )
        given(userRepository.findByEmail("integration@test.com")).willReturn(Optional.of(user))

        mockMvc.perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("success")))
            .andExpect(jsonPath("$.message", `is`("비밀번호 재설정 이메일 전송되었습니다.")))
    }

    // 비밀번호 재설정 확인 - 비밀번호 불일치 통합 테스트
//    @Test
//    fun `비밀번호 재설정 확인 - 비밀번호 불일치`() {
//        val token = "validToken"
//        val resetConfirmRequest = PasswordResetConfirmRequest(
//            newPassword = "newPass1!",
//            newPasswordConfirm = "differentPass!"
//        )
//        mockMvc.perform(
//            post("/api/auth/password-reset/confirm")
//                .param("token", token)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(resetConfirmRequest))
//        )
//            .andExpect(status().isBadRequest)
//            .andExpect(jsonPath("$.status", `is`("error")))
//            .andExpect(jsonPath("$.message", `is`("비밀번호 확인이 일치하지 않습니다.")))
//    }

    // 비밀번호 재설정 확인 - 성공 통합 테스트
    @Test
    fun `비밀번호 재설정 확인 - 성공`() {
        val token = "validToken"
        val resetConfirmRequest = PasswordResetConfirmRequest(
            newPassword = "newPass1!",
            newPasswordConfirm = "newPass1!"
        )
        val userId = UUID.randomUUID()
        given(authTokenService.parseId(token)).willReturn(userId)
        // 실제 updatePassword()가 정상 수행된다고 가정
        mockMvc.perform(
            post("/api/auth/password-reset/confirm")
                .param("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetConfirmRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("success")))
            .andExpect(jsonPath("$.message", `is`("비밀번호가 재설정되었습니다.")))
    }

    // 리다이렉트 엔드포인트 통합 테스트
    @Test
    fun `리다이렉트`() {
        mockMvc.perform(get("/api/auth"))
            .andExpect(status().isOk)
    }
}
