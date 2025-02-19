package io.github.susimsek.springbootreactiveexample.config.apidoc

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail

/**
 * Configuration class for customizing the OpenAPI documentation.
 *
 * This class configures the OpenAPI documentation for the application,
 * including general API information and custom error responses.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    /**
     * Creates a custom [OpenAPI] bean with API metadata.
     *
     * This method defines the title, description, version, contact information,
     * and license details for the API documentation.
     *
     * @return a customized [OpenAPI] object with metadata.
     */
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(Components())
            .info(
                Info()
                    .title("Spring Boot Reactive Example REST API")
                    .description("Spring Boot Reactive Example REST API Documentation")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .name("Şuayb Şimşek")
                            .url("https://github.com/susimsek")
                            .email("suaybsimsek58@gmail.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("http://springdoc.org")
                    )
            )
    }

    /**
     * Customizes the OpenAPI documentation to include standardized error responses.
     *
     * This method adds error response definitions (e.g., 500 Internal Server Error)
     * to all operations in the OpenAPI documentation.
     *
     * @return an [OpenApiCustomizer] that applies error response customization.
     */
    @Bean
    fun errorResponsesCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            val components = openApi.components
            openApi.paths.values.forEach { pathItem ->
                pathItem.readOperations().forEach { operation ->
                    addErrorToApi(operation, components)
                }
            }
        }
    }

    /**
     * Adds a standardized error response to an API operation.
     *
     * This method defines a 500 Internal Server Error response with a schema based on
     * [ProblemDetail] and adds it to the given operation.
     *
     * @param operation  the [Operation] to which the error response is added.
     * @param components the [Components] used to resolve schemas.
     */
    private fun addErrorToApi(operation: Operation, components: Components) {
        val mediaType = io.swagger.v3.oas.models.media.MediaType()
            .schema(AnnotationsUtils.resolveSchemaFromType(ProblemDetail::class.java, components, null))

        // 500 Internal Server Error
        operation.responses.addApiResponse(
            "500",
            ApiResponse()
                .description("Internal Server Error")
                .content(Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, mediaType))
        )
    }
}
