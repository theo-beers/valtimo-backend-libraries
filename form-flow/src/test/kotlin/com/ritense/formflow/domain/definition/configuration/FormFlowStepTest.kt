/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.formflow.domain.definition.configuration

import com.ritense.formflow.domain.definition.FormFlowNextStep as FormFlowNextStepEntity
import com.ritense.formflow.domain.definition.FormFlowStep as FormFlowStepEntity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowStepId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FormFlowStepTest {

    @Test
    fun `contentEquals should match when nextsteps match with single entry`() {
        val otherMock: FormFlowNextStepEntity = mock()

        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(otherMock)).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock
            )
        )

        assertTrue(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should match when nextsteps match with multiple entries`() {
        val otherMock1: FormFlowNextStepEntity = mock()
        val otherMock2: FormFlowNextStepEntity = mock()

        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(otherMock1)).thenReturn(true)
        whenever(mockStep1.contentEquals(otherMock2)).thenReturn(false)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(otherMock1)).thenReturn(false)
        whenever(mockStep2.contentEquals(otherMock2)).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock1,
                otherMock2
            )
        )

        assertTrue(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when this step has more nextsteps then other`() {
        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(any())).thenReturn(true)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("step-key"),
            mutableListOf(
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when other step has more nextsteps then this`() {
        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("step-key"),
            mutableListOf(
                mock(),
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when equal number of nextsteps but content is different`() {
        val otherMock1: FormFlowNextStepEntity = mock()
        val otherMock2: FormFlowNextStepEntity = mock()

        val mockStep1: FormFlowNextStep = mock()
        whenever(mockStep1.contentEquals(otherMock1)).thenReturn(true)
        whenever(mockStep1.contentEquals(otherMock2)).thenReturn(false)

        val mockStep2: FormFlowNextStep = mock()
        whenever(mockStep2.contentEquals(otherMock1)).thenReturn(false)
        whenever(mockStep2.contentEquals(otherMock2)).thenReturn(false)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep1,
                mockStep2
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("step-key"),
            mutableListOf(
                otherMock1,
                otherMock2
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }

    @Test
    fun `contentEquals should not match when start step is different`() {
        val mockStep: FormFlowNextStep = mock()
        whenever(mockStep.contentEquals(any())).thenReturn(true)

        val thisStep = FormFlowStep(
            "step-key",
            mutableListOf(
                mockStep
            )
        )
        val otherStep = FormFlowStepEntity(
            FormFlowStepId("other-key"),
            mutableListOf(
                mock()
            )
        )

        assertFalse(thisStep.contentEquals(otherStep))
    }
}