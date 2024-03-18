import java.util.concurrent.atomic.*

/**
 * @author Shpileva Anastasiya
 */
class MSQueue<E> : Queue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>
    private val dummy: Node<E> = Node(null)

    init {
        tail = AtomicReference(dummy)
        head = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        var ind = 0
        while (true) {
            val newTail = Node(element)
            ind += 1
            val tailCur = tail.get()
            val nextTail = tailCur.next
            if (nextTail.compareAndSet(null, newTail)) {
                tail.compareAndSet(tailCur, newTail)
                ind += 2
                break
            } else {
                tail.compareAndSet(tailCur, nextTail.get()!!)
            }
        }
    }

    override fun dequeue(): E? {
        var ind = 0
        while (true) {
            val headCur = head.get()
            ind += 1
            val curHeadNext = headCur.next.get()
            if(curHeadNext == null){
                return null
            }
            ind += 1
            if (head.compareAndSet(headCur, curHeadNext)) {
                val curHeadNextElement = curHeadNext.element
                curHeadNext.element = null
                return curHeadNextElement
            }
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    override fun validate() {
        check(tail.get().next.get() == null) {
            "At the end of the execution, `tail.next` must be `null`"
        }
        check(head.get().element == null) {
            "At the end of the execution, the dummy node shouldn't store an element"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}
