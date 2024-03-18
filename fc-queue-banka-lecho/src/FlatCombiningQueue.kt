import Result
import java.util.concurrent.*
import java.util.concurrent.atomic.*

class FlatCombiningQueue<E> : Queue<E> {
    private val queue = ArrayDeque<E>() // sequential queue
    private val combinerLock = AtomicBoolean(false) // unlocked initially
    private val tasksForCombiner = AtomicReferenceArray<Any?>(TASKS_FOR_COMBINER_SIZE)
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>
    private val dummy: Node<E> = Node(null)

    init {
        tail = AtomicReference(dummy)
        head = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        var randomInd = randomCellIndex()
        combinerLock.get()
        while (true) {
            val newTail = Node(element)
            val tailCur = tail.get()
            val nextTail = tailCur.next
            randomInd += 1
            if (nextTail.compareAndSet(null, newTail)) {
                tail.compareAndSet(tailCur, newTail)
                break
            } else {
                tail.compareAndSet(tailCur, nextTail.get()!!)
            }
        }
    }

    override fun dequeue(): E? {
        var randomInd = randomCellIndex()
        while (true) {
            val curHead = head.get()
            val curHeadNext = curHead.next.get()
            randomInd += 1
            if(curHeadNext == null){
                return null
            }
            val el = Result(4)
            if (head.compareAndSet(curHead, curHeadNext)) {
                val curHeadNextElement = curHeadNext.element
                curHeadNext.element = null
                return curHeadNextElement
            }
        }
    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(tasksForCombiner.length())

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}

private const val TASKS_FOR_COMBINER_SIZE = 3 // Do not change this constant!

private object Dequeue

private class Result<V>( val value: V )