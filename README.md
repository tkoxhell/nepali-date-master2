# Nepali Date Picker

A customizable Nepali date picker library for Android applications.

## Features

- Support for both Nepali (BS) and English (AD) calendar systems
- Customizable colors and themes
- **Future date restriction** - Disable selection of future dates from today
- Holiday highlighting
- Responsive design

## Usage

### Basic Usage

```kotlin
Pal.Builder(fragmentManager)
    .fromNepali(2080, 1, 1) // Start from specific Nepali date
    .withCallback(object : DatePickCompleteListener {
        override fun onDateSelectionComplete(result: GhadiResult) {
            // Handle date selection
            Log.d("Date", "Selected: ${result.humanReadableBs}")
        }
    })
    .build()
    .show()
```

### With Future Date Restriction

```kotlin
Pal.Builder(fragmentManager)
    .fromNepali(2080, 1, 1)
    .setDisableFutureDates(true) // Disable future dates (default: true)
    .withCallback(object : DatePickCompleteListener {
        override fun onDateSelectionComplete(result: GhadiResult) {
            // Handle date selection
        }
    })
    .build()
    .show()
```

### Customize Colors

```kotlin
Pal.Builder(fragmentManager)
    .fromNepali(2080, 1, 1)
    .setBackgroundColor(Color.BLUE)
    .setForegroundColor(Color.WHITE)
    .setDisableFutureDates(true)
    .withCallback(datePickCompleteListener)
    .build()
    .show()
```

## Future Date Restriction

The library now includes a feature to disable future date selections:

- **Enabled by default**: Future dates are automatically disabled
- **Configurable**: Use `setDisableFutureDates(false)` to allow future date selection
- **Visual feedback**: Disabled dates are shown with a grayed-out appearance
- **Both calendars**: Works with both Nepali (BS) and English (AD) calendar systems

### How it works

1. **Date comparison**: The library compares each date with today's date
2. **Clickable state**: Future dates have `isClickable` set to `false`
3. **Visual styling**: Disabled dates are styled with gray text and background
4. **User feedback**: An informational text shows "Future dates are disabled"

## Installation

Add the dependency to your `build.gradle` file:

```gradle
implementation 'np.com.naveenniraula:ghadi:1.0.0'
```

## License

This project is licensed under the MIT License.
