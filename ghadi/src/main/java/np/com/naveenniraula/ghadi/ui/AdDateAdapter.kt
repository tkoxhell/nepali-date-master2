package np.com.naveenniraula.ghadi.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import np.com.naveenniraula.ghadi.R
import np.com.naveenniraula.ghadi.data.DateItem
import np.com.naveenniraula.ghadi.miti.Date
import np.com.naveenniraula.ghadi.miti.DateUtils
import np.com.naveenniraula.ghadi.ui.CalendarDialogFragment.Companion.DAYS_IN_A_WEEK
import np.com.naveenniraula.ghadi.ui.CalendarDialogFragment.Companion.DAYS_START_NUM
import java.util.*

class AdDateAdapter : RecyclerView.Adapter<AdDateAdapter.Vh>() {

    private lateinit var mBgDrawable: Drawable
    private lateinit var mBgDrawableToday: Drawable

    private var dataList: ArrayList<DateItem> = arrayListOf()
    private lateinit var selectedDate: DateItem
    private val color = Color.parseColor("#7f8c8d")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        if (!::mBgDrawable.isInitialized) {
            mBgDrawableToday =
                ContextCompat.getDrawable(parent.context, R.drawable.bg_circle_padding)!!
            mBgDrawableToday.setBounds(8, 8, 8, 8)

            mBgDrawable = ContextCompat.getDrawable(parent.context, R.drawable.bg_circle_padding)!!
            mBgDrawable.setBounds(8, 8, 8, 8)
        }

        val inflater = LayoutInflater.from(parent.context)
        val vh = Vh(inflater.inflate(R.layout.item_date_cell, parent, false))
        vh.root.setOnClickListener {
            val di = dataList[vh.adapterPosition]
            if (di.isClickable) {
                if (lastCellPosition != RecyclerView.NO_POSITION) {
                    try {
                        val lastCell = dataList[lastCellPosition]
                        lastCell.isSelected = false
                        dataList[lastCellPosition] = lastCell
                    } catch (ex: IndexOutOfBoundsException) {
                        Log.e(
                            "RVHEIGHT",
                            "This is a non fatal crash. I have not handled the AD / BS toggle properly."
                        )
                    }
                }

                di.isSelected = true
                dataList[vh.adapterPosition] = di

                notifyItemChanged(lastCellPosition)
                notifyItemChanged(vh.adapterPosition)

                lastCellPosition = vh.adapterPosition
                selectedDate = di
            }
        }
        return vh
    }

    fun adBsToggled() {
        lastCellPosition = RecyclerView.NO_POSITION
        Log.d("AdDateAdapter", "adBsToggled called, resetting lastCellPosition")
    }

    fun getSelectedDate(): DateItem {
        if (!::selectedDate.isInitialized) {
            val cal = Calendar.getInstance()
            val today = Date(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val todayNepali = today.convertToNepali()
            selectedDate = DateItem(
                date = todayNepali.day.toString(),
                month = todayNepali.month.toString(),
                year = todayNepali.year.toString(),
                adDate = cal.get(Calendar.DAY_OF_MONTH).toString(),
                adMonth = (cal.get(Calendar.MONTH) + 1).toString(),
                adYear = cal.get(Calendar.YEAR).toString(),
                isClickable = true,
                isSelected = true,
                isHoliday = false,
                isToday = true
            )
        }
        return selectedDate.apply {
            dateEnd = DateUtils.getNumDays(year.toInt(), month.toInt()).toString()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        val dt = dataList[position]
        holder.adjust(dt)

        if (position >= DAYS_IN_A_WEEK && dt.date.toIntOrNull()?.let { it < DAYS_START_NUM } == true) {
            holder.test.text = EMPTY_STRING
            return
        }
    }

    fun setDataList(data: ArrayList<DateItem>) {
        dataList.clear()
        notifyDataSetChanged()

        dataList.addAll(data)
        notifyItemRangeInserted(0, data.size)
    }

    fun addData(data: DateItem) {
        dataList.add(data)
    }

    fun getData(position: Int): DateItem {
        return dataList[position]
    }

    class Vh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.idcRoot)
        val test: TextView = itemView.findViewById(R.id.tesss)
        val engDate: TextView = itemView.findViewById(R.id.english_date)

        fun adjust(di: DateItem) {
            root.isClickable = di.isClickable

            if (di.isToday) setTodayColor()
            else setNormalColor(di)

            if (di.isHoliday) setHolidayColor()

            test.text = di.date
            engDate.text = di.adDate

            if (!di.isClickable) {
                engDate.setTextColor(Color.WHITE)
                test.setTextColor(Color.GRAY)
            }
        }

        private fun setNormalColor(di: DateItem) {
            if (di.isSelected) {
                root.setBackgroundResource(R.drawable.bg_circle_padding_tran)
                test.setTextColor(Color.BLACK)
            } else {
                test.setTextColor(if (di.isClickable) Color.BLACK else Color.GRAY)
                root.setBackgroundColor(Color.WHITE)
            }
        }

        private fun setTodayColor() {
            test.setTextColor(Color.WHITE)
            root.setBackgroundResource(R.drawable.bg_circle_padding_blue)
        }

        private fun setHolidayColor() {
            test.setTextColor(Color.RED)
        }
    }

    companion object {
        private var lastCellPosition: Int = RecyclerView.NO_POSITION
        private const val EMPTY_STRING = ""
    }
}