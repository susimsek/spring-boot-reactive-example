package io.github.susimsek.springbootreactiveexample.repository.custom

import io.github.susimsek.springbootreactiveexample.entity.Todo
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Repository
@Transactional(readOnly = true)
class CustomTodoRepositoryImpl(
    private val template: R2dbcEntityTemplate
) : CustomTodoRepository {

    override suspend fun findPagedTodos(pageable: Pageable): Page<Todo> {
        val query = Query.query(Criteria.empty()).with(pageable)
        return Mono.zip(
            template.select(query, Todo::class.java).collectList(),
            template.count(Query.query(Criteria.empty()), Todo::class.java)
        ).map { tuple ->
            PageImpl(tuple.t1, pageable, tuple.t2)
        }.awaitSingle()
    }

    override suspend fun searchTodos(keyword: String, pageable: Pageable): Page<Todo> {
        val criteria = Criteria.where("title")
            .like("%$keyword%").ignoreCase(true)
        val query = Query.query(criteria).with(pageable)
        return Mono.zip(
            template.select(query, Todo::class.java).collectList(),
            template.count(Query.query(criteria), Todo::class.java)
        ).map { tuple ->
            PageImpl(tuple.t1, pageable, tuple.t2)
        }.awaitSingle()
    }
}
