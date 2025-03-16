package com.mybenru.app.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybenru.app.R
import com.mybenru.app.databinding.FragmentSettingsBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.utils.show
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.SettingsViewModel
import com.mybenru.domain.model.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment for app settings
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // Activity result launchers for file operations
    private val backupFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.backupData(uri)
            }
        }
    }

    private val restoreFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.restoreData(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        // Theme settings
        binding.cardTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNotifications(isChecked)
        }

        // Automatic updates
        binding.switchAutomaticUpdates.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleAutomaticUpdates(isChecked)
        }

        // Language settings
        binding.cardLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Clear cache
        binding.btnClearCache.setOnClickListener {
            confirmClearCache()
        }

        // Backup data
        binding.btnBackup.setOnClickListener {
            startBackupProcess()
        }

        // Restore data
        binding.btnRestore.setOnClickListener {
            startRestoreProcess()
        }

        // Reset backup/restore state
        binding.btnCancelBackupRestore.setOnClickListener {
            viewModel.resetBackupRestoreState()
        }

        // About app
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        // Help and feedback
        binding.cardHelp.setOnClickListener {
            showHelpDialog()
        }

        // Privacy policy
        binding.cardPrivacy.setOnClickListener {
            openPrivacyPolicy()
        }

        // Terms of service
        binding.cardTerms.setOnClickListener {
            openTermsOfService()
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SettingsViewModel.SettingsUiState.Loading -> {
                        binding.progressBar.show()
                    }
                    is SettingsViewModel.SettingsUiState.Success -> {
                        binding.progressBar.hide()
                    }
                    is SettingsViewModel.SettingsUiState.Error -> {
                        binding.progressBar.hide()
                        binding.root.showSnackbar(state.message)
                    }
                }
            }
        }

        // Observe app settings
        viewModel.appSettings.observeWithLifecycle(viewLifecycleOwner) { settings ->
            updateSettingsUI(settings)
        }

        // Observe cache size
        viewModel.cacheSize.observeWithLifecycle(viewLifecycleOwner) { cacheSize ->
            binding.txtCacheSize.text = getString(R.string.cache_size_value, cacheSize)
        }

        // Observe backup/restore state
        viewModel.backupRestoreState.observeWithLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsViewModel.BackupRestoreState.Idle -> {
                    binding.layoutBackupRestoreProgress.visibility = View.GONE
                    binding.layoutBackupRestore.visibility = View.VISIBLE
                    binding.progressBarOperation.progress = 0
                }
                is SettingsViewModel.BackupRestoreState.InProgress -> {
                    binding.layoutBackupRestoreProgress.visibility = View.VISIBLE
                    binding.layoutBackupRestore.visibility = View.GONE
                    binding.txtOperationStatus.text = state.message
                }
                is SettingsViewModel.BackupRestoreState.Success -> {
                    binding.layoutBackupRestoreProgress.visibility = View.GONE
                    binding.layoutBackupRestore.visibility = View.VISIBLE
                    binding.root.showSnackbar(state.message)
                    binding.progressBarOperation.progress = 100
                }
                is SettingsViewModel.BackupRestoreState.Error -> {
                    binding.layoutBackupRestoreProgress.visibility = View.GONE
                    binding.layoutBackupRestore.visibility = View.VISIBLE
                    binding.root.showSnackbar(state.message)
                    binding.progressBarOperation.progress = 0
                }
            }
        }

        // Observe operation progress
        viewModel.operationProgress.observeWithLifecycle(viewLifecycleOwner) { progress ->
            binding.progressBarOperation.progress = progress
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                binding.root.showSnackbar(error)
                Timber.e("Error in SettingsFragment: $error")
            }
        }
    }

    private fun updateSettingsUI(settings: AppSettings) {
        // Update theme info
        binding.txtCurrentTheme.text = when (settings.themeMode) {
            "light" -> getString(R.string.theme_light)
            "dark" -> getString(R.string.theme_dark)
            "system" -> getString(R.string.theme_system)
            else -> getString(R.string.theme_system)
        }

        // Update notification switch
        binding.switchNotifications.isChecked = settings.notificationsEnabled

        // Update automatic updates switch
        binding.switchAutomaticUpdates.isChecked = settings.automaticUpdates

        // Update language info
        binding.txtCurrentLanguage.text = when (settings.preferredLanguage) {
            "en" -> "English"
            "fr" -> "Français"
            "es" -> "Español"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "pt" -> "Português"
            "ru" -> "Русский"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "zh" -> "中文"
            "system" -> getString(R.string.language_system)
            else -> getString(R.string.language_system)
        }

        // Update app version
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        binding.txtAppVersion.text = getString(
            R.string.app_version,
            packageInfo.versionName,
            packageInfo.versionCode
        )
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        val currentThemeMode = viewModel.appSettings.value?.themeMode ?: "system"
        val currentThemeIndex = when (currentThemeMode) {
            "light" -> 0
            "dark" -> 1
            else -> 2 // system
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_theme)
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedThemeMode = when (which) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "system"
                }

                // Update theme
                viewModel.updateTheme(selectedThemeMode)

                // Apply theme
                applyTheme(selectedThemeMode)

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun applyTheme(themeMode: String) {
        val nightMode = when (themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            "English",
            "Français",
            "Español",
            "Deutsch",
            "Italiano",
            "Português",
            "Русский",
            "日本語",
            "한국어",
            "中文",
            getString(R.string.language_system)
        )

        val languageCodes = arrayOf(
            "en",
            "fr",
            "es",
            "de",
            "it",
            "pt",
            "ru",
            "ja",
            "ko",
            "zh",
            "system"
        )

        val currentLanguage = viewModel.appSettings.value?.preferredLanguage ?: "system"
        val currentLanguageIndex = languageCodes.indexOf(currentLanguage).takeIf { it >= 0 } ?: 10 // default to system

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]

                // Update language
                viewModel.setPreferredLanguage(selectedLanguage)

                // Show restart recommendation
                binding.root.showSnackbar(getString(R.string.language_change_restart))

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmClearCache() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_cache)
            .setMessage(R.string.clear_cache_confirmation)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearCache()
                binding.root.showSnackbar(getString(R.string.cache_cleared))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startBackupProcess() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, "mybenru_backup_${System.currentTimeMillis()}.zip")
        }

        try {
            backupFilePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error launching file picker")
            binding.root.showSnackbar(getString(R.string.file_picker_error))
        }
    }

    private fun startRestoreProcess() {
        // Show warning dialog first
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.restore_data)
            .setMessage(R.string.restore_warning)
            .setPositiveButton(R.string.continue_text) { _, _ ->
                // Launch file picker for restore
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/zip"
                }

                try {
                    restoreFilePickerLauncher.launch(intent)
                } catch (e: Exception) {
                    Timber.e(e, "Error launching file picker")
                    binding.root.showSnackbar(getString(R.string.file_picker_error))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAboutDialog() {
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about_app)
            .setMessage(getString(
                R.string.about_app_message,
                getString(R.string.app_name),
                packageInfo.versionName,
                packageInfo.versionCode
            ))
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showHelpDialog() {
        val options = arrayOf(
            getString(R.string.help_documentation),
            getString(R.string.report_bug),
            getString(R.string.send_feedback)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.help_and_feedback)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openDocumentation()
                    1 -> reportBug()
                    2 -> sendFeedback()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openDocumentation() {
        openUrl("https://mybenru.com/docs")
    }

    private fun reportBug() {
        openUrl("https://mybenru.com/report-bug")
    }

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@mybenru.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for MyBenru App")
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_feedback)))
        } catch (e: Exception) {
            Timber.e(e, "Error launching email client")
            binding.root.showToast(getString(R.string.no_email_app))
        }
    }

    private fun openPrivacyPolicy() {
        openUrl("https://mybenru.com/privacy")
    }

    private fun openTermsOfService() {
        openUrl("https://mybenru.com/terms")
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening URL: $url")
            binding.root.showSnackbar(getString(R.string.browser_error))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}