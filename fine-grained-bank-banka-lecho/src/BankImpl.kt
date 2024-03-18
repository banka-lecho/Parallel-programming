import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Bank implementation.
 *
 * :TODO: This implementation has to be made thread-safe.
 *
 * @author :TODO: LastName FirstName
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val numberOfAccounts: Int
        get() = accounts.size

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override fun getAmount(index: Int): Long {
        val account = accounts[index]
        account.lock.withLock {
            return account.amount
        }
    }

    override val totalAmount: Long
        // тут я должна ручками брать блокировку у счета
        get() {
            try{
                var totally : Long = 0
                for (account in accounts){
                    account.lock.lock()
                    totally += account.amount
                }
                return totally
            } finally {
                accounts.forEach { account -> account.lock.unlock() }
            }
        }

    override fun deposit(index: Int, amount: Long): Long {
        // ну чтение и присвоение я могу не засовывать withlock, а вот всё остальное да
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        return account.lock.withLock {
            check(!(amount > Bank.MAX_AMOUNT || account.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            account.amount += amount
            account.amount
        }
    }


    // уменьшение денег на счете
    override fun withdraw(index: Int, amount: Long): Long {
        // аналогично прошлому
        require(amount > 0) { "Invalid amount: $amount" }

        val account = accounts[index]
        return account.lock.withLock {
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            account.amount
        }
    }

    private fun lockT (firstAcc: Account, secondAcc: Account){
        firstAcc.lock.lock()
        secondAcc.lock.lock()
    }
    private fun unlockT (firstAcc: Account, secondAcc: Account){
        firstAcc.lock.unlock()
        secondAcc.lock.unlock()
    }

    // перекидывание денег со счета на счет
    override fun transfer(fromIndex: Int, toIndex: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromIndex != toIndex) { "fromIndex == toIndex" }
        val fromAcc = accounts[fromIndex]
        val toAcc = accounts[toIndex]
        if (fromIndex < toIndex) lockT(fromAcc, toAcc) else lockT(toAcc, fromAcc)
        try {
            check(amount <= fromAcc.amount) { "Underflow" }
            check(!(amount > Bank.MAX_AMOUNT || toAcc.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            toAcc.amount += amount
            fromAcc.amount -= amount
        } finally {
            if (fromIndex > toIndex) unlockT(fromAcc, toAcc) else unlockT(toAcc, fromAcc)
        }

    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        var amount: Long = 0
        val lock = ReentrantLock(true)
    }
}