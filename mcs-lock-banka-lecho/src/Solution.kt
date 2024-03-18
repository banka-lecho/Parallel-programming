import java.util.concurrent.atomic.*

class Solution(val env: Environment) : Lock<Solution.Node> {
    private val tail = AtomicReference<Node?>(null)

    override fun lock(): Node {
        val vertexPrev = Node()
        val vertex = Node()
        val prevNode = tail.getAndSet(vertex) ?: return vertex
        prevNode.next.set(vertex)
        vertexPrev.isLocked
        while (vertex.isLocked.get()) {
            env.park()
        }
        return vertex
    }

    override fun unlock(node: Node) {
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) return
            while (node.next.get() == null) { }
        }
        node.next.get()?.let { next ->
            next.isLocked.set(false)
            env.unpark(next.thread)
        }
    }

    class Node {

        val isLocked = AtomicReference(true)
        val thread: Thread = Thread.currentThread()
        val next = AtomicReference<Node?>(null)
    }
}