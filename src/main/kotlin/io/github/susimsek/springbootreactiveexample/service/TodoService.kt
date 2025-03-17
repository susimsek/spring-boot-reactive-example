package io.github.susimsek.springbootreactiveexample.service

import io.github.susimsek.springbootreactiveexample.dto.CreateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.PartialUpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.TodoDTO
import io.github.susimsek.springbootreactiveexample.dto.UpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.exception.ResourceNotFoundException
import io.github.susimsek.springbootreactiveexample.mapper.TodoMapper
import io.github.susimsek.springbootreactiveexample.repository.TodoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val todoMapper: TodoMapper
) {

    @Transactional(readOnly = true)
    suspend fun getTodoById(id: Long): TodoDTO {
        val todo = todoRepository.findById(id)
            ?: throw ResourceNotFoundException("Todo", "id", id)
        return todoMapper.toDto(todo)
    }

    @Transactional(readOnly = true)
    suspend fun getTodos(pageable: Pageable): Page<TodoDTO> {
        val todosList = todoRepository.findAllBy(pageable)
            .map { todoMapper.toDto(it) }
            .toList()
        val totalCount = todoRepository.count()
        return PageImpl(todosList, pageable, totalCount)
    }

    @Transactional
    suspend fun createTodo(dto: CreateTodoDTO): TodoDTO {
        val todo = todoMapper.toEntity(dto)
        return todoMapper.toDto(todoRepository.save(todo))
    }

    @Transactional
    suspend fun updateTodo(id: Long, dto: UpdateTodoDTO): TodoDTO {
        val todo = todoRepository.findById(id)
            ?: throw ResourceNotFoundException("Todo", "id", id)
        todoMapper.updateEntityFromDto(dto, todo)
        return todoMapper.toDto(todoRepository.save(todo))
    }

    @Transactional
    suspend fun partialUpdateTodo(id: Long, dto: PartialUpdateTodoDTO): TodoDTO {
        val todo = todoRepository.findById(id)
            ?: throw ResourceNotFoundException("Todo", "id", id)
        todoMapper.partialUpdate(dto, todo)
        return todoMapper.toDto(todoRepository.save(todo))
    }

    @Transactional
    suspend fun deleteTodoById(id: Long) {
        val todo = todoRepository.findById(id)
            ?: throw ResourceNotFoundException("Todo", "id", id)
        todoRepository.delete(todo)
    }
}
