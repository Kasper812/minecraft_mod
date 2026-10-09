package com.atsuishio.portlib.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

public class IdentifierSerializer implements JsonDeserializer<Identifier>, JsonSerializer<Identifier> {
    @Override
    public Identifier deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Identifier.parse(GsonHelper.convertToString(json, "location"));
    }

    @Override
    public JsonElement serialize(Identifier src, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
