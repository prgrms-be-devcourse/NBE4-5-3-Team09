package com.coing.util

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	open var createdAt: LocalDateTime? = null
		protected set

	@LastModifiedDate
	@Column(name = "modified_at")
	open var modifiedAt: LocalDateTime? = null
		protected set
}
