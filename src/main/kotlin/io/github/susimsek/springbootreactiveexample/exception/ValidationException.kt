package io.github.susimsek.springbootreactiveexample.exception

/**
 * Exception to be thrown when a validation error occurs.
 *
 * @param message the validation error message.
 */
class ValidationException(
    message: String,
    private val args: Array<out Any> = emptyArray()
) : RuntimeException(message) {

    fun getArgs(): Array<out Any> = args
}
