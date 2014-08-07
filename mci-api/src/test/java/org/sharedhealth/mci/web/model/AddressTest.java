package org.sharedhealth.mci.web.model;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.sharedhealth.mci.validation.constraints.AddressId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        printViolations(constraintViolations);
    }

    @Test
    public void shouldFailIfAddressLineSizeLessThan3() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", "ab");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
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
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "divisionId", "15");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfDivisionIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "divisionId", "abcd");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveDistrictConstrainAnnotation() {
        assertAddressIdConstraint("districtId", new AddressIdConstraint("DISTRICT"));
    }

    @Test
    public void shouldPassIfDistrictIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "districtId", "15");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfDistrictIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "districtId", "abcd");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveUpazillaConstrainAnnotation() {
        assertAddressIdConstraint("upazillaId", new AddressIdConstraint("UPAZILLA"));
    }

    @Test
    public void shouldPassIfUpazillaIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "upazillaId", "15");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfUpazillaIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "upazillaId", "abcd");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveUnionConstrainAnnotation() {
        assertAddressIdConstraint("unionId", new AddressIdConstraint("UNION"));
    }

    @Test
    public void shouldPassIfUnionIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "unionId", "15");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfUnionIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "unionId", "abc");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    private void assertAddressIdConstraint(String propertyName, AddressIdConstraint constraint) {
        Set<ConstraintDescriptor<?>> descriptors = classConstraints.getConstraintsForProperty(propertyName).getConstraintDescriptors();
        assertTrue(constraint.matchesAny(descriptors));
    }

    @Test
    public void shouldFailIfStreetIsMoreThan_50_Characters() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "street", "janagiralamkabirkhanjirkhanjirkhanjirkhanjirkhanjirkhanjahanaliahmadpuri");
        printViolations(constraintViolations);
    }

    @Test
    public void shouldPassIfStreetIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "street", "DH1234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfAreaMouzaIsMoreThan_3_Characters() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "areaMouja", "jaanjirkhanjahanaliahmadpuri");
        printViolations(constraintViolations);
    }

    @Test
    public void shouldPassIfAreaMouzaIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "areaMouja", "134");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfPostOfficeIsMoreThan_50_Characters() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "postOffice", "jaanjirkh999999999999999999yghgh khkj jkhkjh kjh kk kjhkjh khjkhkj kj kj");
        printViolations(constraintViolations);
    }

    @Test
    public void shouldPassIfPostOfficeIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "postOffice", "Dhaka");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfPostCodeIsMoreThan_10_Characters() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "postCode", "jaanjirkh999999999999999999yghgh khkj jkhkjh kjh kk kjhkjh khjkhkj kj kj");
        printViolations(constraintViolations);
    }

    @Test
    public void shouldPassIfPostCodeIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "postCode", "1362");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldHaveVillageConstrainAnnotation() {
        assertAddressIdConstraint("village", new AddressIdConstraint("VILLAGE"));
    }

    @Test
    public void shouldPassIfVillageIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "village", "12");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfVillageIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "village", "abc");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveWardConstrainAnnotation() {
        assertAddressIdConstraint("ward", new AddressIdConstraint("WARD"));
    }

    @Test
    public void shouldPassIfWardIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "ward", "13");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfWardIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "ward", "abc");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveCountryConstrainAnnotation() {
        assertAddressIdConstraint("country", new AddressIdConstraint("COUNTRY"));
    }

    @Test
    public void shouldPassIfCountryIdIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "country", "124");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfCountryIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "country", "abc");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    @Test
    public void shouldHaveCityCorporationConstrainAnnotation() {
        assertAddressIdConstraint("cityCorporation", new AddressIdConstraint("CITYCORPORATION"));
    }

    @Test
    public void shouldPassIfCityCorporationIsValid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "cityCorporation", "12");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfCityCorporationIdIsInvalid() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "cityCorporation", "abc");
        assertEquals(1, constraintViolations.size());
        printViolations(constraintViolations);
    }

    private void printViolations(Set<ConstraintViolation<Address>> constraintViolations) {

        for (ConstraintViolation<Address> violation : constraintViolations) {

            String invalidValue = (String) violation.getInvalidValue();
            String message = violation.getMessage();
            System.out.println("Found constraint violation. Value: " + invalidValue
                    + " Message: " + message);
        }
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
