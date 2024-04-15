package ionic.mayazuc

import java.io.File

class FileNameComparator : Comparator<File> {
    override fun compare(o1: File?, o2: File?): Int {
        if (o1 == null || o2 == null) return 0;

        return IgnoreLeadingNumbersInFileNamesService.GetFileNameTrimmedOrDefault(o1.name)
            .compareTo(IgnoreLeadingNumbersInFileNamesService.GetFileNameTrimmedOrDefault(o2.name))
    }
}