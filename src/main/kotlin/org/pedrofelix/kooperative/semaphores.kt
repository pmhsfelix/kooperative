package org.pedrofelix.kooperative

import kotlin.coroutines.Continuation

/**
 * Just a simple unary semaphore, without timeouts or cancellation.
 */
class SimpleSemaphore(private var units: Int) {

    // Holds the list of tasks waiting for semaphore units.
    private val waitList = mutableListOf<Continuation<Unit>>()

    /**
     * Acquires a semaphore unit, suspending if units are not available.
     */
    suspend fun acquire() = if (units > 0) {
        units -= 1
    } else {
        park(waitList)
    }

    /**
     * Releases a semaphore unit, eventually resuming a suspended task awaiting for units.
     */
    fun release() {
        if (waitList.size > 0) {
            unpark(waitList)
        } else {
            units += 1
        }
    }
}
