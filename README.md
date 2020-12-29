# kooperative

A cooperative multi-tasking kernel based on Kotlin coroutines, for educational purposes.

![build](https://github.com/pmhsfelix/kooperative/workflows/build/badge.svg)

- All cooperative tasks are hosted in a single JVM thread.
- Task contexts are represented as coroutines continuations. 
Namely the "ready queue" is a list of task continuations.
- Scheduling is just retrieving the first continuation in the queue and resuming it.
- Asynchronous events (e.g. timers or I/O) are not supported for the moment being.
- Does not depend on [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines).
It only uses coroutine functionality from the standard library.

See [source code](https://github.com/pmhsfelix/kooperative/blob/main/src/main/kotlin/org/pedrofelix/kooperative/main.kt)
for more information.

