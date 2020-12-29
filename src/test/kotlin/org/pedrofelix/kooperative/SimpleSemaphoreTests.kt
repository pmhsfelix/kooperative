package org.pedrofelix.kooperative

import org.junit.Assert
import org.junit.Test

class SimpleSemaphoreTests {

    @Test
    fun first() {
        val nOfTasks = 1000
        val nOfReps = 1000
        var acc = 0
        val semaphore = SimpleSemaphore(1)

        for (i in 1..nOfTasks) {
            createAndStartCooperativeTask {
                for (j in 1..nOfReps) {
                    semaphore.acquire()
                    acc += 1
                    yield()
                    semaphore.release()
                    yield()
                }
            }
        }

        Assert.assertEquals(0, acc)
        schedule()
        Assert.assertEquals(nOfTasks * nOfReps, acc)
    }
}
