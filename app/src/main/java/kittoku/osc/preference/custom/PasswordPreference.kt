package kittoku.osc.preference.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.Toast
import android.widget.EditText
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.Preference.SummaryProvider
import kittoku.osc.preference.OscPrefKey
import kittoku.osc.preference.accessor.getStringPrefValue

internal abstract class PasswordPreference(context: Context, attrs: AttributeSet) : OscEditTextPreference(context, attrs) {
    override val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    override val provider = SummaryProvider<Preference> {
        val currentValue = getStringPrefValue(oscPrefKey, it.sharedPreferences!!)

        if (currentValue.isEmpty()) {
            "[No Value Entered]"
        } else {
            "[Password Entered]"
        }
    }

    // Validate password before saving
    override fun setText(text: String?) {
        if (text != null && !isValidPassword(text)) {
            Toast.makeText(context, "Password must be at least 8 characters long and contain both letters and numbers.", Toast.LENGTH_LONG).show()
        } else {
            super.setText(text)
        }
    }

    // Function to check password validity
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    // Adds Eye Icon to Toggle Password Visibility
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val editText = holder.findViewById(android.R.id.edit) as? EditText
        editText?.let {
            val eyeIcon = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_menu_view)
                setOnClickListener {
                    val isPasswordVisible = editText.inputType != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    editText.inputType = if (isPasswordVisible) {
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    editText.setSelection(editText.text.length)
                }
            }

            // Add the eye icon to the EditText field
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeIcon.drawable, null)
        }
    }
}

internal class HomePasswordPreference(context: Context, attrs: AttributeSet) : PasswordPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.HOME_PASSWORD
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = "Password"
}

internal class ProxyPasswordPreference(context: Context, attrs: AttributeSet) : PasswordPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.PROXY_PASSWORD
    override val parentKey = OscPrefKey.PROXY_DO_USE_PROXY
    override val preferenceTitle = "Proxy Password (optional)"
}
