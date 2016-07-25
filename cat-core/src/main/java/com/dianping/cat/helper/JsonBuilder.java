package com.dianping.cat.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonBuilder {
	private FieldNamingStrategy m_fieldNamingStrategy = new FieldNamingStrategy() {
		@Override
		public String translateName(Field f) {
			String name = f.getName();

			if (name.startsWith("m_")) {
				return name.substring(2);
			} else {
				return name;
			}
		}
	};

	private Gson m_gson = new GsonBuilder()//
	      .registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter()) //
	      .registerTypeAdapter(Double.TYPE, new DoubleTypeAdapter()) //
	      .setDateFormat("yyyy-MM-dd HH:mm:ss") //
	      .setFieldNamingStrategy(m_fieldNamingStrategy) //
	      .create();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object parse(String json, Class clz) {
		return m_gson.fromJson(json, clz);
	}

	public String toJson(Object o) {
		return m_gson.toJson(o);
	}

	public String toJsonWithEnter(Object o) {
		return m_gson.toJson(o) + "\n";
	}

	private static class DoubleTypeAdapter implements JsonSerializer<Double> {
		@Override
		public JsonElement serialize(final Double d, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(new Number() {
				private static final long serialVersionUID = 1L;

				@Override
				public double doubleValue() {
					return d.doubleValue();
				}

				@Override
				public float floatValue() {
					return d.floatValue();
				}

				@Override
				public int intValue() {
					return d.intValue();
				}

				@Override
				public long longValue() {
					return d.longValue();
				}

				@Override
				public String toString() {
					DecimalFormat format = new DecimalFormat("0.##");
					String temp = format.format(d);

					return temp;
				}
			});
		}
	}

	private static class TimestampTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
		private final DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException {
			if (!(json instanceof JsonPrimitive)) {
				throw new JsonParseException("The date should be a string value!");
			}

			try {
				Date date = m_format.parse(json.getAsString());
				return new Timestamp(date.getTime());
			} catch (ParseException e) {
				throw new JsonParseException(e);
			}
		}

		public JsonElement serialize(Timestamp src, Type arg1, JsonSerializationContext arg2) {
			String dateFormatAsString = m_format.format(src);

			return new JsonPrimitive(dateFormatAsString);
		}
	}
}
