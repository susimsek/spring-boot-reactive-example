package io.github.susimsek.springbootreactiveexample.controller

import io.github.susimsek.springbootreactiveexample.dto.CreateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.PartialUpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.TodoDTO
import io.github.susimsek.springbootreactiveexample.dto.UpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.enums.SortDirection
import io.github.susimsek.springbootreactiveexample.enums.TodoSortField
import io.github.susimsek.springbootreactiveexample.service.TodoService
import io.github.susimsek.springbootreactiveexample.validation.EnumConstraint
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller for managing Todo items.
 */
@RestController
@RequestMapping("/api/v1/todos")
@Tag(name = "todos", description = "Endpoints for managing todo items")
@Validated
class TodoController(private val todoService: TodoService) {

    /**
     * GET /todos : Fetch all todos with optional sorting and pagination.
     */
    @Operation(
        summary = "Get all Todos",
        description = "Retrieves a paginated list of todos"
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTodos(
        @Parameter(description = "Page number (0-based index)", example = "0")
        @RequestParam(required = false, defaultValue = "0")
        @Min(0)
        page: Int,

        @Parameter(description = "Number of items per page", example = "10")
        @RequestParam(required = false, defaultValue = "10")
        @Min(1)
        @Max(100)
        size: Int,

        @Parameter(description = "Sorting field", example = "CREATED_AT")
        @Schema(implementation = TodoSortField::class)
        @RequestParam(required = false, defaultValue = "CREATED_AT")
        @EnumConstraint(enumClass = TodoSortField::class)
        sortField: String,

        @Parameter(description = "Sorting direction", example = "DESC")
        @Schema(implementation = SortDirection::class)
        @RequestParam(required = false, defaultValue = "DESC")
        @EnumConstraint(enumClass = SortDirection::class)
        sortDirection: String
    ): Flow<TodoDTO> {
        val sort = Sort.by(Sort.Direction.valueOf(sortDirection), sortField)
        val pageable: Pageable = PageRequest.of(page, size, sort)
        return todoService.getTodos(pageable)
    }

    /**
     * GET /todos/{id} : Fetch a specific todo item by ID.
     */
    @Operation(summary = "Get Todo by ID", description = "Retrieves a specific todo item by its ID.")
    @ApiResponse(
        responseCode = "200",
        description = "Successful operation",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TodoDTO::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "404",
        description = "Todo not found",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getTodoById(
        @Parameter(description = "ID of the todo item", example = "1", required = true)
        @PathVariable id: Long
    ): ResponseEntity<TodoDTO> {
        return ResponseEntity.ok(todoService.getTodoById(id))
    }

    /**
     * POST /todos : Create a new todo item.
     */
    @Operation(summary = "Create a new Todo", description = "Creates a new todo item and returns the created object.")
    @ApiResponse(
        responseCode = "201",
        description = "Todo created successfully",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TodoDTO::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun createTodo(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New Todo item to create",
            required = true
        )
        @Valid
        @RequestBody dto: CreateTodoDTO
    ): ResponseEntity<TodoDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.createTodo(dto))
    }

    /**
     * PUT /todos/{id} : Update an existing todo item.
     */
    @Operation(
        summary = "Update an existing Todo",
        description = "Fully updates a Todo item and requires all fields."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Todo updated successfully",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TodoDTO::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "404",
        description = "Todo not found",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @PutMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updateTodo(
        @Parameter(description = "ID of the todo item to update", example = "1", required = true)
        @PathVariable id: Long,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated Todo item",
            required = true
        )
        @Valid
        @RequestBody dto: UpdateTodoDTO
    ): ResponseEntity<TodoDTO> {
        return ResponseEntity.ok(todoService.updateTodo(id, dto))
    }

    /**
     * PATCH /todos/{id} : Partially update an existing todo item.
     */
    @Operation(
        summary = "Partially update a Todo",
        description = "Updates only the provided fields of a Todo item."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Todo partially updated successfully",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TodoDTO::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @ApiResponse(
        responseCode = "404",
        description = "Todo not found",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @PatchMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun partialUpdateTodo(
        @Parameter(description = "ID of the todo item to update", example = "1", required = true)
        @PathVariable id: Long,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Fields to be updated in the Todo item",
            required = false
        )
        @Valid
        @RequestBody dto: PartialUpdateTodoDTO
    ): ResponseEntity<TodoDTO> {
        return ResponseEntity.ok(todoService.partialUpdateTodo(id, dto))
    }

    /**
     * DELETE /todos/{id} : Delete a specific todo item by ID.
     */
    @Operation(summary = "Delete a Todo", description = "Deletes a specific todo item by its ID.")
    @ApiResponse(responseCode = "204", description = "Todo deleted successfully")
    @ApiResponse(
        responseCode = "404",
        description = "Todo not found",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = Schema(implementation = ProblemDetail::class)
            )
        ]
    )
    @DeleteMapping("/{id}")
    suspend fun deleteTodo(
        @Parameter(description = "ID of the todo item to delete", example = "1", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        todoService.deleteTodoById(id)
        return ResponseEntity.noContent().build()
    }
}
