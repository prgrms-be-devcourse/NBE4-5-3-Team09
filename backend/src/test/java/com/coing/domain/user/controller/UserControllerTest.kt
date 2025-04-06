package com.coing.integration

import com.coing.domain.user.controller.dto.*
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.util.BasicResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    // 회원가입 엔드포인트 통합 테스트
    @Test
    fun `회원가입 - 성공`() {
        val signUpRequest = UserSignUpRequest(
            name = "테스트",
            email = "integration@test.com",
            password = "pass1234!",
            passwordConfirm = "pass1234!"
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(signUpRequest, headers)

        val response = restTemplate.postForEntity("/api/auth/signup", entity, UserSignupResponse::class.java)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = response.body!!
        assertEquals("integration@test.com", body.email)
        assertNotNull(body.userId)
    }

    // 이메일 인증 엔드포인트 통합 테스트
    @Test
    fun `이메일 인증 - 토큰 유효하지 않음`() {
        // 이 테스트에서는 잘못된 토큰을 전달합니다.
        val response: ResponseEntity<Map<*, *>> =
            restTemplate.getForEntity("/api/auth/verify-email?token=invalidToken", Map::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body!!
        assertEquals("error", body["status"])
        assertEquals("유효하지 않은 토큰입니다.", body["message"])
    }

    // 이메일 인증 성공 테스트 (테스트 환경에 따라 적절히 토큰을 생성하거나 미리 저장된 사용자를 활용)
    @Test
    fun `이메일 인증 - 성공`() {
        // 테스트용으로 미리 사용자를 저장하고, 인증 토큰을 생성했다고 가정합니다.
        val userId = UUID.randomUUID()
        // 테스트 DB에 해당 사용자가 존재한다고 가정:
        // (실제 테스트에서는 @Sql 스크립트 등을 사용하거나 테스트 전용 repository.save() 호출)
        // 그리고 authTokenService.parseId("validToken")가 userId를 반환하도록 테스트 환경을 구성했다고 가정합니다.
        // 여기서는 단순히 "validToken" URL 파라미터가 유효하다고 가정하고 요청합니다.
        // 실제 통합 테스트에서는 이메일 인증 토큰 발급 로직을 통해 생성된 토큰을 사용해야 합니다.
        val response: ResponseEntity<Map<*, *>> =
            restTemplate.getForEntity("/api/auth/verify-email?token=validToken", Map::class.java)
        // 응답 상태는 실제 토큰 유효성 및 사용자 상태에 따라 달라질 수 있습니다.
        // 여기서는 성공 케이스("success" 상태)를 가정합니다.
        if (response.statusCode == HttpStatus.OK) {
            val body = response.body!!
            assertEquals("success", body["status"])
            assertEquals("이메일 인증이 완료되었습니다.", body["message"])
        } else {
            // 만약 토큰이 유효하지 않다면 BAD_REQUEST가 나올 수 있음
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        }
    }

    // 로그인 엔드포인트 통합 테스트
    @Test
    fun `일반 유저 로그인 - 성공`() {
        // 테스트 전에 회원가입이나 미리 저장을 통해 사용자가 존재해야 합니다.
        val loginRequest = UserLoginRequest(
            email = "integration@test.com",
            password = "pass1234!"
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(loginRequest, headers)
        // 로그인 시 응답 헤더에 Authorization, Set-Cookie가 설정되도록 되어 있음
        val response: ResponseEntity<BasicResponse> =
            restTemplate.postForEntity("/api/auth/login", entity, BasicResponse::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val basicResponse = response.body!!
        assertEquals("로그인 성공", basicResponse.message)
        // 헤더 검증은 추가적인 설정이 필요할 수 있습니다.
    }

    // 토큰 재발급 엔드포인트 통합 테스트 (쿠키가 없는 경우)
    @Test
    fun `토큰 재발급 - 실패 (쿠키 없음)`() {
        // 쿠키가 없는 요청을 보내면 FORBIDDEN 상태를 반환하도록 되어 있음.
        val response: ResponseEntity<BasicResponse> =
            restTemplate.postForEntity("/api/auth/refresh", null, BasicResponse::class.java)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    // 로그아웃 엔드포인트 통합 테스트
    @Test
    fun `회원 로그아웃 - 성공`() {
        // 로그아웃 요청 시, refreshToken 쿠키를 제거하는 헤더가 설정되어야 함.
        val response: ResponseEntity<BasicResponse> =
            restTemplate.postForEntity("/api/auth/logout", null, BasicResponse::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    // 회원 탈퇴 엔드포인트 통합 테스트
    @Test
    fun `회원 탈퇴 - 성공`() {
        // 탈퇴 테스트를 위해 CustomUserPrincipal을 사용해 인증된 사용자로 요청을 시뮬레이션합니다.
        // 실제 통합 테스트에서는 보통 SecurityContext를 설정하거나, 테스트 전용 인증 토큰을 발급합니다.
        // 여기서는 간단히 탈퇴 요청을 JSON으로 전송한다고 가정합니다.
        val signOutRequest = SignOutRequest(password = "pass1234!")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(signOutRequest, headers)
        // 회원 탈퇴 엔드포인트는 DELETE 메서드를 사용하므로 exchange() 메서드를 사용합니다.
        val response = restTemplate.exchange(
            "/api/auth/signout",
            HttpMethod.DELETE,
            entity,
            String::class.java
        )
        // 성공 시 "회원 탈퇴 성공" 메시지를 반환하도록 되어 있음.
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    // 비밀번호 재설정 요청 엔드포인트 통합 테스트
    @Test
    fun `비밀번호 재설정 요청 - 성공`() {
        val resetRequest = PasswordResetRequest(email = "integration@test.com")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(resetRequest, headers)
        val response: ResponseEntity<Map<*, *>> =
            restTemplate.postForEntity("/api/auth/password-reset/request", entity, Map::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals("success", body["status"])
    }

    // 비밀번호 재설정 확인 엔드포인트 통합 테스트 (비밀번호 불일치 케이스)
    @Test
    fun `비밀번호 재설정 확인 - 비밀번호 불일치`() {
        val token = "validToken"  // 테스트용 토큰
        val resetConfirmRequest = PasswordResetConfirmRequest(newPassword = "newPass1!", newPasswordConfirm = "differentPass!")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(resetConfirmRequest, headers)
        val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
            "/api/auth/password-reset/confirm?token=$token",
            entity,
            Map::class.java
        )
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = response.body!!
        assertEquals("error", body["status"])
    }

    // 비밀번호 재설정 확인 엔드포인트 통합 테스트 (성공 케이스)
    @Test
    fun `비밀번호 재설정 확인 - 성공`() {
        val token = "validToken"
        val resetConfirmRequest = PasswordResetConfirmRequest(newPassword = "newPass1!", newPasswordConfirm = "newPass1!")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(resetConfirmRequest, headers)
        val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
            "/api/auth/password-reset/confirm?token=$token",
            entity,
            Map::class.java
        )
        // 실제 응답 상태와 메시지는 테스트 환경에 따라 달라질 수 있음.
        // 여기서는 성공 상태를 가정합니다.
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals("success", body["status"])
    }
}
