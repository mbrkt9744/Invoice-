package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreRepository
import com.example.data.model.InvoiceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveInvoice_authenticatedUser_succeeds() = runBlocking {
    val uid = signInTestUser("alice@example.com")
    val repo = FirestoreRepository(firestore, auth)
    val testId = "inv_${System.currentTimeMillis()}"

    val invoice = InvoiceEntity(
      id = testId,
      invoiceNo = "INV-02849",
      invoiceDate = "31 Aug 2026",
      dueDate = "15 Oct 2026",
      clientName = "ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC",
      totalDue = 17627.88,
      status = "Pending"
    )

    withTimeout(10000L) {
      repo.saveInvoice(invoice)
    }

    val invoices = withTimeout(10000L) {
      repo.observeInvoices(uid).first { it.any { inv -> inv.id == testId } }
    }

    val saved = invoices.first { it.id == testId }
    assertEquals("INV-02849", saved.invoiceNo)
    assertEquals("ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC", saved.clientName)
  }

  @Test
  fun saveInvoice_unauthenticatedUser_fails() = runBlocking {
    auth.signOut()
    val repo = FirestoreRepository(firestore)

    val invoice = InvoiceEntity(
      id = "inv_unauth_1",
      invoiceNo = "INV-99999",
      invoiceDate = "31 Aug 2026",
      dueDate = "15 Oct 2026",
      clientName = "Unauth Client",
      totalDue = 500.0,
      status = "Draft"
    )

    var caught = false
    try {
      repo.saveInvoice(invoice)
    } catch (_: IllegalStateException) {
      caught = true
    }
    assertTrue("Should throw IllegalStateException when unauthenticated", caught)
  }

  @Test
  fun observeInvoices_crossUserAccess_rejectedByRules() = runBlocking {
    val aliceUid = signInTestUser("alice_cross@example.com")
    val aliceRepo = FirestoreRepository(firestore, auth)
    val testId = "inv_cross_${System.currentTimeMillis()}"

    val invoice = InvoiceEntity(
      id = testId,
      invoiceNo = "INV-CROSS",
      invoiceDate = "31 Aug 2026",
      dueDate = "15 Oct 2026",
      clientName = "Cross Test Client",
      totalDue = 1000.0,
      status = "Pending"
    )

    withTimeout(10000L) {
      aliceRepo.saveInvoice(invoice)
    }

    // Now sign in as Bob
    signInTestUser("bob_cross@example.com")

    // Bob attempts to directly read Alice's invoice from Firestore
    var permissionDenied = false
    try {
      withTimeout(5000L) {
        val doc = firestore.collection("users").document(aliceUid).collection("invoices").document(testId).get()
        com.google.android.gms.tasks.Tasks.await(doc)
      }
    } catch (e: Exception) {
      if (e.message?.contains("PERMISSION_DENIED") == true || e.cause?.message?.contains("PERMISSION_DENIED") == true) {
        permissionDenied = true
      }
    }
    assertTrue("Direct cross-user read must fail with PERMISSION_DENIED", permissionDenied)
  }
}
