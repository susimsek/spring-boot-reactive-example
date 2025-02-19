package io.github.susimsek.springbootreactiveexample.config

import io.github.susimsek.springbootreactiveexample.dto.Violation
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeHint
import org.springframework.lang.Nullable

/**
 * Configuration class for setting up native runtime hints for Spring AOT (Ahead-of-Time) processing.
 *
 * This class contains the configuration for **native image generation** using **Spring Native**.
 * It registers runtime hints, which guide the native image compiler about what classes, methods, and
 * fields need to be included in the generated native image. This is crucial when building native images
 * using GraalVM or other similar tools.
 *
 * Example usage:
 * ```kotlin
 * @Configuration
 * class ApplicationConfig {
 *     @Bean
 *     fun nativeConfig() = NativeConfig()
 * }
 * ```
 */
class NativeConfig {

    /**
     * Registers native runtime hints for reflection and other native-related processing.
     *
     * This inner class implements the [RuntimeHintsRegistrar] interface to register runtime hints
     * for specific types, such as the `Violation` class, which may require reflection during the
     * runtime of the native image.
     *
     * In this case, it registers the `Violation` class for reflection and allows access to all
     * its members at runtime (fields, methods, etc.).
     *
     * @see [RuntimeHintsRegistrar] for more information on registering hints.
     */
    class AppNativeRuntimeHints : RuntimeHintsRegistrar {
        /**
         * Registers the runtime hints that provide guidance on reflection, serialization,
         * and other native processing requirements.
         *
         * In this example, it registers the `Violation` class to support reflection during native
         * image generation, ensuring that all members (fields, methods) are accessible at runtime.
         *
         * @param hints The runtime hints to be registered.
         * @param classLoader The class loader used to load the application classes.
         */
        override fun registerHints(hints: RuntimeHints, @Nullable classLoader: ClassLoader?) {
            hints.reflection().registerType(io.github.susimsek.springbootreactiveexample.dto.Violation::class.java) { builder: TypeHint.Builder ->
                builder.withMembers() // Register all members of the Violation class
            }
        }
    }
}
