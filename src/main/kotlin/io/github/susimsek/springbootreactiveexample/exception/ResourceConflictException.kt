package io.github.susimsek.springbootreactiveexample.exception

/**
 * Exception to be thrown when a resource conflict occurs.
 *
 * @param resourceName the name of the resource.
 * @param searchCriteria the search criteria involved in the conflict.
 * @param searchValue the value causing the conflict.
 */
class ResourceConflictException(
    private val resourceName: String,
    private val searchCriteria: String,
    private val searchValue: Any
) : RuntimeException("The $resourceName already exists with $searchCriteria: $searchValue") {

    fun getArgs(): Array<Any> = arrayOf(resourceName, searchCriteria, searchValue)
}
