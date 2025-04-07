package com.coing.util

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
object PageUtil {
	fun <T> paginate(list: List<T>, pageable: Pageable): Page<T> {
		val start = pageable.offset.toInt()
		val end = (start + pageable.pageSize).coerceAtMost(list.size)
		return PageImpl(list.subList(start, end), pageable, list.size.toLong())
	}
}
