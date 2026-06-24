package com.example.martclinic_videochat.util

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PortOneUtilTest {

    @Test
    fun getVerifiedCustomer_nonExistentId_returnsNull() = runTest {
        // Given an identityVerificationId that does not exist
        val id = "non-existent-test-id"

        // When we call getVerifiedCustomer
        val customer = PortOneUtil.getVerifiedCustomer(id)

        // Then it should handle the 404 response and return null (since it's not "VERIFIED")
        assertNull(customer)
    }
}
