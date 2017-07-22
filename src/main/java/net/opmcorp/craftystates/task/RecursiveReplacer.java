package net.opmcorp.craftystates.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class RecursiveReplacer
{
	public static void recursiveReplaceString(JsonObject toReplace, Function<String, JsonElement> replacer)
	{
		toReplace.entrySet().forEach(entry ->
		{
			if (entry.getValue().isJsonObject())
				recursiveReplaceString(entry.getValue().getAsJsonObject(), replacer);
			else if (entry.getValue().isJsonArray())
				recursiveReplaceStringInArray(entry.getValue().getAsJsonArray(), replacer);
			else if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString())
				toReplace.add(entry.getKey(), replacer.apply(entry.getValue().getAsString()));
		});
	}

	public static void recursiveReplaceStringInArray(JsonArray toReplace, Function<String, JsonElement> replacer)
	{
		for (int i = 0; i < toReplace.size(); i++)
		{
			JsonElement element = toReplace.get(i);

			if (element.isJsonObject())
				recursiveReplaceString(element.getAsJsonObject(), replacer);
			else if (element.isJsonArray())
				recursiveReplaceStringInArray(element.getAsJsonArray(), replacer);
			else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
				toReplace.set(i, replacer.apply(element.getAsString()));
		}
	}
}
