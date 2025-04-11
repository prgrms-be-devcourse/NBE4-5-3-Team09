//package com.coing.domain.chat.controller
//
//import com.coing.CoingApplication
//import com.coing.domain.chat.entity.ChatRoom
//import com.coing.domain.chat.repository.ChatRoomRepository
//import com.coing.domain.chat.service.ChatService
//import com.coing.domain.coin.market.entity.Market
//import com.coing.domain.coin.market.repository.MarketRepository
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.MediaType
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import java.util.UUID
//
//@SpringBootTest(classes = [CoingApplication::class])
//@AutoConfigureMockMvc
//@ActiveProfiles("test") // 테스트 프로파일 사용
//class ChatControllerIntegrationTest @Autowired constructor(
//    val mockMvc: MockMvc,
//    val objectMapper: ObjectMapper,
//    val chatRoomRepository: ChatRoomRepository,
//    val marketRepository: MarketRepository,
//    val chatService: ChatService
//) {
//
//    lateinit var testChatRoom: ChatRoom
//
//    @BeforeEach
//    fun `테스트용 채팅방 및 메시지 초기화`() {
//        // 테스트용 Market 엔티티 생성 및 영속화
//        val market = Market(code = "KRW-BTC", koreanName = "비트코인", englishName = "Bitcoin")
//        val savedMarket = marketRepository.save(market)
//        // 영속화된 Market을 이용하여 채팅방 생성
//        testChatRoom = ChatRoom(market = savedMarket, name = "테스트 채팅방")
//        testChatRoom = chatRoomRepository.save(testChatRoom)
//
//        // 테스트용 송신자 (User) 생성
//        val sender = com.coing.domain.user.entity.User(
//            id = UUID.randomUUID(),
//            name = "test",
//            email = "test@example.com",
//            password = "encoded",
//            provider = com.coing.domain.user.entity.Provider.EMAIL,
//            verified = true
//        )
//        // 채팅 메시지 전송 (메시지는 캐시에 저장됨)
//        chatService.sendMessage(testChatRoom.id!!, sender, "Hello")
//        chatService.sendMessage(testChatRoom.id!!, sender, "How are you?")
//
//    }
//
//    @Test
//    @WithMockUser(username = "testuser", roles = ["USER"])
//    fun `채팅방의 메시지 목록 조회 - 캐시 메시지 조회`() {
//        // '/api/chat/rooms/{marketCode}/messages' 엔드포인트 호출
//        mockMvc.perform(
//            get("/api/chat/rooms/KRW-BTC/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$[0].sender").value("test"))
//            .andExpect(jsonPath("$[0].content").value("Hello"))
//            .andExpect(jsonPath("$[1].content").value("How are you?"))
//    }
//}
