package net.opmcorp.craftystates.task;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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

		if (jsonObject.getInt("craftystates_marker") > BLOCKSTATES_VERSION)
			throw new UnsupportedOperationException("This version of CraftyStates cannot transpile a file with the marker " + jsonObject.getInt("craftystates_marker"));

		jsonObject.append("forge_marker", 1);

		JSONObject defaults = jsonObject.getJSONObject("defaults");

		if (!defaults.has("textures"))
			defaults.append("textures", null);
		if (!defaults.has("transform"))
			defaults.append("transform", "forge:default-block");

		if (jsonObject.getJSONObject("variants").has("matcher"))
			this.convertMatcher(jsonObject, jsonObject.getJSONObject("variants").getJSONObject("matcher"), defaults);
	}

	private void convertMatcher(JSONObject jsonObject, JSONObject matcher, JSONObject defaults)
	{
		Map<String, JSONArray> properties = matcher.keySet().stream().filter(key -> !key.equals("values"))
				.collect(Collectors.toMap(key -> key, matcher::getJSONArray));


		List<List<String>> beforeCartesian = new ArrayList<>();

		properties.keySet().forEach(key -> beforeCartesian.add(properties.get(key).toList().stream().map(value -> key + "=" + value).collect(Collectors.toList())));

		List<List<String>> cartesianProduct = Lists.cartesianProduct(beforeCartesian);

		cartesianProduct = Lists.newArrayList(cartesianProduct.stream().map(Lists::newArrayList).collect(Collectors.toList()));
		cartesianProduct.forEach(Collections::reverse);
	}
}
