package io.github.susimsek.springbootreactiveexample.repository

import io.github.susimsek.springbootreactiveexample.entity.Todo
import io.github.susimsek.springbootreactiveexample.repository.custom.CustomTodoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository :
    CoroutineCrudRepository<Todo, Long>,
    CoroutineSortingRepository<Todo, Long>,
    CustomTodoRepository
