package io.github.susimsek.springbootreactiveexample.repository

import io.github.susimsek.springbootreactiveexample.entity.Todo
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository :
    CoroutineCrudRepository<Todo, Long>,
    CoroutineSortingRepository<Todo, Long> {
    fun findByCompleted(completed: Boolean): Flow<Todo>
    fun findAllBy(pageable: Pageable): Flow<Todo>
}
