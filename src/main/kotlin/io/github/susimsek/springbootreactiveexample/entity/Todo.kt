package io.github.susimsek.springbootreactiveexample.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("todo")
data class Todo(
    @Id
    @Column("id")
    var id: Long? = null,

    @Column("title")
    var title: String? = null,

    @Column("completed")
    var completed: Boolean? = false
) : BaseEntity()
