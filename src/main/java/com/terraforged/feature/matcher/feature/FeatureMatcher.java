package com.terraforged.feature.matcher.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.function.Predicate;

public class FeatureMatcher implements Predicate<JsonElement> {

    public static final FeatureMatcher ANY = new FeatureMatcher(Collections.emptyList());

    private final List<Rule> rules;

    private FeatureMatcher(List<Rule> rules) {
        super();
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "JsonMatcher{" +
                "rules=" + rules +
                '}';
    }

    @Override
    public boolean test(JsonElement element) {
        if (rules.isEmpty()) {
            return true;
        }
        return test(element, new Search(rules));
    }

    private boolean test(JsonElement element, Search search) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                if (test(e.getValue(), search)) {
                    return true;
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (test(e, search)) {
                    return true;
                }
            }
        } else if (element.isJsonPrimitive()) {
            return search.test(element.getAsJsonPrimitive());
        }
        return false;
    }

    public static Optional<FeatureMatcher> of(JsonElement element) {
        List<Rule> rules = Rule.parseRules(element);
        if (rules.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeatureMatcher(rules));
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain the provided arg
     */
    public static FeatureMatcher of(Object arg) {
        return and(arg);
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain ANY of the provided args
     */
    public static FeatureMatcher or(Object... args) {
        if (args.length == 0) {
            return FeatureMatcher.ANY;
        }
        FeatureMatcher.Builder builder = builder();
        for (Object o : args) {
            builder.or(o);
        }
        return builder.build();
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain ALL of the provided args
     */
    public static FeatureMatcher and(Object... args) {
        if (args.length == 0) {
            return FeatureMatcher.ANY;
        }
        FeatureMatcher.Builder builder = builder();
        for (Object arg : args) {
            builder.and(arg);
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static JsonElement arg(Object arg) {
        if (arg instanceof String) {
            return new JsonPrimitive((String) arg);
        }
        if (arg instanceof Number) {
            return new JsonPrimitive((Number) arg);
        }
        if (arg instanceof Boolean) {
            return new JsonPrimitive((Boolean) arg);
        }
        if (arg instanceof ForgeRegistryEntry) {
            return new JsonPrimitive(((ForgeRegistryEntry<?>) arg).getRegistryName() + "");
        }
        return JsonNull.INSTANCE;
    }

    public static class Builder {

        private List<Rule> rules = Collections.emptyList();
        private List<JsonPrimitive> values = Collections.emptyList();

        public Builder and(Object value) {
            JsonElement element = FeatureMatcher.arg(value);
            if (element.isJsonPrimitive()) {
                and(element.getAsJsonPrimitive());
            }
            return this;
        }

        public Builder and(Boolean value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(Number value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(String value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(JsonPrimitive value) {
            if (values.isEmpty()) {
                values = new ArrayList<>();
            }
            values.add(value);
            return this;
        }

        public Builder or(Object value) {
            JsonElement element = FeatureMatcher.arg(value);
            if (element.isJsonPrimitive()) {
                or(element.getAsJsonPrimitive());
            }
            return this;
        }

        public Builder or(Boolean value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(Number value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(String value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(JsonPrimitive value) {
            return newRule().and(value);
        }

        public Builder newRule() {
            if (!values.isEmpty()) {
                if (rules.isEmpty()) {
                    rules = new ArrayList<>();
                }
                rules.add(new Rule(values));
                values = Collections.emptyList();
            }
            return this;
        }

        public FeatureMatcher build() {
            newRule();
            return new FeatureMatcher(rules);
        }
    }
}
