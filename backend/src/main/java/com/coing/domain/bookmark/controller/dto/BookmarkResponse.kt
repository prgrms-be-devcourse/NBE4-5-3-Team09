package com.coing.domain.bookmark.controller.dto

import com.coing.domain.bookmark.entity.Bookmark

data class BookmarkResponse(
    val code: String,
    val koreanName: String,
    val englishName: String,
    val isBookmarked: Boolean
) {
    companion object {
        fun of(bookmark: Bookmark): BookmarkResponse {
            return BookmarkResponse(
                code = bookmark.market.code,
                koreanName = bookmark.market.koreanName,
                englishName = bookmark.market.englishName,
                isBookmarked = true
            )
        }
    }
}
