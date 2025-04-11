package com.coing.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class UtilTest {

	@Test
	@DisplayName("PageUtil - 빈 리스트 페이징")
	fun paginateEmptyList() {
		// given
		val list: List<String> = listOf()
		val pageable: Pageable = PageRequest.of(0, 5)

		// when
		val page: Page<String> = PageUtil.paginate(list, pageable)

		// then
		assertThat(page.content).isEmpty()
		assertThat(page.totalElements).isEqualTo(0)
	}

	@Test
	@DisplayName("PageUtil - 전체 리스트가 한 페이지에 들어가는 경우")
	fun paginateFullList() {
		// given
		val list: List<Int> = listOf(1, 2, 3, 4)
		val pageable: Pageable = PageRequest.of(0, 10)

		// when
		val page: Page<Int> = PageUtil.paginate(list, pageable)

		// then
		assertThat(page.content).containsExactly(1, 2, 3, 4)
		assertThat(page.totalElements).isEqualTo(4)
	}

	@Test
	@DisplayName("PageUtil - 리스트의 일부만 포함된 페이지 반환")
	fun paginatePartialPage() {
		// given
		val list: List<String> = listOf("A", "B", "C", "D", "E", "F")
		val pageable: Pageable = PageRequest.of(1, 2)

		// when
		val page: Page<String> = PageUtil.paginate(list, pageable)

		// then
		assertThat(page.content).containsExactly("C", "D")
		assertThat(page.totalElements).isEqualTo(6)
	}
}
