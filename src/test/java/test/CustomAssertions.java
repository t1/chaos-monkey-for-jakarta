package test;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.BDDAssertions;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static jakarta.json.stream.JsonGenerator.PRETTY_PRINTING;

public class CustomAssertions extends BDDAssertions {
    public static ExtendedStringAssert then(String actual) {
        return new ExtendedStringAssert(actual);
    }

    public static String normalizeJson(String json) {return toString(toJson(json));}

    public static JsonValue toJson(String json) {
        return Json.createReader(new StringReader(json)).readValue();
    }

    public static String toString(JsonValue jsonValue) {
        Writer out = new StringWriter();
        Json.createWriterFactory(Map.of(PRETTY_PRINTING, true))
                .createWriter(out)
                .write(jsonValue);
        return out.toString();
    }

    public static class ExtendedStringAssert extends AbstractStringAssert<ExtendedStringAssert> {
        public ExtendedStringAssert(String actual) {super(actual, ExtendedStringAssert.class);}

        public void isJsonEqualTo(String expected) {
            then(normalizeJson(actual)).isEqualTo(normalizeJson(expected));
        }
    }
}
