package com.coing.domain.user.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자 권한으로 신고 메시지 조회 - OK 반환`() {
        mockMvc.perform(
            get("/api/admin/reported-messages")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            // 결과가 JSON 배열임을 확인 (신고 내역 데이터가 없더라도 빈 배열이 반환됩니다)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `일반 사용자 권한으로 관리자 엔드포인트 접근 - Forbidden 반환`() {
        mockMvc.perform(
            get("/api/admin/reported-messages")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }


    // 관리자 권한 확인 엔드포인트 테스트 (관리자이면 OK 반환)
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자 권한 확인 엔드포인트 - 관리자이면 OK 반환`() {
        mockMvc.perform(
            get("/api/admin/auth-check")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("관리자 권한이 확인되었습니다."))
    }

    // 관리자 권한 확인 엔드포인트 테스트 (일반 사용자이면 Forbidden 반환)
    @Test
    @WithMockUser(roles = ["USER"])
    fun `관리자 권한 확인 엔드포인트 - 일반 사용자이면 Forbidden 반환`() {
        mockMvc.perform(
            get("/api/admin/auth-check")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }
}
