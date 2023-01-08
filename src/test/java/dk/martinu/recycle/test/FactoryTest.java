package dk.martinu.recycle.test;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.*;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.annotation.*;
import java.util.function.Supplier;

import dk.martinu.recycle.RetentionPolicy;
import dk.martinu.recycle.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for factory methods in {@link Recyclers}.
 */
public class FactoryTest {

    @DisplayName("can create constant")
    @ParameterizedTest
    @CsvSource({
            "type, 128, PoolAny, supplier"
    })
    void constantCreate(@ClassValue final Class<Object> componentType, final int bucketSize,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertNotNull(Recyclers.createConstant(componentType, supplier));
        assertNotNull(Recyclers.createConstant(componentType, bucketSize, supplier));
        assertNotNull(Recyclers.createConstant(componentType, policy, supplier));
        assertNotNull(Recyclers.createConstant(componentType, bucketSize, policy, supplier));
    }

    @DisplayName("constant throws IllegalArgumentException")
    @ParameterizedTest
    @CsvSource({
            "type, 0, PoolAny, supplier"
    })
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void constantThrowsIAE(@ClassValue final Class<Object> componentType, final int bucketSize,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createConstant(componentType, bucketSize, supplier));
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createConstant(componentType, bucketSize, policy, supplier));
    }

    @DisplayName("constant throws NullPointerException")
    @ParameterizedTest
    @CsvSource({
            "null, 128, PoolAny, supplier",
            "type, 128, null, supplier",
            "type, 128, PoolAny, null"
    })
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    void constantThrowsNPE(@ClassValue final Class<Object> componentType, final int bucketSize,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        if (componentType == null || supplier == null) {
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createConstant(componentType, supplier));
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createConstant(componentType, bucketSize, supplier));
        }
        assertThrows(NullPointerException.class, () ->
                Recyclers.createConstant(componentType, policy, supplier));
        assertThrows(NullPointerException.class, () ->
                Recyclers.createConstant(componentType, bucketSize, policy, supplier));
    }

    @DisplayName("can create exponential")
    @ParameterizedTest
    @CsvSource({
            "type, 64, 1.25d, PoolAny, supplier"
    })
    void exponentialCreate(@ClassValue final Class<Object> componentType, final int coefficient, final double base,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertNotNull(Recyclers.createExponential(componentType, supplier));
        assertNotNull(Recyclers.createExponential(componentType, coefficient, base, supplier));
        assertNotNull(Recyclers.createExponential(componentType, policy, supplier));
        assertNotNull(Recyclers.createExponential(componentType, coefficient, base, policy, supplier));
    }

    @DisplayName("exponential throws IllegalArgumentException")
    @ParameterizedTest
    @CsvSource({
            "type, 0, 1.25d, PoolAny, supplier",
            "type, 64, 0, PoolAny, supplier"
    })
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void exponentialThrowsIAE(@ClassValue final Class<Object> componentType, final int coefficient, final double base,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createExponential(componentType, coefficient, base, supplier));
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createExponential(componentType, coefficient, base, policy, supplier));
    }

    @DisplayName("exponential throws NullPointerException")
    @ParameterizedTest
    @CsvSource({
            "null, 64, 1.25d, PoolAny, supplier",
            "type, 64, 1.25d, null, supplier",
            "type, 64, 1.25d, PoolAny, null"
    })
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    void exponentialThrowsNPE(@ClassValue final Class<Object> componentType, final int coefficient, final double base,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        if (componentType == null || supplier == null) {
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createExponential(componentType, supplier));
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createExponential(componentType, coefficient, base, supplier));
        }
        assertThrows(NullPointerException.class, () ->
                Recyclers.createExponential(componentType, policy, supplier));
        assertThrows(NullPointerException.class, () ->
                Recyclers.createExponential(componentType, coefficient, base, policy, supplier));
    }

    @DisplayName("can create linear")
    @ParameterizedTest
    @CsvSource({
            "type, 64, 64, PoolAny, supplier"
    })
    void linearCreate(@ClassValue final Class<Object> componentType, final int slope, final int intercept,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertNotNull(Recyclers.createLinear(componentType, supplier));
        assertNotNull(Recyclers.createLinear(componentType, slope, intercept, supplier));
        assertNotNull(Recyclers.createLinear(componentType, policy, supplier));
        assertNotNull(Recyclers.createLinear(componentType, slope, intercept, policy, supplier));
    }

    @DisplayName("linear throws IllegalArgumentException")
    @ParameterizedTest
    @CsvSource({
            "type, 0, 64, PoolAny, supplier",
            "type, 64, 0, PoolAny, supplier"
    })
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void linearThrowsIAE(@ClassValue final Class<Object> componentType, final int slope, final int intercept,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createLinear(componentType, slope, intercept, supplier));
        assertThrows(IllegalArgumentException.class, () ->
                Recyclers.createLinear(componentType, slope, intercept, policy, supplier));
    }

    @DisplayName("linear throws NullPointerException")
    @ParameterizedTest
    @CsvSource({
            "null, 64, 64, PoolAny, supplier",
            "type, 64, 64, null, supplier",
            "type, 64, 64, PoolAny, null"
    })
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    void linearThrowsNPE(@ClassValue final Class<Object> componentType, final int slope, final int intercept,
            @PolicyValue final RetentionPolicy policy, @SupplierValue final Supplier<Object> supplier) {
        if (componentType == null || supplier == null) {
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createLinear(componentType, supplier));
            assertThrows(NullPointerException.class, () ->
                    Recyclers.createLinear(componentType, slope, intercept, supplier));
        }
        assertThrows(NullPointerException.class, () ->
                Recyclers.createLinear(componentType, policy, supplier));
        assertThrows(NullPointerException.class, () ->
                Recyclers.createLinear(componentType, slope, intercept, policy, supplier));
    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ConvertWith(StringToClassConverter.class)
    public @interface ClassValue { }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ConvertWith(StringToPolicyConverter.class)
    public @interface PolicyValue { }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @ConvertWith(StringToSupplierConverter.class)
    public @interface SupplierValue { }

    @SuppressWarnings("rawtypes")
    public static class StringToClassConverter extends TypedArgumentConverter<String, Class> {

        protected StringToClassConverter() {
            super(String.class, Class.class);
        }

        @Contract(value = "_ -> new", pure = true)
        @Nullable
        @Override
        protected Class<Object> convert(final String source) throws ArgumentConversionException {
            return switch (source) {
                case "type" -> Object.class;
                case "null" -> null;
                default -> throw new ArgumentConversionException("invalid class source {" + source + "}");
            };
        }
    }

    public static class StringToPolicyConverter extends TypedArgumentConverter<String, RetentionPolicy> {

        protected StringToPolicyConverter() {
            super(String.class, RetentionPolicy.class);
        }

        @Contract(value = "_ -> new", pure = true)
        @Nullable
        @Override
        protected RetentionPolicy convert(final String source) throws ArgumentConversionException {
            return switch (source) {
                case "PoolAny" -> PoolAny.get();
                case "null" -> null;
                default -> throw new ArgumentConversionException("invalid policy source {" + source + "}");
            };
        }
    }

    @SuppressWarnings("rawtypes")
    public static class StringToSupplierConverter extends TypedArgumentConverter<String, Supplier> {

        protected StringToSupplierConverter() {
            super(String.class, Supplier.class);
        }

        @Contract(value = "_ -> new", pure = true)
        @Nullable
        @Override
        protected Supplier<Object> convert(final String source) throws ArgumentConversionException {
            return switch (source) {
                case "supplier" -> Object::new;
                case "null" -> null;
                default -> throw new ArgumentConversionException("invalid supplier source {" + source + "}");
            };
        }
    }
}
