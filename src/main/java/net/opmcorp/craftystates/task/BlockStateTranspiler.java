package net.opmcorp.craftystates.task;

import com.google.common.collect.Lists;
import net.opmcorp.craftystates.CraftyStates;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockStateTranspiler
{
	public static final int BLOCKSTATES_VERSION = 1;

	public void transpileBlockState(File file)
	{
		String json = "";
		try
		{
			InputStream inputStream = new FileInputStream(file);

			json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

			inputStream.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		JSONObject jsonObject = new JSONObject(json);

		if (!jsonObject.has("craftystates_marker"))
		{
			CraftyStates.getLogger().error("Blockstate [{}] does not contains the key craftystates_marker, it will not be processed.", file.getName());
			return;
		}

		if (jsonObject.getInt("craftystates_marker") > BLOCKSTATES_VERSION)
			throw new UnsupportedOperationException("This version of CraftyStates cannot transpile a file with the marker " + jsonObject.getInt("craftystates_marker"));

		jsonObject.put("forge_marker", 1);

		JSONObject defaults = jsonObject.getJSONObject("defaults");

		if (!defaults.has("textures"))
			defaults.put("textures", new JSONObject());
		if (!defaults.has("transform"))
			defaults.put("transform", "forge:default-block");

		if (jsonObject.getJSONObject("variants").has("matcher"))
			this.convertMatcher(jsonObject, jsonObject.getJSONObject("variants").getJSONObject("matcher"), defaults);
		jsonObject.remove("craftystates_marker");

		this.replaceTextures(jsonObject);

		try
		{
			File dest = new File(file.getAbsolutePath().replace(".cs.json", ".json"));

			if (!dest.getName().equals(file.getName()))
				FileUtils.write(dest,
						jsonObject.toString(), StandardCharsets.UTF_8);
			else
				CraftyStates.getLogger().warn("Blockstate [{}] was about to be erased, exported name [{}] is the same as" +
						" the source. Nothing will be written.", file.getName(), dest.getName());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void convertMatcher(JSONObject jsonObject, JSONObject matcher, JSONObject defaults)
	{
		List<List<String>> properties = Lists.cartesianProduct(matcher.keySet().stream().filter(key -> !key.equals("values"))
				.map(key -> matcher.getJSONArray(key).toList().stream().map(value -> key + "=" + value)
						.collect(Collectors.toList())).collect(Collectors.toList()));

		properties = Lists.newArrayList(properties.stream().map(Lists::newArrayList).collect(Collectors.toList()));
		properties.forEach(Collections::reverse);

		JSONObject values = matcher.getJSONObject("values");

		for (List<String> property : properties)
		{
			JSONObject value = new JSONObject(values.toString());

			Map<String, String> propertyValues = new HashMap<>();
			property.forEach(p -> propertyValues.put(p.split("=")[0], p.split("=")[1]));
			RecursiveReplacer.recursiveReplace(value, String.class, toReplace ->
			{
				String rtn = toReplace;

				for (Map.Entry<String, String> p : propertyValues.entrySet())
				{
					if (rtn.contains("#" + p.getKey()))
						rtn = rtn.replace("#" + p.getKey(), p.getValue());
				}
				return rtn;
			});

			if (!values.has("model"))
				value.put("model", defaults.getString("model"));
			jsonObject.getJSONObject("variants").put(property.stream().collect(Collectors.joining(",")), value);
		}

		jsonObject.getJSONObject("variants").remove("matcher");
	}

	private void replaceTextures(JSONObject toReplace)
	{
		List<String> keys = Lists.newArrayList(toReplace.keys());
		keys.forEach(key ->
		{
			if (toReplace.get(key) instanceof JSONObject)
				replaceTextures(toReplace.getJSONObject(key));
			else if (toReplace.get(key) instanceof JSONArray)
				replaceTexturesArray(toReplace.getJSONArray(key));
			else if (toReplace.get(key) instanceof String)
			{
				if (key.startsWith("textures#"))
					CraftyStates.getLogger().warn("Found a malformed texture pattern [{} -> {}], it will not be processed.",
							key, key.replace("textures#", "texture#"));
				if (key.startsWith("texture#"))
				{
					if (!toReplace.has("textures"))
						toReplace.put("textures", new JSONObject());
					toReplace.getJSONObject("textures").put(key.replace("texture#", ""), (String) toReplace.get(key));
					toReplace.remove(key);
				}
			}
		});
	}

	private void replaceTexturesArray(JSONArray toReplace)
	{
		for (int i = 0; i < toReplace.length(); i++)
		{
			Object object = toReplace.get(i);

			if (object instanceof JSONObject)
				replaceTextures((JSONObject) object);
			else if (object instanceof JSONArray)
				replaceTexturesArray((JSONArray) object);
		}
	}
}
