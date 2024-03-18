/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :: Shpileva Anastasiya
 */
class Solution : MonotonicClock {
    // d1 - hour, d2 - minute, d1- seconds
    private var actualHour by RegularInt(0) // second
    private var actualMinute by RegularInt(0) // minute
    private var actualSecond by RegularInt(0) // hour

    private var savedHour by RegularInt(0) // hour
    private var savedMinute by RegularInt(0) // minute

    override fun write(time: Time) {
        actualHour = time.hours
        actualMinute = time.minutes
        actualSecond = time.seconds
        savedMinute = time.minutes
        savedHour = time.hours
    }

    override fun read(): Time {
        val copyHours = savedHour
        val copyMinutes = savedMinute
        val locSec = actualSecond
        val locMin = actualMinute
        val locHour = actualHour
        if (copyHours == locHour) {
            return if (copyMinutes == locMin) Time(locHour, locMin, locSec) else Time(locHour, locMin, 0)
        }
        return Time(locHour, 0, 0)
    }
}