package com.coing.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.domain.user.controller.dto.UserSignUpRequest;
import com.coing.domain.user.email.service.EmailVerificationService;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private MessageUtil messageUtil;

	@Mock
	private EmailVerificationService emailVerificationService;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// 메시지 해석 시, 입력받은 메시지 코드를 그대로 반환하도록 설정
		when(messageUtil.resolveMessage(any(String.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	@DisplayName("t1: 일반 회원 가입 - 정상 동작 테스트")
	void t1() {
		String name = "테스트";
		String email = "test@test.com";
		String password = "testtest1!";
		String passwordConfirm = "testtest1!";

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

		User savedUser = User.builder()
			.name(name)
			.email(email)
			.password("encodedPassword")
			.build();
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// DTO record를 생성해서 전달합니다.
		UserSignUpRequest request = new UserSignUpRequest(name, email, password, passwordConfirm);
		UserResponse result = userService.join(request);

		assertNotNull(result);
		assertEquals(email, result.email());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("t2: 일반 회원 가입 - 비밀번호 불일치 테스트")
	void t2() {
		String name = "테스트";
		String email = "test@test.com";
		String password = "testtest!1";
		String passwordConfirm = "testtest!2";

		UserSignUpRequest request = new UserSignUpRequest(name, email, password, passwordConfirm);
		Exception exception = assertThrows(BusinessException.class, () -> {
			userService.join(request);
		});
		assertEquals("invalid.password.confirm", exception.getMessage());
	}

	@Test
	@DisplayName("t3: 일반 회원 가입 - 중복 이메일 테스트")
	void t3() {
		String name = "테스트";
		String email = "test@test.com";
		String password = "testtest1!";
		String passwordConfirm = "testtest1!";

		UserSignUpRequest request = new UserSignUpRequest(name, email, password, passwordConfirm);

		User existingUser = new User();
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

		Exception exception = assertThrows(BusinessException.class, () -> {
			userService.join(request);
		});
		assertEquals("already.registered.email", exception.getMessage());
	}

	@Test
	@DisplayName("t4: 일반 회원 로그인 - 정상 동작 테스트")
	void t4() {
		String email = "test@test.com";
		String password = "test";
		String encodedPassword = "encodedPassword";
		UUID userId = UUID.randomUUID(); // ID 생성

		User user = User.builder()
			.id(userId) // 생성된 ID 할당
			.name("테스트")
			.email(email)
			.password(encodedPassword)
			.verified(true) // 인증 완료 상태
			.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

		UserResponse result = userService.login(email, password);

		assertNotNull(result);
		assertEquals(email, result.email());
		assertTrue(result.verified());
		// 필요하면 userId도 확인할 수 있습니다.
		assertEquals(userId, result.id());
	}

	@Test
	@DisplayName("t5: 일반 회원 로그인 - 잘못된 비밀번호 테스트")
	void t5() {
		String email = "test@test.com";
		String password = "test";
		String encodedPassword = "encodedPassword";

		User user = User.builder()
			.name("테스트")
			.email(email)
			.password(encodedPassword)
			.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

		Exception exception = assertThrows(BusinessException.class, () -> {
			userService.login(email, password);
		});
		assertEquals("password.mismatch", exception.getMessage());
	}

	@Test
	@DisplayName("t6: 일반 회원 로그인 - 사용자 미존재 테스트")
	void t6() {
		String email = "test@test.com";
		String password = "test";

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		Exception exception = assertThrows(BusinessException.class, () -> {
			userService.login(email, password);
		});
		assertEquals("member.not.found", exception.getMessage());
	}
}
