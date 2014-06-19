package org.mci.web.model;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mci.web.annotation.AddressId;

public class AddressTest {

    private static Validator validator;
    private static BeanDescriptor classConstraints;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
        classConstraints = validator.getConstraintsForClass(Address.class);
    }

    @Test
    public void shouldFailIfAddressLineIsBlank() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", null);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfAddressLineSizeLessThan3() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", "ab");
        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 3 and 20", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfAddressLineSize3() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", "row");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldHaveDivisionConstrainAnnotation() {
        assertAddressIdConstraint("divisionId", new AddressIdConstraint("DIVISION"));
    }

    @Test
    public void shouldPassIfDivisionIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "divisionId", "12345");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfDivisionIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "divisionId", "abcd");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldHaveDistrictConstrainAnnotation() {
        assertAddressIdConstraint("districtId", new AddressIdConstraint("DISTRICT"));
    }

    @Test
    public void shouldPassIfDistrictIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "districtId", "12345");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfDistrictIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "districtId", "abcd");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldHaveUpazillaConstrainAnnotation() {
        assertAddressIdConstraint("upazillaId", new AddressIdConstraint("UPAZILLA"));
    }

    @Test
    public void shouldPassIfUpazillaIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "upazillaId", "12345");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfUpazillaIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "upazillaId", "abcd");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldHaveUnionConstrainAnnotation() {
        assertAddressIdConstraint("unionId", new AddressIdConstraint("UNION"));
    }

    @Test
    public void shouldPassIfUnionIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "unionId", "12345");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfUnionIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "unionId", "abc");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    private void assertAddressIdConstraint(String propertyName, AddressIdConstraint constraint) {
        Set<ConstraintDescriptor<?>> descriptors = classConstraints.getConstraintsForProperty(propertyName).getConstraintDescriptors();
        assertTrue(constraint.matchesAny(descriptors));
    }

    private static class AddressIdConstraint {

        private final Class type;
        private final String value;

        public AddressIdConstraint(String value) {
            this.type = AddressId.class;
            this.value = value;
        }

        public boolean matchesAny(Set<ConstraintDescriptor<?>> descriptors) {
            for (ConstraintDescriptor descriptor : descriptors) {
                Class<? extends Annotation> aClass = descriptor.getAnnotation().annotationType();
                Map<String, Object> attributes = descriptor.getAttributes();
                String val = attributes.get("value").toString();
                if (this.type.equals(aClass) && this.value.equals(val)) {
                    return true;
                }
            }
            return false;
        }
    }
}
