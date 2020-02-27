package org.kiegroup.zenithr.drools.model;

import java.util.function.BiFunction;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.vertx.VertxRuntime;

public class InputFact {

    private final BaseRuntime<Object> runtime = new VertxRuntime();
    private final String name;
    private final String value;

    public InputFact(String name) {
        this.name = name;
        this.value = null;
    }

    private InputFact(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public static InputFact withName(String name) {
        return new InputFact(name, null);
    }

    public InputFact withValue(String value) {
        return new InputFact(name, value);
    }

    public boolean equalsTo(String path, Comparable value) {
        return compareTo(path, value, (a, b) -> (a == null && b == null) || (a != null && a.compareTo(b) == 0));
    }

    public boolean lessThan(String path, Comparable value) {
        return compareTo(path, value, (a, b) -> a != null && b != null && a.compareTo(b) < 0);
    }

    public boolean lessThanEquals(String path, Comparable value) {
        return compareTo(path, value, (a, b) -> a != null && b != null && a.compareTo(b) <= 0);
    }

    public boolean greaterThan(String path, Comparable value) {
        return compareTo(path, value, (a, b) -> a != null && b != null && a.compareTo(b) > 0);
    }

    public boolean greaterThanEquals(String path, Comparable value) {
        return compareTo(path, value, (a, b) -> a != null && b != null && a.compareTo(b) >= 0);
    }

    public boolean exists(String path) {
        return search(path) != null;
    }

    public Integer getNumber(String path) {
        return Integer.valueOf(search(path).toString());
    }

    public String getString(String path) {
        return search(path).toString();
    }

    private boolean compareTo(String path, Comparable other, BiFunction<Comparable, Comparable, Boolean> compare) {
        Comparable result = getComparable(search(path));
        if(other.getClass().isAssignableFrom(result.getClass()) || result.getClass().isAssignableFrom(other.getClass())) {
            return compare.apply(result, other);
        } else {
            return compare.apply(result.toString(), other.toString());
        }
    }

    private Object search(String path) {
        return runtime.compile(path).search(runtime.parseString(this.value));
    }

    private Comparable getComparable(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Comparable) {
            return (Comparable) object;
        }
        throw new IllegalArgumentException("Expected a Comparable. Got " + object.getClass().getName());
    }
}
