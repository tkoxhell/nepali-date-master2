# Usage Examples

## Basic Implementation

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        Pal.Builder(supportFragmentManager)
            .fromNepali(2080, 1, 1) // Start from Magh 1, 2080 BS
            .setDisableFutureDates(true) // Disable future dates
            .withCallback(object : DatePickCompleteListener {
                override fun onDateSelectionComplete(result: GhadiResult) {
                    // Handle successful date selection
                    val selectedDate = result.humanReadableBs
                    val selectedDateAd = result.humanReadableAd
                    
                    Toast.makeText(
                        this@MainActivity,
                        "Selected: $selectedDate ($selectedDateAd)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            .build()
            .show()
    }
}
```

## Advanced Configuration

```kotlin
private fun showCustomDatePicker() {
    Pal.Builder(supportFragmentManager)
        .fromEnglish(2024, 1, 15) // Start from January 15, 2024 AD
        .setBackgroundColor(Color.parseColor("#2196F3")) // Blue background
        .setForegroundColor(Color.WHITE) // White text
        .setDisableFutureDates(true) // Disable future dates
        .withCallback(object : DatePickCompleteListener {
            override fun onDateSelectionComplete(result: GhadiResult) {
                handleDateSelection(result)
            }
        })
        .build()
        .show()
}

private fun handleDateSelection(result: GhadiResult) {
    // Access various date formats
    val nepaliDate = "${result.year}-${result.month}-${result.date}"
    val englishDate = "${result.adYear}-${result.adMonth}-${result.adDate}"
    val dayName = result.dayName
    val weekDayNumber = result.weekDayNumber
    
    Log.d("DatePicker", "Nepali: $nepaliDate")
    Log.d("DatePicker", "English: $englishDate")
    Log.d("DatePicker", "Day: $dayName (Week day: $weekDayNumber)")
}
```

## Conditional Future Date Restriction

```kotlin
private fun showDatePickerWithCondition() {
    val shouldDisableFutureDates = checkUserPermission() // Your logic here
    
    Pal.Builder(supportFragmentManager)
        .fromNepali(2080, 1, 1)
        .setDisableFutureDates(shouldDisableFutureDates)
        .withCallback(datePickCompleteListener)
        .build()
        .show()
}

private fun checkUserPermission(): Boolean {
    // Example: Only admin users can select future dates
    return !isAdminUser()
}
```

## Handling Different Calendar Systems

```kotlin
private fun showDatePickerForCalendar(useNepaliCalendar: Boolean) {
    val builder = Pal.Builder(supportFragmentManager)
        .setDisableFutureDates(true)
        .withCallback(datePickCompleteListener)
    
    if (useNepaliCalendar) {
        builder.fromNepali(2080, 1, 1)
    } else {
        builder.fromEnglish(2024, 1, 15)
    }
    
    builder.build().show()
}
```

## Date Validation

```kotlin
private fun validateSelectedDate(result: GhadiResult): Boolean {
    // Check if selected date is valid for your use case
    val selectedYear = result.year
    val selectedMonth = result.month
    val selectedDay = result.date
    
    // Example: Only allow dates from 2075 BS onwards
    if (selectedYear < 2075) {
        Toast.makeText(this, "Please select a date from 2075 BS onwards", Toast.LENGTH_SHORT).show()
        return false
    }
    
    // Example: Only allow dates from current month onwards
    val currentDate = Date(Calendar.getInstance()).convertToNepali()
    if (selectedYear == currentDate.year && selectedMonth < currentDate.month) {
        Toast.makeText(this, "Please select a date from current month onwards", Toast.LENGTH_SHORT).show()
        return false
    }
    
    return true
}
```

## Error Handling

```kotlin
private fun showDatePickerWithErrorHandling() {
    try {
        Pal.Builder(supportFragmentManager)
            .fromNepali(2080, 1, 1)
            .setDisableFutureDates(true)
            .withCallback(object : DatePickCompleteListener {
                override fun onDateSelectionComplete(result: GhadiResult) {
                    try {
                        // Validate the selected date
                        if (validateSelectedDate(result)) {
                            processSelectedDate(result)
                        }
                    } catch (e: Exception) {
                        Log.e("DatePicker", "Error processing date: ${e.message}")
                        showErrorMessage("Error processing selected date")
                    }
                }
            })
            .build()
            .show()
    } catch (e: Exception) {
        Log.e("DatePicker", "Error showing date picker: ${e.message}")
        showErrorMessage("Error showing date picker")
    }
}

private fun showErrorMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
```

## Integration with ViewModel

```kotlin
class DatePickerViewModel : ViewModel() {
    
    private val _selectedDate = MutableLiveData<GhadiResult>()
    val selectedDate: LiveData<GhadiResult> = _selectedDate
    
    fun showDatePicker(fragmentManager: FragmentManager) {
        Pal.Builder(fragmentManager)
            .fromNepali(2080, 1, 1)
            .setDisableFutureDates(true)
            .withCallback(object : DatePickCompleteListener {
                override fun onDateSelectionComplete(result: GhadiResult) {
                    _selectedDate.value = result
                }
            })
            .build()
            .show()
    }
}
```

## Notes

- **Future date restriction is enabled by default** (`setDisableFutureDates(true)`)
- **Disabled dates are visually distinct** with gray text and background
- **Works with both calendar systems** (Nepali BS and English AD)
- **Informational text** shows when future dates are disabled
- **Customizable** - you can enable/disable the feature as needed
- **Performance optimized** - date comparison is done efficiently
