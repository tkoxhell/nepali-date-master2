package np.com.naveenniraula.ghadi.data

import android.text.TextUtils
import android.util.Log
import np.com.naveenniraula.ghadi.miti.Date
import java.util.*

data class DateItem(
    val date: String,
    var dateEnd: String = "",
    var month: String = "",
    var year: String = "",
    var adDate: String = "",
    var adMonth: String = "",
    var adYear: String = "",
    var isToday: Boolean = false,
    var isSelected: Boolean = false,
    var isClickable: Boolean = false,
    var isHoliday: Boolean = false
) {

    override fun toString(): String {
        val currentData =
            String.format(
                "{ date: %s, dateEnd: %s, month: %s, year: %s, adDate: %s, adMonth: %s, adYear: %s, isToday: %s, isSelected: %s, isClickable: %s, isHoliday: %s }",
                date,
                dateEnd,
                month,
                year,
                adDate,
                adMonth,
                adYear,
                isToday,
                isSelected,
                isClickable,
                isHoliday
            )
        return currentData
        // return "$year-$month-$date"
    }

    companion object {
        fun getTodayNepali(): DateItem {
            val cal = Calendar.getInstance()
            val today = Date(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            ).convertToNepali()
            return DateItem(
                date = "${today.day}",
                month = "${today.month}",
                year = "${today.year}",
                isClickable = false,
                isSelected = true,
                isHoliday = false,
                isToday = true
            )
        }
    }

}