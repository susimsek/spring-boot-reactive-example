package io.github.susimsek.springbootreactiveexample.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumConstraint, String?> {

    private lateinit var allowedValues: Set<String>
    private lateinit var message: String

    override fun initialize(annotation: EnumConstraint) {
        allowedValues = annotation.enumClass.java.enumConstants
            .map { it.name }
            .toSet()
        this.message = annotation.message
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        val isValid = allowedValues.contains(value)

        if (!isValid) {
            val allowedValuesString = allowedValues.joinToString(", ")

            val hibernateContext = context.unwrap(HibernateConstraintValidatorContext::class.java)
            hibernateContext.disableDefaultConstraintViolation()
            hibernateContext.addMessageParameter("value", value)
            hibernateContext.addMessageParameter("allowedValues", allowedValuesString)
            hibernateContext.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation()
        }

        return isValid
    }
}
