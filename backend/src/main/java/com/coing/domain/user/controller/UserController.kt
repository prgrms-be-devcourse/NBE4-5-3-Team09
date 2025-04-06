package com.coing.domain.user.controller

import com.coing.domain.user.controller.dto.*
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.domain.user.service.AuthTokenService
import com.coing.domain.user.service.UserService
import com.coing.domain.user.email.service.EmailVerificationService
import com.coing.domain.user.email.service.PasswordResetService
import com.coing.global.exception.BusinessException
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import com.coing.util.BasicResponse
import com.coing.util.MessageUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User API", description = "회원 관련 API 엔드포인트")
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
    private val emailVerificationService: EmailVerificationService,
    private val messageUtil: MessageUtil,
    private val passwordResetService: PasswordResetService
) {

    @Operation(summary = "일반 유저 회원 가입")
    @PostMapping("/signup")
    @ApiErrorCodeExamples(ErrorCode.MAIL_SEND_FAIL, ErrorCode.ALREADY_REGISTERED_EMAIL, ErrorCode.INVALID_PASSWORD_CONFIRM)
    fun signUp(
        @RequestBody @Validated request: UserSignUpRequest,
        response: HttpServletResponse
    ): ResponseEntity<UserSignupResponse> {
        val user: UserResponse = userService.join(request)
        val signupResponse = UserSignupResponse(
            message = "회원가입 성공. 인증 이메일 전송 완료.",
            name = user.name,
            email = user.email,
            userId = user.id
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse)
    }

    @Operation(summary = "이메일 인증")
    @GetMapping("/verify-email")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun verifyEmail(@RequestParam(name = "token") token: String): ResponseEntity<*> {
        val userId: UUID? = authTokenService.parseId(token)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("status" to "error", "message" to "유효하지 않은 토큰입니다."))
        }

        val user: User = userRepository.findById(userId).orElseThrow {
            BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "")
        }
        if (user.verified) {
            return ResponseEntity.status(HttpStatus.OK)
                .body(mapOf("status" to "already", "message" to "이미 인증되었습니다."))
        }
        emailVerificationService.verifyEmail(userId)
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "이메일 인증이 완료되었습니다."))
    }

    @Operation(
        summary = "이메일 인증 상태 확인",
        description = "회원가입을 요청한 사용자의 UUID(userId)를 기준으로 이메일 인증 여부를 확인합니다."
    )
    @GetMapping("/is-verified")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun isVerified(@RequestParam(name = "userId") userId: UUID): ResponseEntity<EmailVerificationResponse> {
        val userOpt = userRepository.findById(userId)
        if (userOpt.isEmpty) {
            throw BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "")
        }
        val response = EmailVerificationResponse(userOpt.get().verified)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "이메일 인증 메일 재전송")
    @PostMapping("/resend-email")
    @ApiErrorCodeExamples(ErrorCode.MAIL_SEND_FAIL, ErrorCode.MEMBER_NOT_FOUND)
    fun resendEmail(@RequestParam(name = "userId") userId: UUID): ResponseEntity<*> {
        val user: User = userRepository.findById(userId).orElseThrow {
            BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "")
        }
        if (user.verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("status" to "already_verified", "message" to "이미 인증된 사용자입니다."))
        }
        return try {
            emailVerificationService.resendVerificationEmail(userId)
            ResponseEntity.ok(mapOf("status" to "success", "message" to "이메일이 재전송되었습니다."))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("status" to "error", "message" to "이메일 재전송에 실패했습니다."))
        }
    }

    @Operation(summary = "일반 유저 로그인")
    @PostMapping("/login")
    @ApiErrorCodeExamples(ErrorCode.EMAIL_NOT_VERIFIED, ErrorCode.MEMBER_NOT_FOUND, ErrorCode.PASSWORD_MISMATCH)
    fun login(
        @RequestBody @Validated request: UserLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<BasicResponse> {
        val user: UserResponse = userService.login(request.email, request.password)
        if (!user.verified) {
            throw BusinessException("이메일 인증이 완료되지 않았습니다.", HttpStatus.UNAUTHORIZED, "")
        }
        issuedToken(response, user)
        val basicResponse = BasicResponse(HttpStatus.OK, "로그인 성공", "")
        return ResponseEntity.ok(basicResponse)
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND, ErrorCode.INVALID_REFRESH_TOKEN, ErrorCode.REFRESH_TOKEN_REQUIRED)
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BasicResponse> {
        val cookies: Array<Cookie>? = request.cookies
        if (cookies.isNullOrEmpty()) {
            throw BusinessException(messageUtil.resolveMessage("refresh.token.required"), HttpStatus.FORBIDDEN, "")
        }
        var refreshToken: String? = null
        for (cookie in cookies) {
            if (cookie.name == "refreshToken") {
                refreshToken = cookie.value
                break
            }
        }
        if (refreshToken.isNullOrBlank()) {
            throw BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.FORBIDDEN, "")
        }
        val claims = authTokenService.verifyToken(refreshToken)
        if (claims == null || claims["id"] == null) {
            throw BusinessException(messageUtil.resolveMessage("invalid.refresh.token"), HttpStatus.FORBIDDEN, "")
        }
        val id = UUID.fromString(claims["id"].toString())
        val user: UserResponse = userService.findById(id)
            ?: throw BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.BAD_REQUEST, "")
        issuedToken(response, user)
        return ResponseEntity.ok(BasicResponse(HttpStatus.OK, "토큰 재발급 성공", ""))
    }

    @Operation(summary = "회원 로그아웃")
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<BasicResponse> {
        val expiredCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0) // 쿠키 삭제
            .sameSite("None")
            .build()
        response.setHeader("Set-Cookie", expiredCookie.toString())
        return ResponseEntity.ok(BasicResponse(HttpStatus.OK, "로그아웃 성공", "로그아웃 처리 완료"))
    }

    @Operation(summary = "회원 탈퇴", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/signout")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND, ErrorCode.EMPTY_TOKEN_PROVIDED, ErrorCode.PASSWORD_MISMATCH)
    fun signOut(
        @RequestBody @Validated request: SignOutRequest,
        @AuthenticationPrincipal principal: CustomUserPrincipal?
    ): ResponseEntity<*> {
        if (principal == null) {
            throw BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.FORBIDDEN, "")
        }
        userService.quit(principal.id, request.password)
        return ResponseEntity.ok("회원 탈퇴 성공")
    }

    @Operation(summary = "회원 정보 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @GetMapping("/info")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND, ErrorCode.EMPTY_TOKEN_PROVIDED)
    fun getUserInfo(@AuthenticationPrincipal principal: CustomUserPrincipal?): ResponseEntity<Any> {
        if (principal == null) {
            throw BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.FORBIDDEN, "")
        }
        val user: UserResponse = userService.findById(principal.id)
        return ResponseEntity.ok(user)
    }

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/password-reset/request")
    @ApiErrorCodeExamples(ErrorCode.MAIL_SEND_FAIL)
    fun requestPasswordReset(@RequestBody @Validated request: PasswordResetRequest): ResponseEntity<*> {
        val userOptional = userRepository.findByEmail(request.email)
        if (userOptional.isEmpty) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("status" to "error", "message" to "사용자를 찾을 수 없습니다."))
        }
        val user = userOptional.get()
        passwordResetService.sendPasswordResetEmail(user)
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "비밀번호 재설정 이메일 전송되었습니다."))
    }

    @Operation(summary = "비밀번호 재설정 확인")
    @PostMapping("/password-reset/confirm")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun confirmPasswordReset(
        @RequestParam("token") token: String,
        @RequestBody @Validated request: PasswordResetConfirmRequest
    ): ResponseEntity<*> {
        val userId = authTokenService.parseId(token)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("status" to "error", "message" to "유효하지 않은 토큰입니다."))
        }
        if (request.newPassword != request.newPasswordConfirm) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("status" to "error", "message" to "비밀번호 확인이 일치하지 않습니다."))
        }
        userService.updatePassword(userId, request.newPassword)
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "비밀번호가 재설정되었습니다."))
    }

    @Operation(summary = "리다이렉트")
    @GetMapping
    fun redirectSocialLogin() {
        // 별도 동작 없음
    }

    private fun issuedToken(response: HttpServletResponse, user: UserResponse) {
        val accessToken = authTokenService.genAccessToken(user)
        val refreshToken = authTokenService.genRefreshToken(user)

        val refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(604800) // 7일
            .sameSite("None")
            .build()

        response.setHeader("Set-Cookie", refreshCookie.toString())
        response.setHeader("Authorization", "Bearer $accessToken")
    }
}
