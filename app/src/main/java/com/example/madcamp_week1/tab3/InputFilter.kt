import android.text.InputFilter
import android.text.Spanned

class InputFilterMaxLength(private val maxLength: Int) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val keep = maxLength - (dest?.length ?: 0)

        if (keep <= 0) {
            // Display warning message when maximum length is reached
            // You can handle this as per your UI requirements
            // For simplicity, returning an empty string to prevent further input
            return ""
        } else if (keep >= end - start) {
            // Accept original input within the limit
            return null
        } else {
            // Trim the input to the maximum length
            return source?.subSequence(start, start + keep)
        }
    }
}
