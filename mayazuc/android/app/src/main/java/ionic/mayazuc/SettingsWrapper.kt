package ionic.mayazuc

import androidx.preference.PreferenceManager

object SettingsWrapper {

    private val PreferenceStore = PreferenceManager.getDefaultSharedPreferences(MCApplication.context!!)

    fun IgnoreLeadingNumbersInFileNames(): Boolean {
        if (PreferenceStore != null)
            return PreferenceStore.getBoolean(
                MCApplication.context?.getString(R.string.ignore_leading_numbers_key),
                false
            )
        return false;
    }

    fun IgnoreLeadingNumbersInFileNames(value: Boolean): Boolean {
        if (PreferenceStore != null)
            PreferenceStore.edit().putBoolean(MCApplication.context?.getString(R.string.ignore_leading_numbers_key), value).commit()
        return value;
    }

    fun ShuffleEnabled(): Boolean {
        if (PreferenceStore != null)
            return PreferenceStore.getBoolean(
                MCApplication.context?.getString(R.string.shuffle_enabled),
                false
            )
        return false;
    }

    fun ShuffleEnabled(value: Boolean) {
        if (PreferenceStore != null)
            PreferenceStore.edit().putBoolean(MCApplication.context?.getString(R.string.shuffle_enabled),value).commit()
    }

    fun RepeatMode(): Int {
        if (PreferenceStore != null)
            return PreferenceStore.getInt(
                MCApplication.context?.getString(R.string.repeat_mode),
                0
            )
        return 0;
    }

    fun RepeatMode(value: Int) {
        if (PreferenceStore != null)
            PreferenceStore.edit().putInt(MCApplication.context?.getString(R.string.repeat_mode) ,value).commit()
    }

    fun SkipToQueueItemInExternalControllers(): Boolean {
        if (PreferenceStore != null)
            return PreferenceStore.getBoolean(
                MCApplication.context?.getString(R.string.skip_to_queue_item_in_external_controller),
                true
            )
        return true;
    }

    fun SkipToQueueItemInExternalControllers(value: Boolean): Boolean {
        if (PreferenceStore != null)
            PreferenceStore.edit().putBoolean(MCApplication.context?.getString(R.string.skip_to_queue_item_in_external_controller), value).commit()
        return value;
    }

    fun CategoryRootFolder(): String{
        if (PreferenceStore != null)
            return PreferenceStore.getString(
                MCApplication.context?.getString(R.string.RootCategoryFolder),
                ""
            )!!;
        return "";
    }

    fun CategoryRootFolder(value: String): String{
        if (PreferenceStore != null)
            PreferenceStore.edit().putString(MCApplication.context?.getString(R.string.RootCategoryFolder), value).commit()
        return value;
    }

    fun ShowHierarchyCommands(): Boolean{
        if (PreferenceStore != null)
            return PreferenceStore.getBoolean(
                MCApplication.context?.getString(R.string.show_hierarchy_commands_in_browser),
                false
            )!!;
        return false;
    }

    fun ShowHierarchyCommands(value: Boolean): Boolean{
        if (PreferenceStore != null)
            PreferenceStore.edit().putBoolean(MCApplication.context?.getString(R.string.show_hierarchy_commands_in_browser), value).commit()
        return value;
    }
}