package com.coing.domain.user.service

import com.coing.domain.user.controller.UserController
import com.coing.domain.user.controller.dto.*
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.domain.user.entity.Authority
import com.coing.domain.user.repository.UserRepository
import com.coing.domain.user.email.service.EmailVerificationService
import com.coing.domain.user.email.service.PasswordResetService
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

internal class UserControllerTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var authTokenService: AuthTokenService
    private lateinit var emailVerificationService: EmailVerificationService
    private lateinit var messageUtil: MessageUtil
    private lateinit var passwordResetService: PasswordResetService

    private lateinit var userController: UserController
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse

    @BeforeEach
    fun setUp() {
        userService = mock(UserService::class.java)
        userRepository = mock(UserRepository::class.java)
        authTokenService = mock(AuthTokenService::class.java)
        emailVerificationService = mock(EmailVerificationService::class.java)
        messageUtil = mock(MessageUtil::class.java)
        passwordResetService = mock(PasswordResetService::class.java)

        userController = UserController(
            userService,
            userRepository,
            authTokenService,
            emailVerificationService,
            messageUtil,
            passwordResetService
        )

        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)

        // 메시지 해석: 입력받은 메시지 코드를 그대로 반환하도록 설정
        `when`(messageUtil.resolveMessage(anyString())).thenAnswer { invocation -> invocation.getArgument(0) }
    }

    @Test
    @DisplayName("회원가입 - 성공")
    fun testSignUpSuccess() {
        val signupRequest = UserSignUpRequest("테스트", "test@test.com", "pass1234!", "pass1234!")
        val userResponse = UserResponse(UUID.randomUUID(), "테스트", "test@test.com", false)
        `when`(userService.join(signupRequest)).thenReturn(userResponse)

        val result: ResponseEntity<UserSignupResponse> = userController.signUp(signupRequest, response)
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("test@test.com", result.body?.email)
        // 이메일 발송은 서비스 내에서 처리되므로 별도 검증은 서비스 테스트에서 진행
    }

    @Test
    @DisplayName("이메일 인증 - 토큰 유효하지 않음")
    fun testVerifyEmailInvalidToken() {
        `when`(authTokenService.parseId("invalidToken")).thenReturn(null)
        val result: ResponseEntity<*> = userController.verifyEmail("invalidToken")
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("error", body["status"])
        assertEquals("유효하지 않은 토큰입니다.", body["message"])
    }

    @Test
    @DisplayName("이메일 인증 - 이미 인증됨")
    fun testVerifyEmailAlreadyVerified() {
        val userId = UUID.randomUUID()
        `when`(authTokenService.parseId("validToken")).thenReturn(userId)
        val user = User(userId, "테스트", "test@test.com", "dummy", Authority.USER, true)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result: ResponseEntity<*> = userController.verifyEmail("validToken")
        val body = result.body as Map<*, *>
        assertEquals("already", body["status"])
        assertEquals("이미 인증되었습니다.", body["message"])
    }

    @Test
    @DisplayName("이메일 인증 - 성공")
    fun testVerifyEmailSuccess() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            name = "테스트",
            email = "test@test.com",
            password = "dummy",
            provider = Provider.EMAIL,
            verified = false
        )
        // authTokenService.parseId("validToken")가 userId를 반환하도록 stubbing
        `when`(authTokenService.parseId("validToken")).thenReturn(userId)
        // userRepository에서 해당 user를 반환하도록 stubbing
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        // emailVerificationService.verifyEmail(userId)가 인증된 user를 반환하도록 stubbing
        `when`(emailVerificationService.verifyEmail(userId))
            .thenReturn(user.copy(verified = true))

        val result: ResponseEntity<*> = userController.verifyEmail("validToken")
        val body = result.body as Map<*, *>
        assertEquals("success", body["status"])
        assertEquals("이메일 인증이 완료되었습니다.", body["message"])
    }

    @Test
    @DisplayName("이메일 인증 상태 확인 - 사용자 미존재")
    fun testIsVerifiedUserNotFound() {
        val userId = UUID.randomUUID()
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        val exception = assertThrows(BusinessException::class.java) {
            userController.isVerified(userId)
        }
        assertEquals("member.not.found", exception.message)
    }

    @Test
    @DisplayName("이메일 인증 메일 재전송 - 이미 인증된 사용자")
    fun testResendEmailAlreadyVerified() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            name = "테스트",
            email = "test@test.com",
            password = "dummy",
            provider = Provider.EMAIL,
            verified = true
        )
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        val result: ResponseEntity<*> = userController.resendEmail(userId)
        val body = result.body as Map<*, *>
        assertEquals("already_verified", body["status"])
        assertEquals("이미 인증된 사용자입니다.", body["message"])
    }

    @Test
    @DisplayName("이메일 인증 메일 재전송 - 성공")
    fun testResendEmailSuccess() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            name = "테스트",
            email = "test@test.com",
            password = "dummy",
            provider = Provider.EMAIL,
            verified = false
        )
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        doNothing().`when`(emailVerificationService).resendVerificationEmail(userId)
        val result: ResponseEntity<*> = userController.resendEmail(userId)
        val body = result.body as Map<*, *>
        assertEquals("success", body["status"])
        assertEquals("이메일이 재전송되었습니다.", body["message"])
    }

    @Test
    @DisplayName("일반 유저 로그인 - 성공")
    fun testLoginSuccess() {
        val email = "test@test.com"
        val password = "pass1234!"
        val userResponse = UserResponse(UUID.randomUUID(), "테스트", email, true)
        `when`(userService.login(email, password)).thenReturn(userResponse)

        // 모의 response 객체에서 헤더 설정을 확인하기 위해 stub 처리
        doNothing().`when`(response).setHeader(anyString(), anyString())
        val result: ResponseEntity<*> = userController.login(UserLoginRequest(email, password), response)
        assertEquals(HttpStatus.OK, result.statusCode)
        verify(response, atLeastOnce()).setHeader(eq("Set-Cookie"), anyString())
        verify(response, atLeastOnce()).setHeader(eq("Authorization"), anyString())
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    fun testRefreshTokenSuccess() {
        val refreshToken = "dummyRefreshToken"
        val cookie = Cookie("refreshToken", refreshToken)
        `when`(request.cookies).thenReturn(arrayOf(cookie))
        val userId = UUID.randomUUID()
        val claims = mapOf("id" to userId.toString())
        `when`(authTokenService.verifyToken(refreshToken)).thenReturn(claims)
        val userResponse = UserResponse(userId, "테스트", "test@test.com", true)
        `when`(userService.findById(userId)).thenReturn(userResponse)

        doNothing().`when`(response).setHeader(anyString(), anyString())
        val result: ResponseEntity<*> = userController.refreshToken(request, response)
        assertEquals(HttpStatus.OK, result.statusCode)
        verify(response, atLeastOnce()).setHeader(eq("Set-Cookie"), anyString())
        verify(response, atLeastOnce()).setHeader(eq("Authorization"), anyString())
    }

    @Test
    @DisplayName("회원 로그아웃")
    fun testLogout() {
        doNothing().`when`(response).setHeader(anyString(), anyString())
        val result: ResponseEntity<*> = userController.logout(request, response)
        assertEquals(HttpStatus.OK, result.statusCode)
        verify(response, atLeastOnce()).setHeader(eq("Set-Cookie"), anyString())
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    fun testSignOutSuccess() {
        val principal = CustomUserPrincipal(UUID.randomUUID())
        val signOutRequest = SignOutRequest("pass1234!")
        doNothing().`when`(userService).quit(principal.id, signOutRequest.password)

        val result: ResponseEntity<*> = userController.signOut(signOutRequest, principal)
        assertEquals(HttpStatus.OK, result.statusCode)
        verify(userService, times(1)).quit(principal.id, signOutRequest.password)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    @DisplayName("회원 정보 조회 - 성공")
    fun testGetUserInfoSuccess() {
        val principal = CustomUserPrincipal(UUID.randomUUID())
        val userResponse = UserResponse(principal.id, "테스트", "test@test.com", true)
        `when`(userService.findById(principal.id)).thenReturn(userResponse)
        val result = userController.getUserInfo(principal) as? ResponseEntity<UserResponse>
            ?: throw IllegalStateException("Expected ResponseEntity<UserResponse>")
        assertEquals(HttpStatus.OK, result.statusCode)
        val responseBody = result.body as UserResponse
        assertEquals("test@test.com", responseBody.email)
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 사용자 미존재")
    fun testRequestPasswordResetUserNotFound() {
        val requestDto = PasswordResetRequest("test@test.com")
        `when`(userRepository.findByEmail(requestDto.email)).thenReturn(Optional.empty())
        val result: ResponseEntity<*> = userController.requestPasswordReset(requestDto)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("error", body["status"])
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 성공")
    fun testRequestPasswordResetSuccess() {
        val requestDto = PasswordResetRequest("test@test.com")
        val user = User(
            id = UUID.randomUUID(),
            name = "테스트",
            email = requestDto.email,
            password = "dummy",
            provider = Provider.EMAIL,
            verified = true
        )
        `when`(userRepository.findByEmail(requestDto.email)).thenReturn(Optional.of(user))
        doNothing().`when`(passwordResetService).sendPasswordResetEmail(user)
        val result: ResponseEntity<*> = userController.requestPasswordReset(requestDto)
        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("success", body["status"])
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 - 토큰 유효하지 않음")
    fun testConfirmPasswordResetInvalidToken() {
        `when`(authTokenService.parseId("invalidToken")).thenReturn(null)
        val requestDto = PasswordResetConfirmRequest("newPass1!", "newPass1!")
        val result: ResponseEntity<*> = userController.confirmPasswordReset("invalidToken", requestDto)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("error", body["status"])
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 - 비밀번호 불일치")
    fun testConfirmPasswordResetMismatch() {
        `when`(authTokenService.parseId("validToken")).thenReturn(UUID.randomUUID())
        val requestDto = PasswordResetConfirmRequest("newPass1!", "differentPass!")
        val result: ResponseEntity<*> = userController.confirmPasswordReset("validToken", requestDto)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("error", body["status"])
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 - 성공")
    fun testConfirmPasswordResetSuccess() {
        val userId = UUID.randomUUID()
        `when`(authTokenService.parseId("validToken")).thenReturn(userId)
        val requestDto = PasswordResetConfirmRequest("newPass1!", "newPass1!")
        doNothing().`when`(userService).updatePassword(userId, requestDto.newPassword)

        val result: ResponseEntity<*> = userController.confirmPasswordReset("validToken", requestDto)
        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("success", body["status"])
    }
}
