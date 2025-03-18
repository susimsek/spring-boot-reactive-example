package io.github.susimsek.springbootreactiveexample.repository.custom

import io.github.susimsek.springbootreactiveexample.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomTodoRepository {
    suspend fun findPagedTodos(pageable: Pageable): Page<Todo>
    suspend fun searchTodos(keyword: String, pageable: Pageable): Page<Todo>
}
