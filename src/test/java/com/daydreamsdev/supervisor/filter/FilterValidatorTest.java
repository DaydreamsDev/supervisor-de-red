/*
 * MIT License
 *
 * Copyright (c) 2026 Juan Camilo Rivera
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.daydreamsdev.supervisor.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FilterValidatorTest {

    private static final String MAXIMUM_LENGTH_EXPRESSION = "a".repeat(513);

    private final FilterValidator filterValidator = new FilterValidator();

    @Test
    void validate_givenReasonableExpression_returnsValidResult() {
        FilterValidationResult result = filterValidator.validate("tcp port 8080");

        assertThat(result.valid()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void validate_givenBlankExpression_returnsError() {
        FilterValidationResult result = filterValidator.validate("   ");

        assertThat(result.valid()).isFalse();
        assertThat(result.errorMessage()).isNotBlank();
    }

    @Test
    void validate_givenNullExpression_rejectsWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> filterValidator.validate(null));
    }

    @Test
    void validate_givenOverlyLongExpression_returnsError() {
        FilterValidationResult result = filterValidator.validate(MAXIMUM_LENGTH_EXPRESSION);

        assertThat(result.valid()).isFalse();
        assertThat(result.errorMessage()).isNotBlank();
    }

    @Test
    void validate_givenExpressionAtMaximumLength_returnsValidResult() {
        FilterValidationResult result =
                filterValidator.validate("a".repeat(FilterValidator.MAXIMUM_EXPRESSION_LENGTH));

        assertThat(result.valid()).isTrue();
    }
}
