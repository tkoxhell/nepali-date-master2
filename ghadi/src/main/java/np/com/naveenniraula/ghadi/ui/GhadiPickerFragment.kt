package np.com.naveenniraula.ghadi.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import np.com.naveenniraula.ghadi.R
import np.com.naveenniraula.ghadi.data.DateItem
import np.com.naveenniraula.ghadi.data.GhadiResult
import np.com.naveenniraula.ghadi.listeners.DatePickCompleteListener
import np.com.naveenniraula.ghadi.miti.Date
import np.com.naveenniraula.ghadi.miti.DateUtils
import np.com.naveenniraula.ghadi.utils.ColorUtil
import np.com.naveenniraula.ghadi.utils.ConversionUtil
import np.com.naveenniraula.ghadi.utils.Ui
import java.util.*

class GhadiPickerFragment : DialogFragment() {

    companion object {

        fun newInstance() = GhadiPickerFragment()
        fun newInstance(date: Date): GhadiPickerFragment {
            val ghadiPickerFragment = GhadiPickerFragment()

            ghadiPickerFragment.requestedDate = date.convertToEnglish()

            Log.i("BQ7CH72", "${ghadiPickerFragment.requestedDate} instance!")

            return ghadiPickerFragment
        }

        @Deprecated("Pass an instance of date instead of year, month and day.")
        fun newInstance(year: Int, month: Int, day: Int): GhadiPickerFragment {
            val ghadiPickerFragment = GhadiPickerFragment()

            ghadiPickerFragment.requestedDate = Date(year, month, day).convertToEnglish()

            Log.i("BQ7CH72", "${ghadiPickerFragment.requestedDate} instance!")

            return ghadiPickerFragment
        }

        const val DAYS_IN_A_WEEK = 7
        const val DAYS_START_NUM = 1
    }

    var bgFgColor: Pair<Int, Int> = Pair(Color.BLACK, Color.WHITE)

    private val adapter = NepaliDateAdapter<DateItem>()
    private lateinit var fManager: FragmentManager

    private lateinit var requestedDate: Date
    private lateinit var viewModel: GhadiPickerViewModel
    private lateinit var datePickCompleteListener: DatePickCompleteListener

    private lateinit var alertDialog: AlertDialog
    private lateinit var rootView: View

    private val currentDateInNepali = Date(Calendar.getInstance()).convertToNepali()

