package dijkstra

import java.util.concurrent.Phaser
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import java.util.PriorityQueue
import kotlin.Comparator


private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

private class MultiQueue(

    val workers: Int
) {
    private val size: Int = workers * 2
    private val locks: Array<ReentrantLock> =
        Array(size) { ReentrantLock() }
    private val queues: Array<PriorityQueue<Node>> =
        Array(size) { PriorityQueue<Node>(workers, NODE_DISTANCE_COMPARATOR) }

    fun enqueue(x: Node) {
        while (true) {
            val index = getRandomQueue()

            if (locks[index].tryLock()) {
                try {
                    queues[index].add(x)
                    break
                } finally {
                    locks[index].unlock()
                }
            }

        }
    }
    fun dequeue(): Node? {
        var index1 = getRandomQueue()
        val index2 = getRandomQueue()

        if (index2 == index1) {
            index1++
            index1 %= size
        }
        if (locks[index1].tryLock()) {

            try {
                if (locks[index2].tryLock()) {
                    try {
                        val firstValue = queues[index1].peek() ?: return queues[index2].poll()
                        val secondValue = queues[index2].peek() ?: return queues[index1].poll()

                        if (minOf(firstValue, secondValue, NODE_DISTANCE_COMPARATOR) == firstValue) {
                            return queues[index1].poll()
                        }
                        return queues[index2].poll()
                    } finally {
                        locks[index2].unlock()
                    }

                }

            } finally {
                locks[index1].unlock()
            }
        }
        return null
    }

    private fun getRandomQueue(): Int {
        return ThreadLocalRandom.current().nextInt(0, size)
    }
}

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()

    // The distance to the start node is `0`
    start.distance = 0

    // Create a priority (by distance) queue and add the start node into it
    val q = MultiQueue(workers)
    q.enqueue(start)
    val activeNodes = AtomicInteger(1)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while (activeNodes.get() > 0) {
                val cur = q.dequeue() ?: continue
                val d = cur.distance
                for (edge in cur.outgoingEdges) {
                    var dist = edge.to.distance
                    while (dist > d + edge.weight) {
                        if (edge.to.casDistance(dist, d + edge.weight)) {
                            q.enqueue(edge.to)
                            activeNodes.getAndIncrement()
                            break
                        }
                        dist = edge.to.distance
                    }
                }
                activeNodes.getAndDecrement()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}