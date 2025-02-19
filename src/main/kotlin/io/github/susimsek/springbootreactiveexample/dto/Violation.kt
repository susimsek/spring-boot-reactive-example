package io.github.susimsek.springbootreactiveexample.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.susimsek.springbootreactiveexample.utils.StringUtils.toSnakeCase
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.ConstraintViolation
import jakarta.validation.Path
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import java.io.Serializable

/**
 * Represents a validation violation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Violation(
    @Schema(description = "Code.", example = "not_blank")
    @JsonProperty("code")
    val code: String?,

    @Schema(description = "Object name", example = "chatRequest")
    @JsonProperty("object")
    val objectName: String?,

    @Schema(description = "Field name", example = "prompt")
    @JsonProperty
    val field: String?,

    @Schema(description = "Violation rejected value", example = "a")
    @JsonProperty
    val rejectedValue: Any?,

    @Schema(description = "Error Message", example = "size must be between 4 and 50")
    @JsonProperty
    val message: String?
) : Serializable {

    /**
     * Constructs a simple [Violation] with only the object name and message.
     */
    constructor(objectName: String, message: String) : this(
        code = null,
        objectName = objectName,
        field = null,
        rejectedValue = null,
        message = message
    )

    /**
     * Constructs a [Violation] from a [FieldError] instance.
     */
    constructor(error: FieldError) : this(
        code = error.code?.toSnakeCase(),
        objectName = error.objectName.replaceFirst("DTO$", ""),
        field = error.field,
        rejectedValue = error.rejectedValue,
        message = error.defaultMessage
    )

    /**
     * Constructs a [Violation] from an [ObjectError] instance.
     */
    constructor(error: ObjectError) : this(
        code = error.code?.toSnakeCase(),
        objectName = null,
        field = error.objectName.replaceFirst("DTO$", ""),
        rejectedValue = null,
        message = error.defaultMessage
    )

    /**
     * Constructs a [Violation] from a [ConstraintViolation] instance.
     */
    constructor(violation: ConstraintViolation<*>) : this(
        code = violation.constraintDescriptor.annotation.annotationClass.simpleName?.toSnakeCase(),
        objectName = null,
        field = io.github.susimsek.springbootreactiveexample.dto.Violation.Companion.getField(violation.propertyPath),
        rejectedValue = violation.invalidValue,
        message = violation.message
    )

    companion object {
        /**
         * Extracts the field name from the provided property path.
         */
        private fun getField(propertyPath: Path): String? {
            var fieldName: String? = null
            for (node in propertyPath) {
                fieldName = node.name
            }
            return fieldName
        }
    }
}
