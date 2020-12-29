package org.pedrofelix.kooperative

import org.slf4j.LoggerFactory
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume

private val log = LoggerFactory.getLogger("kooperative")

/*
 * Queue type and operations.
 */
typealias Queue<T> = MutableList<T>

fun <T> Queue<T>.enqueue(t: T) = this.add(t)
fun <T> Queue<T>.dequeue() = this.removeAt(0)

/*
 * A non-running task is represented by a continuation.
 * Resuming the task execution is done by calling that continuation.
 */
typealias TaskContinuation = Continuation<Unit>

/*
 * The list of ready task is just the list of ready continuations.
 */
private val readyList = mutableListOf<TaskContinuation>()

/*
 * The scheduling function.
 * All the cooperative tasks run in the context of the JVM thread where this function is called.
 */
fun schedule() {
    while (true) {
        if (readyList.isEmpty()) {
            // Since we don't have any I/O or timers, there isn't anything left to do if the ready list is empty.
            log.info("Ready list is empty, nothing else to do. Ending.")
            return
        }
        /*
         * Scheduling: just take the first continuation of the list and run it.
         * This call will return when the cooperative task suspends or ends.
         */
        readyList.dequeue().resume(Unit)
    }
}

/*
 * Suspend current cooperative task, but keep it ready.
 */
suspend fun yield() =
    suspendCoroutineUninterceptedOrReturn<Unit> { cont ->
        readyList.enqueue(cont)
        COROUTINE_SUSPENDED
    }

/*
 * Suspend current cooperative task, storing it in the given wait queue.
 */
suspend fun park(waitQueue: Queue<TaskContinuation>) =
    suspendCoroutineUninterceptedOrReturn<Unit> { cont ->
        waitQueue.enqueue(cont)
        COROUTINE_SUSPENDED
    }

/*
 * Remove cooperative task from queue and add it to the ready list.
 */
fun unpark(waitQueue: Queue<TaskContinuation>) {
    readyList.enqueue(waitQueue.dequeue())
}

/*
 * Continuation that represents the start of a cooperative task.
 */
private class StartContinuation(private val taskBlock: suspend () -> Unit) : Continuation<Unit> {
    /*
     * Using the [EmptyCoroutineContext] so no schedulers are used.
     */
    override val context: CoroutineContext get() = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>) {
        taskBlock.startCoroutineUninterceptedOrReturn(EndContinuation())
    }
}

/*
 * Continuation that represents the end of a cooperative task.
 */
private class EndContinuation() : Continuation<Unit> {
    override val context: CoroutineContext get() = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>) {
        log.info("cooperative thread ending")
    }
}

/*
 * Create and start a cooperative task.
 * A cooperative task is represented by a suspend function without parameters and returning Unit.
 */
fun createAndStartCooperativeTask(taskBlock: suspend () -> Unit) {
    // Here we just create the continuation to start the task and store it on the ready list.
    // It will be eventually picked up on a future schedule
    readyList.add(StartContinuation(taskBlock))
}
