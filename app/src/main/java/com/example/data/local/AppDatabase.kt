package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceItemJsonAdapter
import com.example.data.model.ServiceEntity
import com.example.data.model.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [InvoiceEntity::class, VehicleEntity::class, ServiceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceDao(): ServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ava_invoices.db"
                ).addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val vehicleDao = database.vehicleDao()
    val serviceDao = database.serviceDao()
    val invoiceDao = database.invoiceDao()

    if (vehicleDao.getCount() == 0) {
        val initialVehicles = listOf(
            VehicleEntity(
                busNo = "14377",
                plateNo = "14377 Dubai",
                makeModel = "Ashok Leyland Falcon",
                seatingCapacity = "60-Seater AC Bus",
                fuelType = "Diesel",
                driverName = "Muhammad Tariq",
                status = "On Contract",
                mulkiyaExpiry = "15/03/2027",
                defaultDailyRate = 416.67
            ),
            VehicleEntity(
                busNo = "14378",
                plateNo = "14378 Dubai",
                makeModel = "Ashok Leyland Falcon",
                seatingCapacity = "60-Seater AC Bus",
                fuelType = "Diesel",
                driverName = "Abdul Rahman",
                status = "Active",
                mulkiyaExpiry = "20/04/2027",
                defaultDailyRate = 500.00
            ),
            VehicleEntity(
                busNo = "18205",
                plateNo = "18205 Sharjah",
                makeModel = "King Long XMQ6120",
                seatingCapacity = "66-Seater AC Bus",
                fuelType = "Diesel",
                driverName = "Rajesh Kumar",
                status = "On Contract",
                mulkiyaExpiry = "10/01/2027",
                defaultDailyRate = 550.00
            ),
            VehicleEntity(
                busNo = "09142",
                plateNo = "09142 Dubai",
                makeModel = "Yutong ZK6122H",
                seatingCapacity = "50-Seater Luxury AC",
                fuelType = "Diesel",
                driverName = "Shamsher Singh",
                status = "Active",
                mulkiyaExpiry = "30/08/2027",
                defaultDailyRate = 650.00
            ),
            VehicleEntity(
                busNo = "22104",
                plateNo = "22104 Dubai",
                makeModel = "Toyota Coaster AC",
                seatingCapacity = "30-Seater AC Minibus",
                fuelType = "Diesel",
                driverName = "Bilal Ahmed",
                status = "Active",
                mulkiyaExpiry = "18/06/2027",
                defaultDailyRate = 350.00
            ),
            VehicleEntity(
                busNo = "22105",
                plateNo = "22105 Sharjah",
                makeModel = "Mitsubishi Rosa AC",
                seatingCapacity = "33-Seater AC Minibus",
                fuelType = "Diesel",
                driverName = "Nouman Ali",
                status = "Standby",
                mulkiyaExpiry = "05/11/2026",
                defaultDailyRate = 350.00
            ),
            VehicleEntity(
                busNo = "33011",
                plateNo = "33011 Dubai",
                makeModel = "Toyota HiAce AC",
                seatingCapacity = "14-Seater High-Roof",
                fuelType = "Petrol",
                driverName = "Farooq Khan",
                status = "Active",
                mulkiyaExpiry = "28/02/2027",
                defaultDailyRate = 250.00
            ),
            VehicleEntity(
                busNo = "10499",
                plateNo = "10499 Dubai",
                makeModel = "Ashok Leyland Falcon",
                seatingCapacity = "60-Seater AC Bus",
                fuelType = "Diesel",
                driverName = "Scheduled Service",
                status = "Maintenance",
                mulkiyaExpiry = "12/12/2026",
                defaultDailyRate = 450.00
            )
        )
        vehicleDao.insertAll(initialVehicles)
    }

    if (serviceDao.getCount() == 0) {
        val initialServices = listOf(
            ServiceEntity(
                code = "SRV-CAMP-SITE",
                category = "Labor Transportation",
                name = "Camp to Site Pick & Drop",
                description = "60 Seated AC Bus with driver and fuel from camp to Site Pick and Drop",
                typicalBusCapacity = "60-Seater AC Bus",
                rateBasis = "Daily",
                standardRate = 416.67,
                inclusions = "With Driver, Fuel, Maintenance"
            ),
            ServiceEntity(
                code = "SRV-INTER-EMIRATE",
                category = "Inter-Emirate Transfer",
                name = "Sharjah Camp to RAK Site Transfer",
                description = "From Shj Camp - Rak daily transport with driver and fuel",
                typicalBusCapacity = "60-Seater AC Bus",
                rateBasis = "Daily",
                standardRate = 500.00,
                inclusions = "With Driver & Fuel"
            ),
            ServiceEntity(
                code = "SRV-ADD-UAQ",
                category = "Additional Trip",
                name = "Added Trip: UAQ Camp",
                description = "ADDED TRIP: from Uaq camp to project site",
                typicalBusCapacity = "30/60-Seater AC",
                rateBasis = "Per Trip",
                standardRate = 240.39,
                inclusions = "Single pick/drop run"
            ),
            ServiceEntity(
                code = "SRV-SUN-OT",
                category = "Overtime / Weekend",
                name = "Sunday Overtime Trip",
                description = "ADDED TRIP: Sunday dedicated bus transfer with driver & fuel",
                typicalBusCapacity = "60-Seater AC Bus",
                rateBasis = "Per Trip",
                standardRate = 576.92,
                inclusions = "Weekend rate with Driver & Fuel"
            ),
            ServiceEntity(
                code = "SRV-AD-HOC",
                category = "Ad-hoc Charter",
                name = "Ad-hoc Camp Transfer (Shj - RAK)",
                description = "ADDED TRIP: One-way dedicated transfer from Sharjah Camp to RAK",
                typicalBusCapacity = "60-Seater AC Bus",
                rateBasis = "Per Trip",
                standardRate = 576.92,
                inclusions = "Standby & one single run"
            ),
            ServiceEntity(
                code = "SRV-EXEC-STAFF",
                category = "Staff Commute",
                name = "Monthly Staff Commute",
                description = "30 Seater AC Coaster for daily staff transfer (Monthly contract)",
                typicalBusCapacity = "30-Seater Coaster",
                rateBasis = "Monthly",
                standardRate = 7500.00,
                inclusions = "AC, Driver, Fuel & Salik included"
            ),
            ServiceEntity(
                code = "SRV-AIRPORT-TRF",
                category = "Airport Transfer",
                name = "Airport Group Transfer (DXB/SHJ)",
                description = "Airport Group Pick-up / Drop-off (DXB / SHJ Airports)",
                typicalBusCapacity = "14-30 Seater",
                rateBasis = "Per Transfer",
                standardRate = 450.00,
                inclusions = "Driver, Parking & Fuel included"
            ),
            ServiceEntity(
                code = "SRV-DRV-ONLY",
                category = "Bus & Driver Only",
                name = "Monthly Bus & Driver Contract",
                description = "Client provides fuel; AVA provides bus + certified driver",
                typicalBusCapacity = "60-Seater AC Bus",
                rateBasis = "Monthly",
                standardRate = 8000.00,
                inclusions = "Driver salary, maintenance, insurance"
            )
        )
        serviceDao.insertAll(initialServices)
    }

    if (invoiceDao.getInvoiceCount() == 0) {
        val initialItems = listOf(
            InvoiceItem(
                description = "60 Seated AC Bus with driver and fuel from camp to Site Pick and Drop\n        BUS: 14377\n        GO NO: 219\n        FROM 01/09/2026-12/09/2026",
                qty = 12.0,
                unitPrice = 416.67,
                busNo = "14377",
                goNo = "219"
            ),
            InvoiceItem(
                description = "From Shj Camp - Rak\n        FROM 13/09/2026-30/09/2026",
                qty = 18.0,
                unitPrice = 500.00
            ),
            InvoiceItem(
                description = "ADDED TRIP\n        01,10-Sep 2026        from Uaq camp",
                qty = 2.0,
                unitPrice = 240.39
            ),
            InvoiceItem(
                description = "ADDED TRIP\n        18 Sep 2026        From Shj Camp - Rak",
                qty = 1.0,
                unitPrice = 576.92
            ),
            InvoiceItem(
                description = "ADDED TRIP\n        13,20,27-Sep 2026        Sunday",
                qty = 3.0,
                unitPrice = 576.92
            )
        )

        val subtotal = 16788.46
        val vatAmount = 839.42
        val totalDue = 17627.88

        val seedInvoice = InvoiceEntity(
            id = "seed-invoice-02849",
            invoiceNo = "INV-02849",
            invoiceDate = "31 Aug 2026",
            dueDate = "15 Oct 2026",
            companyTrn = "100364388700003",
            clientName = "ALUMINIUM & LIGHT INDUSTRIES CO. (ALICO) LLC",
            clientAddress = "Sharjah - United Arab Emirates",
            clientPoBox = "P.O. Box 6011",
            clientPhone = "+971 6 5582244",
            clientTrn = "100016708800003",
            itemsJson = InvoiceItemJsonAdapter.toJson(initialItems),
            subtotal = subtotal,
            vatAmount = vatAmount,
            totalDue = totalDue,
            status = "Pending"
        )
        invoiceDao.insertOrUpdate(seedInvoice)
    }
}
