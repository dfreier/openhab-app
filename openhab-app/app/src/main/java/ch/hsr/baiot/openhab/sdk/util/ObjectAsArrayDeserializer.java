package ch.hsr.baiot.openhab.sdk.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * Created by dominik on 13.05.15.
 */
public class ObjectAsArrayDeserializer<T> implements JsonDeserializer<T[]> {


    public ObjectAsArrayDeserializer(Class<T> aClass) {
        mClass = aClass;
    }

    private Class<T> mClass;

    @Override
    public T[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = toJsonArray(json);

        T[] elements = (T[]) Array.newInstance(mClass, jsonArray.size());
        for(int i = 0; i < jsonArray.size(); i++) {
            elements[i] = context.deserialize(jsonArray.get(i), mClass);
        }
        return elements;
    }

    private JsonArray toJsonArray(JsonElement json) {
        JsonArray jsonArray;
        if(json.isJsonArray()) {
            jsonArray = json.getAsJsonArray();
        } else {
            jsonArray = new JsonArray();
            jsonArray.add(json);
        }
        return jsonArray;
    }
}
