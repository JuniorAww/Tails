package me.junioraww.tails.data.types.animation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import org.joml.Matrix4f;

import java.lang.reflect.Type;

public class Matrix4fDeserializer implements JsonDeserializer<Matrix4f> {
    @Override
    public Matrix4f deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
        var array = element.getAsJsonArray();
        return new Matrix4f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat(),
                array.get(3).getAsFloat(),
                array.get(4).getAsFloat(),
                array.get(5).getAsFloat(),
                array.get(6).getAsFloat(),
                array.get(7).getAsFloat(),
                array.get(8).getAsFloat(),
                array.get(9).getAsFloat(),
                array.get(10).getAsFloat(),
                array.get(11).getAsFloat(),
                array.get(12).getAsFloat(),
                array.get(13).getAsFloat(),
                array.get(14).getAsFloat(),
                array.get(15).getAsFloat()
                );
    }
}
