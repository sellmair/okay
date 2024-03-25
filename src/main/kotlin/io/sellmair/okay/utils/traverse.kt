package io.sellmair.okay.utils

/**
 * General purpose implementation of a transitive closure
 * - Recursion free
 * - Predictable amount of allocations
 * - Handles loops and self references gracefully
 * @param edges: Producer function from one node to all its children. This implementation can handle loops and self references gracefully.
 * @return Note: The order of the set is guaranteed to be bfs
 */
inline fun <reified T> T.closure(edges: (T) -> Iterable<T>): Set<T> {
    val initialEdges = edges(this)

    val dequeue = if (initialEdges is Collection) {
        if (initialEdges.isEmpty()) return emptySet()
        ArrayDeque(initialEdges)
    } else createDequeueFromIterable(initialEdges)

    val results = createResultSet<T>(dequeue.size)

    while (dequeue.isNotEmpty()) {
        val element = dequeue.removeAt(0)
        if (element != this && results.add(element)) {
            dequeue.addAll(edges(element))
        }
    }
    return results
}


/**
 * Similar to [closure], but will also include the receiver(seed) of this function into the final set
 * @see closure
 */
inline fun <reified T> T.withClosure(edges: (T) -> Iterable<T>): Set<T> {
    val initialEdges = edges(this)

    val dequeue = if (initialEdges is Collection) {
        if (initialEdges.isEmpty()) return setOf(this)
        ArrayDeque(initialEdges)
    } else createDequeueFromIterable(initialEdges)

    val results = createResultSet<T>(dequeue.size)
    results.add(this)

    while (dequeue.isNotEmpty()) {
        val element = dequeue.removeAt(0)
        if (results.add(element)) {
            dequeue.addAll(edges(element))
        }
    }
    return results
}

@PublishedApi
internal fun <T> createResultSet(initialSize: Int = 16): MutableSet<T> {
    return LinkedHashSet(initialSize)
}


@PublishedApi
internal inline fun <reified T> createDequeueFromIterable(elements: Iterable<T>): MutableList<T> {
    return ArrayDeque<T>().apply {
        elements.forEach { element -> add(element) }
    }
}