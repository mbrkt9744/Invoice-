const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

test("Unauthenticated user cannot read or write invoices", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const invoiceRef = unauthedDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv1");
  await assertFails(invoiceRef.get());
  await assertFails(invoiceRef.set({ invoiceNo: "INV-001" }));
});

test("Alice can create and read her own invoice", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const invoiceRef = aliceDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv1");

  await assertSucceeds(
    invoiceRef.set({
      id: "inv1",
      userId: ALICE_UID,
      invoiceNo: "INV-02849",
      invoiceDate: "31 Aug 2026",
      clientName: "ALICO LLC",
      totalDue: 17627.88,
      status: "Pending",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(invoiceRef.get());
});

test("Bob cannot read or modify Alice's invoice", async () => {
  // Alice creates her invoice first
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceInvoice = aliceDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv1");
  await aliceInvoice.set({
    id: "inv1",
    userId: ALICE_UID,
    invoiceNo: "INV-02849",
    invoiceDate: "31 Aug 2026",
    clientName: "ALICO LLC",
    totalDue: 17627.88,
    status: "Pending",
    createdAt: new Date(),
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const targetInvoice = bobDb.collection("users").doc(ALICE_UID).collection("invoices").doc("inv1");

  await assertFails(targetInvoice.get());
  await assertFails(targetInvoice.update({ status: "Paid" }));
  await assertFails(targetInvoice.delete());
});

test("Alice can create and read a vehicle in her fleet", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const vehicleRef = aliceDb.collection("users").doc(ALICE_UID).collection("vehicles").doc("veh1");

  await assertSucceeds(
    vehicleRef.set({
      id: "veh1",
      userId: ALICE_UID,
      busNo: "14377",
      plateNo: "14377 Dubai",
      makeModel: "Ashok Leyland Falcon",
      driverName: "Muhammad Tariq",
      status: "On Contract",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(vehicleRef.get());
});

test("Alice can create and read a service in her catalog", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const serviceRef = aliceDb.collection("users").doc(ALICE_UID).collection("services").doc("srv1");

  await assertSucceeds(
    serviceRef.set({
      id: "srv1",
      userId: ALICE_UID,
      code: "SRV-CAMP-SITE",
      category: "Labor Transportation",
      name: "Camp to Site Pick & Drop",
      standardRate: 416.67,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(serviceRef.get());
});
