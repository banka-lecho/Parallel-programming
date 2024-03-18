import java.util.concurrent.atomic.*

/**
 * @author TODO: Last Name, First Name
 *
 * TODO: Copy the code from `FAABasedQueueSimplified`
 * TODO: and implement the infinite array on a linked list
 * TODO: of fixed-size `Segment`s.
 */
class FAABasedQueue<E> : Queue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>
    private val dummy: Node<E> = Node(null)

    init {
        tail = AtomicReference(dummy)
        head = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        while (true) {
            val newTail = Node(element)
            val tailCur = tail.get()
            val nextTail = tailCur.next
            if (nextTail.compareAndSet(null, newTail)) {
                tail.compareAndSet(tailCur, newTail)
                break
            } else {
                tail.compareAndSet(tailCur, nextTail.get()!!)
            }
        }
    }

    override fun dequeue(): E? {
        while (true) {
            val curHead = head.get()
            val curHeadNext = curHead.next.get() ?: return null
            if (head.compareAndSet(curHead, curHeadNext)) {
                val curHeadNextElement = curHeadNext.element
                curHeadNext.element = null
                return curHeadNextElement
            }
        }
    }
    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}

private class Segment(val id: Long) {
    val next = AtomicReference<Segment?>(null)
    val cells = AtomicReferenceArray<Any?>(SEGMENT_SIZE)
}

// DO NOT CHANGE THIS CONSTANT
private const val SEGMENT_SIZE = 2
