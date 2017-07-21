package net.opmcorp.craftystates.task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Function;

public class RecursiveReplacer
{
	public static <T> void recursiveReplace(JSONObject toReplace, Class<T> clazz, Function<T, ?> replacer)
	{
		toReplace.keySet().forEach(key ->
		{
			if (toReplace.get(key) instanceof JSONObject)
				recursiveReplace(toReplace.getJSONObject(key), clazz, replacer);
			else if (toReplace.get(key) instanceof JSONArray)
				recursiveReplaceArray(toReplace.getJSONArray(key), clazz, replacer);
			if (clazz.isInstance(toReplace.get(key)))
				toReplace.put(key, replacer.apply((T) toReplace.get(key)));
		});
	}

	public static <T> void recursiveReplaceArray(JSONArray toReplace, Class<T> clazz, Function<T, ?> replacer)
	{
		for (int i = 0; i < toReplace.length(); i++)
		{
			Object object = toReplace.get(i);

			if (object instanceof JSONObject)
				recursiveReplace((JSONObject) object, clazz, replacer);
			else if (object instanceof JSONArray)
				recursiveReplaceArray((JSONArray) object, clazz, replacer);
			if (clazz.isInstance(object))
				toReplace.put(i, replacer.apply((T) toReplace.get(i)));
		}
	}
}
