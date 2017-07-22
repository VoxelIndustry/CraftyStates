package net.opmcorp.craftystates.task;

import com.google.common.collect.Lists;
import com.google.gson.*;
import lombok.Setter;
import net.opmcorp.craftystates.CraftyStates;
import net.opmcorp.craftystates.CraftyStatesExtension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
public class BlockStateTranspiler
{
	public static final int BLOCKSTATES_VERSION = 1;

	private CraftyStatesExtension extension;

	private Gson gson = null;

	private Gson getGson()
	{
		if (gson == null)
		{
			if (extension.isPrettyPrinting())
				gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			else
				gson = new Gson();
		}
		return gson;
	}


	public void transpileBlockState(File file)
	{
		String json = "";
		try
		{
			InputStream inputStream = new FileInputStream(file);

			json = IOUtils.toString(inputStream);

			inputStream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

		if (!jsonObject.has("craftystates_marker"))
		{
			CraftyStates.getLogger().error("Blockstate [{}] does not contains the key craftystates_marker, it will not be processed.", file.getName());
			return;
		}

		if (jsonObject.get("craftystates_marker").getAsInt() > BLOCKSTATES_VERSION)
			throw new UnsupportedOperationException("This version of CraftyStates cannot transpile a file with the marker " + jsonObject.get("craftystates_marker").getAsInt());

		jsonObject.addProperty("forge_marker", 1);

		JsonObject defaults = jsonObject.get("defaults").getAsJsonObject();

		if (!defaults.has("textures"))
			defaults.add("textures", new JsonObject());
		if (!defaults.has("transform"))
			defaults.addProperty("transform", "forge:default-block");

		if (jsonObject.get("variants").getAsJsonObject().has("matcher"))
			this.convertMatcher(jsonObject, jsonObject.get("variants").getAsJsonObject().get("matcher").getAsJsonObject(), defaults);
		jsonObject.remove("craftystates_marker");

		this.replaceTextures(jsonObject);

		try
		{
			File dest = new File(file.getAbsolutePath().replace(".cs.json", ".json"));

			if (!dest.getName().equals(file.getName()))
				FileUtils.writeByteArrayToFile(dest,
						gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
			else
				CraftyStates.getLogger().warn("Blockstate [{}] was about to be erased, exported name [{}] is the same as" +
						" the source. Nothing will be written.", file.getName(), dest.getName());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void convertMatcher(JsonObject jsonObject, JsonObject matcher, JsonObject defaults)
	{
		List<List<String>> properties = Lists.cartesianProduct(matcher.entrySet().stream().filter(entry -> !entry.getKey().equals("values"))
				.map(entry -> ((ArrayList<String>) this.getGson().fromJson(matcher.get(entry.getKey()).getAsJsonArray().toString(), ArrayList.class))
						.stream().map(value -> entry.getKey() + "=" + value).collect(Collectors.toList())).collect(Collectors.toList()));

		JsonObject values = matcher.get("values").getAsJsonObject();

		for (List<String> property : properties)
		{
			JsonObject value = new JsonObject();

			if (!values.has("model"))
				value.addProperty("model", defaults.get("model").getAsString());

			for (Map.Entry<String, JsonElement> p : values.entrySet())
				value.add(p.getKey(), p.getValue());

			Map<String, String> propertyValues = new HashMap<>();
			property.forEach(p -> propertyValues.put(p.split("=")[0], p.split("=")[1]));
			RecursiveReplacer.recursiveReplaceString(value, toReplace ->
			{
				String rtn = toReplace;

				for (Map.Entry<String, String> p : propertyValues.entrySet())
				{
					if (rtn.contains("#" + p.getKey()))
						rtn = rtn.replace("#" + p.getKey(), p.getValue());
				}
				return getGson().toJsonTree(rtn);
			});

			jsonObject.get("variants").getAsJsonObject().add(property.stream().collect(Collectors.joining(",")), value);
		}

		jsonObject.get("variants").getAsJsonObject().remove("matcher");
	}

	private void replaceTextures(JsonObject toReplace)
	{
		List<Map.Entry<String, JsonElement>> keys = Lists.newArrayList(toReplace.entrySet());
		keys.forEach(entry ->
		{
			if (entry.getValue().isJsonObject())
				replaceTextures(entry.getValue().getAsJsonObject());
			else if (entry.getValue().isJsonArray())
				replaceTexturesArray(entry.getValue().getAsJsonArray());
			else if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString())
			{
				if (entry.getKey().startsWith("textures#"))
					CraftyStates.getLogger().warn("Found a malformed texture pattern [{} -> {}], it will not be processed.",
							entry.getKey(), entry.getKey().replace("textures#", "texture#"));
				if (entry.getKey().startsWith("texture#"))
				{
					if (!toReplace.has("textures"))
						toReplace.add("textures", new JsonObject());
					toReplace.get("textures").getAsJsonObject().add(entry.getKey().replace("texture#", ""), entry.getValue());
					toReplace.remove(entry.getKey());
				}
			}
		});
	}

	private void replaceTexturesArray(JsonArray toReplace)
	{
		for (int i = 0; i < toReplace.size(); i++)
		{
			Object object = toReplace.get(i);

			if (object instanceof JsonObject)
				replaceTextures((JsonObject) object);
			else if (object instanceof JsonArray)
				replaceTexturesArray((JsonArray) object);
		}
	}
}
