import java.util.concurrent.atomic.*
import kotlin.math.*

/**
 * @author TODO: Last Name, First Name
 */
class FAABasedQueueSimplified<E> : Queue<E> {
    private val infiniteArray = AtomicReferenceArray<Any?>(1024) // conceptually infinite array
    private val enqIdx = AtomicLong(0)
    private val deqIdx = AtomicLong(0)
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

    override fun validate() {
        for (i in 0 until min(deqIdx.get().toInt(), enqIdx.get().toInt())) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `deqIdx = ${deqIdx.get()}` at the end of the execution"
            }
        }
        for (i in max(deqIdx.get().toInt(), enqIdx.get().toInt()) until infiniteArray.length()) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `enqIdx = ${enqIdx.get()}` at the end of the execution"
            }
        }
    }
    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}

// TODO: poison cells with this value.
private val POISONED = Any()