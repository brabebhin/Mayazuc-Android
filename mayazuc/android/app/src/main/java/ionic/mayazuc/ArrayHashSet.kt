package ionic.mayazuc

class ArrayHashSet<T> : ArrayList<T>() {
    private val Items = ArrayList<T>();
    private val Set = HashSet<T>();

    override fun add(element: T): Boolean {
        if (shouldAddItem(element))
            return super.add(element)

        return false;
    }

    override fun add(index: Int, element: T) {
        if (shouldAddItem(element))
            super.add(index, element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var returnValue = true;
        elements.forEach {
            returnValue = returnValue && this.add(it);
        }

        return returnValue;
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        elements.reversed().forEach {
            this.add(index, it);
        };
        return true;
    }

    private fun shouldAddItem(element: T): Boolean {
        return Set.add(element);
    }
}