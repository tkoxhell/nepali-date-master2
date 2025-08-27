package np.com.naveenniraula.ghadi.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.Observable
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import np.com.naveenniraula.ghadi.R
import np.com.naveenniraula.ghadi.data.DateItem
import np.com.naveenniraula.ghadi.data.GhadiResult
import np.com.naveenniraula.ghadi.databinding.CalendarDialogFragmentBinding
import np.com.naveenniraula.ghadi.listeners.DatePickCompleteListener
import np.com.naveenniraula.ghadi.miti.Date
import np.com.naveenniraula.ghadi.miti.DateUtils
import np.com.naveenniraula.ghadi.utils.ConversionUtil
import np.com.naveenniraula.ghadi.utils.Ui
import java.util.*

class CalendarDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = CalendarDialogFragment()

        fun newInstance(date: Date): CalendarDialogFragment {
            val ghadiPickerFragment = CalendarDialogFragment()
            ghadiPickerFragment.requestedDate = date.convertToEnglish()
            return ghadiPickerFragment
        }

        @Deprecated("Don't use this method. Pass in the Date instance instead.")
        fun newInstance(year: Int, month: Int, day: Int): CalendarDialogFragment {
            val ghadiPickerFragment = CalendarDialogFragment()
            ghadiPickerFragment.requestedDate = Date(year, month, day).convertToEnglish()
            return ghadiPickerFragment
        }

        const val DAYS_IN_A_WEEK = 7
        const val DAYS_START_NUM = 1
    }

    private lateinit var mBinding: CalendarDialogFragmentBinding

    var bgFgColor: Pair<Int, Int> = Pair(Color.BLACK, Color.WHITE)

    private val nepaliAdapter = NepaliDateAdapter<DateItem>()
    private val adAdapter = AdDateAdapter()

    private lateinit var mFragmentManager: FragmentManager
    private lateinit var requestedDate: Date
    private val viewModel: CalendarDialogViewModel by lazy {
        ViewModelProvider(this).get(CalendarDialogViewModel::class.java)
    }
    private lateinit var datePickCompleteListener: DatePickCompleteListener

    private var minHeight = 0
    private var lastHeight = 0
    private var addedHeight = 0

    private lateinit var alertDialog: AlertDialog

    private val currentDateInNepali = Date(Calendar.getInstance()).convertToNepali()

    private val listenerException =
        IllegalAccessException("DatePickCompleteListener has not been implemented. Please implement this to return result when action is completed.")

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog_Alert)
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let {
            val inflater = LayoutInflater.from(it)
            mBinding = CalendarDialogFragmentBinding.inflate(inflater)

            setupDateAdapter()

            setupListeners()

            mBinding.materialButtonToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.btn1 -> viewModel.ui.isBs = false
                        R.id.btn2 -> viewModel.ui.isBs = true
                    }
                }
            }

            val builder = AlertDialog.Builder(it, R.style.CustomAlertDialogCalendar)
            builder.setView(mBinding.root)

            val width = activity?.window?.decorView?.width ?: 0

            alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window?.setLayout(width, 400)

            return alertDialog
        } ?: return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mBinding.ui = viewModel.ui
        viewModel.ui.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val ui = viewModel.ui
                when (propertyId) {
                    BR.bs -> {
                        mBinding.nepaliDateList.adapter = if (ui.isBs) nepaliAdapter else adAdapter
                        if (ui.isBs) {
                            changeDate(currentDateInNepali)
                            nepaliAdapter.adBsToggled()
                        } else {
                            changeDate(viewModel.currentEnglishDate.convertToNepali())
                            adAdapter.adBsToggled()
                        }
                    }
                }
            }
        })

        viewModel.getCalendarData().observe(this, androidx.lifecycle.Observer {
            viewModel.ui.isCalculating = false
            if (viewModel.ui.isBs) {
                nepaliAdapter.setDataList(it)
            } else {
                adAdapter.setDataList(it)
            }
            adjustRecyclerViewHeight(it.size)
        })

        val _date = if (::requestedDate.isInitialized) {
            requestedDate
        } else {
            Date(Calendar.getInstance())
        }

        if (viewModel.ui.isBs) viewModel.prepareCalendarData(_date, true)
        else viewModel.getAdDate(_date, true)
    }

    private fun adjustRecyclerViewHeight(itemCount: Int) {
        val currentHeight = mBinding.nepaliDateList.layoutManager?.height ?: 0

        if (lastHeight == 0) {
            lastHeight = currentHeight
            minHeight = lastHeight
            addedHeight = currentHeight / 6
        }

        if (itemCount < 43) {
            lastHeight = minHeight
        } else {
            if ((lastHeight - addedHeight) == minHeight) {
                // do nothing
            } else {
                lastHeight = minHeight + addedHeight
            }
        }

        val lp = mBinding.nepaliDateList.layoutParams
        lp.height = lastHeight
        mBinding.nepaliDateList.layoutParams = lp

        val plp = mBinding.progressBarParent.layoutParams
        plp.height = lp.height
        mBinding.progressBarParent.layoutParams = plp
    }

    private fun setupDateAdapter() {
        val recyclerView = getRootView().findViewById<RecyclerView>(R.id.nepaliDateList)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = if (viewModel.ui.isBs) nepaliAdapter else adAdapter
        recyclerView.layoutManager = GridLayoutManager(context, DAYS_IN_A_WEEK)
    }

    private fun setupListeners() {
        mBinding.gpfYear.setTextColor(getResources().getColor(R.color.collMat))
        viewModel.ui.bsYear =
            if (::requestedDate.isInitialized) ConversionUtil.toNepali(requestedDate.convertToNepali().year.toString())
                ?: ""
            else ConversionUtil.toNepali(currentDateInNepali.year.toString()) ?: ""

        val yPrev = getRootView().findViewById<ImageButton>(R.id.gpfPrevYear)
        val yNext = getRootView().findViewById<ImageButton>(R.id.gpfNextYear)

        yPrev.setOnClickListener {
            val yearNumber = getDisplayedYear() - 1

            if (yearNumber == DateUtils.endNepaliYear - 1) {
                Ui.tintButtonImage(yPrev, R.color.holo_red_dark)
            } else {
                Ui.tintButtonImage(yNext, R.color.collMat)
            }

            if (yearNumber <= DateUtils.startNepaliYear) {
                return@setOnClickListener
            }

            if (!viewModel.ui.isBs) {
                viewModel.currentEnglishDate.year -= 1
                changeDate(null)
            } else {
                val upcomingMonthNumber = DateUtils.getMonthNumber(getDisplayedMonth())
                mBinding.ui!!.bsYear = ConversionUtil.toNepali(yearNumber.toString()) ?: ""
                changeDate(Date(yearNumber, upcomingMonthNumber, 1))
            }
        }
        yNext.setOnClickListener {
            val yearNumber = getDisplayedYear() + 1

            if (yearNumber == DateUtils.endNepaliYear - 1) {
                Ui.tintButtonImage(yNext, R.color.holo_red_dark)
            } else {
                Ui.tintButtonImage(yPrev, R.color.collMat)
            }

            if (yearNumber >= DateUtils.endNepaliYear) {
                return@setOnClickListener
            }

            if (!viewModel.ui.isBs) {
                viewModel.currentEnglishDate.year += 1
                changeDate(null)
            } else {
                val upcomingMonthNumber = DateUtils.getMonthNumber(getDisplayedMonth())
                mBinding.ui!!.bsYear = ConversionUtil.toNepali(yearNumber.toString()) ?: ""
                changeDate(Date(yearNumber, upcomingMonthNumber, 1))
            }
        }

        mBinding.gpfMonth.setTextColor(getResources().getColor(R.color.collMat))
        var extMonth = if (::requestedDate.isInitialized) {
            DateUtils.MONTH_NAMES_MAPPED[requestedDate.convertToNepali().month]
        } else {
            DateUtils.MONTH_NAMES_MAPPED[currentDateInNepali.month]
        }
        viewModel.ui.bsMonth = if (::requestedDate.isInitialized) {
            DateUtils.getMonthName(requestedDate.convertToNepali().month)
        } else {
            DateUtils.getMonthName(currentDateInNepali.month)
        }

        viewModel.ui.bsMonth = "${viewModel.ui.bsMonth} ( $extMonth )"

        mBinding.gpfNextMonth.setOnClickListener {
            if (viewModel.ui.isBs) {
                val nYear = getDisplayedYear()
                val upcomingMonthName = DateUtils.getNextMonthName(getDisplayedMonth())
                val upcomingMonthNumber = DateUtils.getMonthNumber(upcomingMonthName)
                viewModel.ui.bsMonth = upcomingMonthName
                changeDate(Date(nYear, upcomingMonthNumber, 1))
                extMonth = DateUtils.MONTH_NAMES_MAPPED[upcomingMonthNumber]
                viewModel.ui.bsMonth = "${viewModel.ui.bsMonth} ( $extMonth )"
            } else {
                viewModel.currentEnglishDate.month = viewModel.currentEnglishDate.month + 1
                changeDate(null)
            }
        }

        val prev = getRootView().findViewById<ImageButton>(R.id.gpfPrevMonth)
        prev.setOnClickListener {
            if (viewModel.ui.isBs) {
                val nYear = getDisplayedYear()
                val upcomingMonthName = DateUtils.getPreviousMonthName(getDisplayedMonth())
                val upcomingMonthNumber = DateUtils.getMonthNumber(upcomingMonthName)
                viewModel.ui.bsMonth = upcomingMonthName
                changeDate(Date(nYear, upcomingMonthNumber, 1))
                extMonth = DateUtils.MONTH_NAMES_MAPPED[upcomingMonthNumber]
                viewModel.ui.bsMonth = "${viewModel.ui.bsMonth} ( $extMonth )"
            } else {
                viewModel.currentEnglishDate.month = viewModel.currentEnglishDate.month - 1
                changeDate(null)
            }
        }

        val confirm = getRootView().findViewById<TextView>(R.id.gpfConfirm)
        confirm.setOnClickListener {
            if (!::datePickCompleteListener.isInitialized) throw listenerException

            val date = if (viewModel.ui.isBs) nepaliAdapter.getSelectedDate() else adAdapter.getSelectedDate()

            if (date.adYear == date.year) {
                date.adMonth = "${date.adMonth.toInt().inc()}"
            }

            Log.d("jqiu7", "$date")

            val engDate = Date(date.adYear.toInt(), date.adMonth.toInt(), date.adDate.toInt())
            val weekDayNumber = engDate.weekDayNum

            val humanReadableBs = engDate.convertToNepali().readableBsDate
            val humanReadableAd = engDate.readableAdDate

            val result = GhadiResult(
                date.date.toInt(),
                date.dateEnd.toInt(),
                date.month.toInt(),
                date.year.toInt(),
                date.adDate.toInt(),
                date.adMonth.toInt(),
                date.adYear.toInt(),
                weekDayNumber,
                DateUtils.getDayName(weekDayNumber),
                humanReadableBs,
                humanReadableAd
            )

            datePickCompleteListener.onDateSelectionComplete(result)
            Log.d("BQ7CH72", result.toString())
            dismiss()
        }
        val cancel = getRootView().findViewById<TextView>(R.id.gpfCancel)
        cancel.setOnClickListener {
            if (!::datePickCompleteListener.isInitialized) throw listenerException
            dismiss()
        }
    }

    private fun changeDate(date: Date?) {
        viewModel.ui.isCalculating = true
        Log.i("RVHEIGHT", "the date is null : ${date == null}")

        if (viewModel.ui.isBs) {
            date?.convertToEnglish()?.let {
                viewModel.prepareCalendarData(it)
            } ?: showDateUnavailable()
        } else {
            viewModel.getAdDate()
        }
    }

    private fun showDateUnavailable() {
        // Implement if needed
    }

    private fun getRootView(): View {
        return mBinding.root
    }

    private fun getDisplayedYear(): Int {
        return viewModel.ui.bsYear.toInt()
    }

    private fun getDisplayedMonth(): String {
        return viewModel.ui.bsMonth.split(" ")[0]
    }

    fun setDatePickCompleteListener(datePickCompleteListener: DatePickCompleteListener) {
        this.datePickCompleteListener = datePickCompleteListener
    }

    fun setFragmentManager(mFragmentManager: FragmentManager) {
        this.mFragmentManager = mFragmentManager
    }

    fun show() {
        show(childFragmentManager, this.tag)
    }
}