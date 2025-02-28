package kittoku.osc.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.forEach
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kittoku.osc.BuildConfig
import kittoku.osc.R
import kittoku.osc.databinding.ActivityMainBinding
import kittoku.osc.extension.firstEditText
import kittoku.osc.extension.sum
import kittoku.osc.fragment.HomeFragment
import kittoku.osc.fragment.SettingFragment
import kittoku.osc.preference.OscPrefKey
import kittoku.osc.preference.PROFILE_KEY_HEADER
import kittoku.osc.preference.accessor.getStringPrefValue
import kittoku.osc.preference.custom.OscPreference
import kittoku.osc.preference.exportProfile
import kittoku.osc.preference.importProfile


class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    private lateinit var homeFragment: PreferenceFragmentCompat
    private lateinit var settingFragment: PreferenceFragmentCompat

    private val dialogResource: Int by lazy { EditTextPreference(this).dialogLayoutResource }

    private val profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@registerForActivityResult
        }

        updatePreferenceView()
    }

    private fun updatePreferenceView() {
        listOf(homeFragment, settingFragment).forEach { fragment ->
            if (fragment.isAdded) {
                val preferenceGroups = mutableListOf<PreferenceGroup>(fragment.preferenceScreen)

                while (preferenceGroups.isNotEmpty()) {
                    preferenceGroups.removeFirst().forEach {
                        if (it is OscPreference) {
                            it.updateView()
                        }

                        if (it is PreferenceGroup) {
                            preferenceGroups.add(it)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)  // Ensure this is the correct menu file name
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Fix: Initialize `prefs` at the very beginning
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        title = "${getString(R.string.app_name)}: ${BuildConfig.VERSION_NAME}"
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Fix: Use prefs AFTER initialization
        val isDarkModeEnabled = prefs.getBoolean("dark_mode", false)
        val mode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        homeFragment = HomeFragment()
        settingFragment = SettingFragment()

        object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> homeFragment
                    1 -> settingFragment
                    else -> throw NotImplementedError(position.toString())
                }
            }
        }.also {
            binding.pager.adapter = it
        }

        TabLayoutMediator(binding.tabBar, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "HOME"
                1 -> "SETTING"
                else -> throw NotImplementedError(position.toString())
            }
        }.attach()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.load_profile -> {
                profileLauncher.launch(Intent(this, BlankActivity::class.java).putExtra(
                    EXTRA_KEY_TYPE,
                    BLANK_ACTIVITY_TYPE_PROFILES
                ))
            }

            R.id.save_profile -> showSaveDialog()

            R.id.reload_defaults -> showReloadDialog()

            // New Help Option
            R.id.action_help -> {
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }


    private fun showSaveDialog() {
        val inflated = layoutInflater.inflate(dialogResource, null)
        val editText = inflated.firstEditText()

        val hostname = getStringPrefValue(OscPrefKey.HOME_HOSTNAME, prefs)

        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.hint = hostname
        editText.requestFocus()

        AlertDialog.Builder(this).also {
            it.setView(inflated)
            it.setMessage(sum(
                "Enter the profile's name.\n",
                "If blank, the hostname will be used.\n",
                "If duplicated, the profile will be overwritten."
            ))

            it.setPositiveButton("SAVE") { _, _ ->
                prefs.edit().also { editor ->
                    editor.putString(
                        PROFILE_KEY_HEADER + editText.text.ifEmpty { hostname },
                        exportProfile(prefs)
                    )
                    editor.apply()
                }

                Toast.makeText(this, "PROFILE SAVED", Toast.LENGTH_SHORT).show()
            }

            it.setNegativeButton("CANCEL") { _, _ -> }

            it.show()
        }
    }

    private fun showReloadDialog() {
        AlertDialog.Builder(this).also {
            it.setMessage("Are you sure to reload the default settings?")

            it.setPositiveButton("YES") { _, _ ->
                importProfile(null, prefs)

                updatePreferenceView()

                Toast.makeText(this, "DEFAULTS RELOADED", Toast.LENGTH_SHORT).show()
            }

            it.setNegativeButton("NO") { _, _ -> }

            it.show()
        }
    }
}
