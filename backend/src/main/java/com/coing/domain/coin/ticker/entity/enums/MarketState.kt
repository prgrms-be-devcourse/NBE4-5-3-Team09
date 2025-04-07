package com.coing.domain.coin.ticker.entity.enums

enum class MarketState {
    PREVIEW,  // 입금지원
    ACTIVE,  // 거래지원 가능
    DELISTED,  // 거래지원 종료
    PREDELISTING // 재상장 중
}
