import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * @author  Shpileva Anastasiya
 */

open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()

    private val eliminationArray = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushElimination(element: E): Boolean {
        val randomIndex = randomCellIndex()

        if (!eliminationArray.compareAndSet(randomIndex, CELL_STATE_EMPTY, element)) {
            return false
        } else {
            if (popHelp(randomIndex)) {
                return true
            }
            if (eliminationArray.compareAndSet(randomIndex, element, CELL_STATE_EMPTY)) {
                return false
            } else {
                eliminationArray.set(randomIndex, CELL_STATE_EMPTY)
                return true
            }
        }
    }

    private fun popHelp(randomCellIndex: Int): Boolean {
        repeat(ELIMINATION_WAIT_CYCLES) {
            if (eliminationArray.compareAndSet(randomCellIndex, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) {
                return true
            }
        }
        return false
    }

    override fun pop(): E? = tryPopElimination() ?: stack.pop()

    protected open fun tryPopElimination(): E? {
        val randomIndex = randomCellIndex()
        val cell = eliminationArray[randomIndex]
        if (cell == CELL_STATE_EMPTY) {
            return null
        }
        if (cell == CELL_STATE_RETRIEVED) {
            return null
        }

        if (!eliminationArray.compareAndSet(randomIndex, cell, CELL_STATE_RETRIEVED)) {
            return null
        } else {
            return cell as E
        }
    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(eliminationArray.length())

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 2 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        private val CELL_STATE_EMPTY = null
        private val CELL_STATE_RETRIEVED = Any()
    }
}