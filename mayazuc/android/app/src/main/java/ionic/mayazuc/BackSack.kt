package ionic.mayazuc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class BackSack {
    val backstack: ArrayDeque<String> = ArrayDeque<String>();

    fun Push(item: String) {
        backstack.push(item);
    }

    fun Pop(): String? {
        if (backstack.isEmpty()) return null
        return backstack.pop()
    }

    fun SaveToJson(): String {

        val jsonObject = Gson().toJson(backstack);
        return jsonObject;
    }

    fun LoadFromJson(jsonObject: String) {
        if (jsonObject == null) return

        val listOfMyClassObject: Type = object : TypeToken<ArrayList<String>>() {}.getType()

        val items = Gson().fromJson<ArrayList<String>>(jsonObject, listOfMyClassObject);
        backstack.clear()
        backstack.addAll(items);
    }
}