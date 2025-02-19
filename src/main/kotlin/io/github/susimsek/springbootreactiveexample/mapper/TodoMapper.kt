package io.github.susimsek.springbootreactiveexample.mapper

import io.github.susimsek.springbootreactiveexample.dto.CreateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.PartialUpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.dto.TodoDTO
import io.github.susimsek.springbootreactiveexample.dto.UpdateTodoDTO
import io.github.susimsek.springbootreactiveexample.entity.Todo
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring")
interface TodoMapper {

    fun toDto(entity: Todo): TodoDTO

    fun toEntity(dto: CreateTodoDTO): Todo

    @Mapping(target = "id", ignore = true)
    fun updateEntityFromDto(dto: UpdateTodoDTO, @MappingTarget entity: Todo)

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun partialUpdate(dto: PartialUpdateTodoDTO, @MappingTarget entity: Todo)
}
