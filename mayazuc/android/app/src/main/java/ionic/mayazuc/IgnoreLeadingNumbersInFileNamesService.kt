package ionic.mayazuc

object IgnoreLeadingNumbersInFileNamesService {
    private val trimRegex = Regex("^[0-9- _]*");

    fun GetFileNameTrimmedOrDefault(filename: String): String {
        if (SettingsWrapper.IgnoreLeadingNumbersInFileNames()) {
            return RemoveLeadingNumbersInFileName(filename)
        }
        return filename;
    }

   private fun RemoveLeadingNumbersInFileName(filename: String): String {
        return trimRegex.replace(filename, "");
    }
}