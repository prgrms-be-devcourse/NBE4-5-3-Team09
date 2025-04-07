package com.coing.domain.user.service

import com.coing.domain.user.controller.dto.UserResponse
import com.coing.domain.user.controller.dto.UserSignUpRequest
import com.coing.domain.user.email.service.EmailVerificationService
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailVerificationService: EmailVerificationService,
    private val messageUtil: MessageUtil
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun join(request: UserSignUpRequest): UserResponse {
        log.info("회원가입 시도: ${request.email}")

        // 비밀번호와 비밀번호 확인 일치 여부 검사
        if (request.password != request.passwordConfirm) {
            throw BusinessException(
                messageUtil.resolveMessage("invalid.password.confirm"),
                HttpStatus.BAD_REQUEST,
                ""
            )
        }

        if (userRepository.findByEmail(request.email).isPresent) {
            throw BusinessException(
                messageUtil.resolveMessage("already.registered.email"),
                HttpStatus.BAD_REQUEST,
                ""
            )
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        // 빌더 대신 생성자 호출 (User 엔티티의 기본 생성자/기본값 활용)
        val userEntity = User(
            name = request.name,
            email = request.email,
            password = encodedPassword,
            provider = Provider.EMAIL
            // verified 기본값은 false로 처리한다고 가정
        )

        val savedUser = userRepository.save(userEntity)

        // 이메일 인증 메일 전송 실패 시 예외 전파 (전체 롤백)
        emailVerificationService.sendVerificationEmail(savedUser)

        return UserResponse.from(savedUser)
    }

    @Transactional(readOnly = true)
    fun login(email: String, password: String): UserResponse {
        log.info("로그인 시도: $email")
        val optionalUser = userRepository.findByEmail(email)
        val user = validateUser(password, optionalUser)

        // 이메일 인증 여부 확인
        if (!user.verified) {
            throw BusinessException(
                messageUtil.resolveMessage("email.not.verified"),
                HttpStatus.UNAUTHORIZED,
                user.id.toString()
            )
        }
        return UserResponse.from(user)
    }

    @Transactional
    fun quit(id: UUID, password: String) {
        val optionalUser = userRepository.findById(id)
        val user = validateUser(password, optionalUser)
        userRepository.delete(user)
        log.info("회원 탈퇴 성공: $id")
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): UserResponse {
        return userRepository.findById(id)
            .map { UserResponse.from(it) }
            .orElseThrow {
                BusinessException(
                    messageUtil.resolveMessage("member.not.found"),
                    HttpStatus.BAD_REQUEST,
                    ""
                )
            }
    }

    // 비밀번호 재설정 메서드
    @Transactional
    fun updatePassword(userId: UUID, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow {
                BusinessException(
                    messageUtil.resolveMessage("member.not.found"),
                    HttpStatus.BAD_REQUEST,
                    ""
                )
            }
        val encodedPassword = passwordEncoder.encode(newPassword)
        // 불변 객체 업데이트: withPassword() 혹은 copy() 사용
        val updatedUser = user.copy(password = encodedPassword)
        userRepository.save(updatedUser)
        log.info("비밀번호 재설정 성공: $userId")
    }

    // 매일 새벽 5시에 실행되는 미인증 사용자 삭제 스케줄러 (cron: 초, 분, 시, 일, 월, 요일)
    @Scheduled(cron = "00 00 5 * * *")
    @Transactional
    fun cleanupUnverifiedUsers() {
        // 현재 시각에서 1주일 전보다 오래된 미인증 사용자 삭제
        val threshold = LocalDateTime.now().minusWeeks(1)
        val deletedCount = userRepository.deleteUnverifiedUsers(threshold)
        if (deletedCount > 0) {
            log.info("삭제된 인증 미완료 사용자 수: $deletedCount")
        }
    }

    private fun validateUser(password: String, optionalUser: Optional<User>): User {
        if (optionalUser.isEmpty) {
            throw BusinessException(
                messageUtil.resolveMessage("member.not.found"),
                HttpStatus.BAD_REQUEST,
                ""
            )
        }
        val user = optionalUser.get()
        if (!passwordEncoder.matches(password, user.password)) {
            throw BusinessException(
                messageUtil.resolveMessage("password.mismatch"),
                HttpStatus.BAD_REQUEST,
                ""
            )
        }
        return user
    }
}
