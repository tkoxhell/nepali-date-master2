package np.com.naveenniraula.ghadi.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import np.com.naveenniraula.ghadi.data.DateItem
import np.com.naveenniraula.ghadi.data.UiProperty
import np.com.naveenniraula.ghadi.miti.Date
import np.com.naveenniraula.ghadi.miti.DateUtils
import np.com.naveenniraula.ghadi.ui.CalendarDialogFragment.Companion.DAYS_IN_A_WEEK
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class CalendarDialogViewModel : ViewModel() {

    val ui = UiProperty()

    private val mThisMonth = Calendar.getInstance()
    var currentEnglishDate: Date = Date(Calendar.getInstance()).convertToEnglish()
        private set

    private val mService = Executors.newSingleThreadExecutor()
    private val calendarData: MutableLiveData<ArrayList<DateItem>> = MutableLiveData()

    fun getCalendarData(): MutableLiveData<ArrayList<DateItem>> {
        return calendarData
    }

    /**
     * Gets the calendar dates for current month in Nepali (BS) calendar.
     * @param date the date instance which can be any custom date.
     * @param markPassedDay whether to mark the passed day as selected.
     */
    fun prepareCalendarData(date: Date, markPassedDay: Boolean = false) {
        mService.submit {
            val list = ArrayList<DateItem>()

            // Add headers for days representation
            list.add(DateItem(DateUtils.HEADER_SUN))
            list.add(DateItem(DateUtils.HEADER_MON))
            list.add(DateItem(DateUtils.HEADER_TUE))
            list.add(DateItem(DateUtils.HEADER_WED))
            list.add(DateItem(DateUtils.HEADER_THU))
            list.add(DateItem(DateUtils.HEADER_FRI))
            list.add(DateItem(DateUtils.HEADER_SAT))

            val todayInBs = Date(Calendar.getInstance()).convertToNepali()
            val nepaliDate = date.convertToNepali()
            val numberOfDaysInMonth = DateUtils.getNumDays(nepaliDate.year, nepaliDate.month)

            // Calculate the starting day of the week for the first day of the month
            val nepMonthSuruVayekoEnglishDate =
                Date(nepaliDate.year, nepaliDate.month, 1).convertToEnglish()
            var saturdayIndex = 8 - nepMonthSuruVayekoEnglishDate.weekDayNum

            // Add padding for days before the first day of the month
            for (i in 1 until nepMonthSuruVayekoEnglishDate.weekDayNum) {
                list.add(DateItem.getDefault())
            }

            for (i in 1..numberOfDaysInMonth) {
                val model = DateItem(
                    date = "$i",
                    month = "${nepaliDate.month}",
                    year = "${nepaliDate.year}"
                )

                // Set holiday status (Saturdays)
                model.isHoliday = if (saturdayIndex == i) {
                    saturdayIndex += DAYS_IN_A_WEEK
                    true
                } else false

                // Set clickability based on whether the date is in the future
                model.isClickable = !model.isFutureDate()

                // Select passed date's day by default if specified
                if (markPassedDay && date.convertToNepali().day == i) {
                    model.isSelected = true
                }

                // Convert to English (AD) date for display
                val convertedAd = Date(nepaliDate.year, nepaliDate.month, i).convertToEnglish()
                convertedAd.apply {
                    model.adYear = this.year.toString()
                    model.adMonth = this.month.toString()
                    model.adDate = this.day.toString()
                }

                Log.d("DAteIIII", "actual : $date converted : $convertedAd")

                // Check if the date is today
                model.isToday = todayInBs.day == i
                        && model.year == todayInBs.year.toString()
                        && model.month == todayInBs.month.toString()

                if (model.isToday) {
                    currentEnglishDate =
                        Date(model.year.toInt(), model.month.toInt(), i).convertToEnglish()
                }

                list.add(model)
            }

            calendarData.postValue(list)
        }
    }

    private val months = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    /**
     * Prepare the date for English (AD) calendar and get it.
     *
     * @param date the date instance, or null to use currentEnglishDate.
     * @param markPassedDay whether to mark the passed day as selected.
     */
    fun getAdDate(
        date: Date? = null,
        markPassedDay: Boolean = false
    ) {
        mService.submit {
            val list = ArrayList<DateItem>()

            // Add headers for days representation
            list.add(DateItem(DateUtils.HEADER_SUN))
            list.add(DateItem(DateUtils.HEADER_MON))
            list.add(DateItem(DateUtils.HEADER_TUE))
            list.add(DateItem(DateUtils.HEADER_WED))
            list.add(DateItem(DateUtils.HEADER_THU))
            list.add(DateItem(DateUtils.HEADER_FRI))
            list.add(DateItem(DateUtils.HEADER_SAT))

            // Use provided date or currentEnglishDate
            val calendar = (date ?: currentEnglishDate).calendar

            val _year = calendar.get(Calendar.YEAR)
            val _month = calendar.get(Calendar.MONTH)
            val _maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val _originallySetDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Move to the start of the month to find the first day's weekday
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val _dayStartOffset = calendar.get(Calendar.DAY_OF_WEEK)

            log("we have _dayStartOffset : $_dayStartOffset")

            // Pad with default items for days before the first day
            for (index in 1 until _dayStartOffset) {
                val item = DateItem.getDefault()
                list.add(item)
            }

            // Today
            val thisYear = mThisMonth.get(Calendar.YEAR)
            val thisMonth = mThisMonth.get(Calendar.MONTH)
            val thisDay = mThisMonth.get(Calendar.DAY_OF_MONTH)

            // Generate days for the month
            for (day in 1.._maxDays) {
                val item = DateItem(
                    date = day.toString(),
                    year = _year.toString(),
                    month = _month.toString(),
                    adDate = day.toString(),
                    adYear = _year.toString(),
                    adMonth = (_month + 1).toString() // Adjust for 1-based month indexing
                )

                // Set clickability based on whether the date is in the future
                item.isClickable = !item.isFutureDate(isAdCalendar = true)

                // Mark today
                if ((_originallySetDayOfMonth == day)
                    && (_year == thisYear)
                    && (_month == thisMonth)
                ) {
                    item.isToday = true
                }

                list.add(item)
            }

            ui.adMonth = months[_month]
            ui.adYear = _year.toString()

            calendarData.postValue(list)
        }
    }

    private fun log(msg: String) {
        Log.i("RVHEIGHT", msg)
    }

    override fun onCleared() {
        super.onCleared()
        mService.shutdown()
    }
}