    private val listenerException =
        IllegalAccessException("DatePickCompleteListener has not been implemented. Please implement this to return result when action is completed.")

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog_Alert)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let {
            val inflater = LayoutInflater.from(it)
            rootView = inflater.inflate(R.layout.ghadi_picker_fragment, null, false)

            setupNepaliDate()

            setupListeners()

            val builder = AlertDialog.Builder(it)
            builder.setView(rootView)

            val width = activity?.window?.decorView?.width ?: 0

            alertDialog = builder.create()
            alertDialog.window?.setLayout(width, 400)

            return alertDialog
        } ?: return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GhadiPickerViewModel::class.java)
        viewModel.getCalendarData().observe(this, androidx.lifecycle.Observer {
            adapter.setDataList(it)
        })
        viewModel.prepareCalendarData(
            if (::requestedDate.isInitialized) {
                requestedDate
            } else {
                Date(Calendar.getInstance())
            }, true
        )
    }

    /**
     * Setup listeners of all available actions.
     * Year [next | prev]; Month [next | prev] and [ OK | Cancel]
     */
    private fun setupListeners() {

        // -----------------------
        // year button listeners
        // -----------------------

        val year = getRootView().findViewById<TextView>(R.id.gpfYear)
        year.setBackgroundColor(bgFgColor.first)
        year.setTextColor(bgFgColor.second)
        year.text =
            if (::requestedDate.isInitialized) ConversionUtil.toNepali(requestedDate.convertToNepali().year.toString())
            else ConversionUtil.toNepali(currentDateInNepali.year.toString())

        val yPrev = getRootView().findViewById<ImageButton>(R.id.gpfPrevYear)
        val yNext = getRootView().findViewById<ImageButton>(R.id.gpfNextYear)

        yPrev.setOnClickListener {

            val yearNumber = getDisplayedYear() - 1

            if (yearNumber == DateUtils.endNepaliYear - 1) {
                Ui.tintButtonImage(yPrev, android.R.color.holo_red_dark)
            } else {
                Ui.tintButtonImage(yNext, android.R.color.white)
            }

            if (yearNumber <= DateUtils.startNepaliYear) {
                return@setOnClickListener
            }

            val upcomingMonthNumber = DateUtils.getMonthNumber(getDisplayedMonth())
            year.text = ConversionUtil.toNepali(yearNumber.toString())
            changeDate(Date(yearNumber, upcomingMonthNumber, 1))
        }
        yNext.setOnClickListener {

            val yearNumber = getDisplayedYear() + 1

            if (yearNumber == DateUtils.endNepaliYear - 1) {
                Ui.tintButtonImage(yNext, android.R.color.holo_red_dark)
            } else {
                Ui.tintButtonImage(yPrev, android.R.color.white)
            }

            if (yearNumber >= DateUtils.endNepaliYear) {
                return@setOnClickListener
            }

            val upcomingMonthNumber = DateUtils.getMonthNumber(getDisplayedMonth())
            year.text = ConversionUtil.toNepali(yearNumber.toString())

            changeDate(Date(yearNumber, upcomingMonthNumber, 1))
        }

        // -----------------------
        // month button listeners
        // -----------------------

        val month = getRootView().findViewById<TextView>(R.id.gpfMonth)
        month.setBackgroundColor(ColorUtil.lighten(bgFgColor.first, .5))
        month.setTextColor(bgFgColor.second)
        month.text =
            if (::requestedDate.isInitialized) DateUtils.getMonthName(requestedDate.convertToNepali().month)
            else DateUtils.getMonthName(currentDateInNepali.month)

        val next = getRootView().findViewById<ImageButton>(R.id.gpfNextMonth)
        next.setOnClickListener {

            val nYear = getDisplayedYear()
            val upcomingMonthName = DateUtils.getNextMonthName(getDisplayedMonth())
            val upcomingMonthNumber = DateUtils.getMonthNumber(upcomingMonthName)
            month.text = upcomingMonthName
            changeDate(Date(nYear, upcomingMonthNumber, 1))
        }

        val prev = getRootView().findViewById<ImageButton>(R.id.gpfPrevMonth)
        prev.setOnClickListener {

            val nYear = getDisplayedYear()
            val upcomingMonthName = DateUtils.getPreviousMonthName(getDisplayedMonth())
            val upcomingMonthNumber = DateUtils.getMonthNumber(upcomingMonthName)
            month.text = upcomingMonthName
            changeDate(Date(nYear, upcomingMonthNumber, 1))
        }

        // -----------------------
        // action button listeners
        // -----------------------

        val confirm = getRootView().findViewById<Button>(R.id.gpfConfirm)
        confirm.setBackgroundColor(bgFgColor.first)
        confirm.setTextColor(bgFgColor.second)
        confirm.setOnClickListener {

            if (!::datePickCompleteListener.isInitialized) throw listenerException

            val date = adapter.getSelectedDate()

            Log.d("jqiu7", "$date")

            val engDate =
                Date(date.year.toInt(), date.month.toInt(), date.date.toInt()).convertToEnglish()
            val weekDayNumber = engDate.weekDayNum

            val humanReadableBs = engDate.convertToNepali().readableBsDate
            val humanReadableAd = engDate.readableAdDate

            val result = GhadiResult(
                date.date.toInt(),
                date.month.toInt(),
                date.year.toInt(),
                engDate.day,
                engDate.month,
                engDate.year,
                weekDayNumber,
                DateUtils.getDayName(weekDayNumber),
                humanReadableBs,
                humanReadableAd
            )

            Log.d("BQ7CH72", "${result.getTimestamp()}")

            datePickCompleteListener.onDateSelectionComplete(result)
            dismiss()
        }
        val cancel = getRootView().findViewById<Button>(R.id.gpfCancel)
        cancel.setBackgroundColor(bgFgColor.first)
        cancel.setTextColor(bgFgColor.second)
        cancel.setOnClickListener {

            if (!::datePickCompleteListener.isInitialized) throw listenerException
            dismiss()
        }
    }

    /**
     * Changes date based on the button click of respective entity; Month or Year.
     */
    private fun changeDate(date: Date?) {
        date?.convertToEnglish()?.let {
            viewModel.prepareCalendarData(it)
        } ?: showDateUnavailable()
    }

    private fun showDateUnavailable() {

    }

    /**
     * Prepare nepali date for current month.
     */
    private fun setupNepaliDate() {
        val recyclerView = getRootView().findViewById<RecyclerView>(R.id.nepaliDateList)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, DAYS_IN_A_WEEK)
    }

    fun setDatePickCompleteListener(datePickCompleteListener: DatePickCompleteListener) {
        this.datePickCompleteListener = datePickCompleteListener
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fManager = fragmentManager
    }

    fun show() {
        if (::fManager.isInitialized) {
            show(fManager, this.tag)
        } else {
            Log.d("BQ7CH72", "fragment manager has not been initialized.")
        }
    }

    /**
     * Get the root view of this fragment.
     */
    private fun getRootView(): View {
        return rootView
    }

    /**
     * Get currently visible month name in the header of the calendar view.
     * @return name of the month currently visible.
     */
    private fun getDisplayedMonth(): String {
        val month = getRootView().findViewById<TextView>(R.id.gpfMonth)
        return month.text.toString()
    }

    /**
     * Get currently visible year in the header of the calendar view.
     * @return year number as [Int].
     */
    private fun getDisplayedYear(): Int {
        val year = getRootView().findViewById<TextView>(R.id.gpfYear)
        return year.text.toString().toInt()
    }

}
