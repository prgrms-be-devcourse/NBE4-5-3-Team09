package com.coing.util

import com.coing.global.exception.BusinessException
import com.coing.global.exception.doc.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class BasicResponse(
    val status: HttpStatus,
    val message: String,
    val detail: String
) {
    companion object {
        fun of(exception: BusinessException): BasicResponse {
            return BasicResponse(
                exception.status,
                exception.messageCode,
                exception.detail
            )
        }

        fun of(message: String): BasicResponse {
            return BasicResponse(
                HttpStatus.OK,
                message,
                ""
            )
        }

        fun to(exception: BusinessException): ResponseEntity<BasicResponse> {
            return ResponseEntity
                .status(exception.status)
                .body(of(exception))
        }

        fun from(errorCode: ErrorCode, messageUtil: MessageUtil): BasicResponse {
            return BasicResponse(
                errorCode.status,
                messageUtil.resolveMessage(errorCode.messageKey),
                ""
            )
        }
    }
}
