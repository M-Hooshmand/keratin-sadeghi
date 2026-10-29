package com.keratinsadeghi.salon.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.keratinsadeghi.salon.util.currentJalaliYear
import com.keratinsadeghi.salon.util.jalaliToIso
import com.keratinsadeghi.salon.util.monthRange
import com.keratinsadeghi.salon.util.todayIso
import com.keratinsadeghi.salon.util.yearRange

class Database(context: Context) : SQLiteOpenHelper(context, "keratin.db", null, 2) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val schema =
            """
            CREATE TABLE customers(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              first_name TEXT NOT NULL,
              last_name TEXT NOT NULL,
              phone TEXT NOT NULL,
              notes TEXT,
              created_at TEXT DEFAULT (datetime('now'))
            );
            CREATE UNIQUE INDEX idx_customers_phone ON customers(phone);
            CREATE TABLE services(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL UNIQUE,
              sort INTEGER NOT NULL DEFAULT 0,
              active INTEGER NOT NULL DEFAULT 1
            );
            CREATE TABLE appointments(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
              service_id INTEGER REFERENCES services(id) ON DELETE SET NULL,
              appointment_date TEXT NOT NULL,
              appointment_time TEXT NOT NULL,
              deposit INTEGER NOT NULL DEFAULT 0,
              status TEXT NOT NULL DEFAULT 'waiting',
              notes TEXT,
              created_at TEXT DEFAULT (datetime('now')),
              updated_at TEXT DEFAULT (datetime('now'))
            );
            CREATE INDEX idx_appointments_date ON appointments(appointment_date);
            CREATE INDEX idx_appointments_status ON appointments(status);
            CREATE TABLE visits(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              appointment_id INTEGER NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
              customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
              service_id INTEGER REFERENCES services(id) ON DELETE SET NULL,
              performed_date TEXT NOT NULL,
              final_amount INTEGER NOT NULL DEFAULT 0,
              duration_minutes INTEGER,
              assistant_count INTEGER NOT NULL DEFAULT 0,
              assistant_cost INTEGER NOT NULL DEFAULT 0,
              hair_length_cm REAL,
              hair_volume TEXT,
              hair_condition TEXT,
              work_done TEXT,
              notes TEXT,
              created_at TEXT DEFAULT (datetime('now')),
              updated_at TEXT DEFAULT (datetime('now'))
            );
            CREATE INDEX idx_visits_date ON visits(performed_date);
            CREATE TABLE materials(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL UNIQUE,
              unit TEXT NOT NULL DEFAULT 'ml',
              price_per_unit INTEGER NOT NULL DEFAULT 0,
              notes TEXT,
              active INTEGER NOT NULL DEFAULT 1
            );
            CREATE TABLE material_usages(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              visit_id INTEGER NOT NULL REFERENCES visits(id) ON DELETE CASCADE,
              material_id INTEGER NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
              amount REAL NOT NULL DEFAULT 0,
              unit_price INTEGER NOT NULL DEFAULT 0,
              unit TEXT NOT NULL DEFAULT 'ml'
            );
            CREATE TABLE holidays(
              date TEXT PRIMARY KEY,
              is_holiday INTEGER NOT NULL DEFAULT 0,
              title TEXT
            );
            CREATE TABLE users(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL UNIQUE,
              salt TEXT NOT NULL,
              password_hash TEXT NOT NULL,
              created_at TEXT DEFAULT (datetime('now'))
            );
            CREATE TABLE settings(
              key TEXT PRIMARY KEY,
              value TEXT
            );
            """.trimIndent()
        schema.split(";").map { it.trim() }.filter { it.isNotEmpty() }.forEach { db.execSQL(it) }
        db.beginTransaction()
        try {
            val services = listOf(
                "کراتین موهای نچرال",
                "کراتین موهای رنگ‌شده",
                "پروتئین تراپی موهای نچرال",
                "پروتئین تراپی موهای آسیب‌دیده",
                "پروتئین تراپی موهای دکلره‌دار",
                "پروتئین تراپی + بسترساز (موهای خیلی آسیب‌دیده و کشسان)",
                SPLIT_END_SERVICE_NAME,
            )
            services.forEachIndexed { index, name ->
                db.execSQL(
                    "INSERT OR IGNORE INTO services(name, sort) VALUES(?, ?)",
                    arrayOf(name, index),
                )
            }
            val materials = listOf(
                arrayOf("پروتئین", 2500L),
                arrayOf("کراتین موهای نچرال", 3000L),
                arrayOf("کراتین موهای رنگ‌شده", 3200L),
                arrayOf("بسترساز", 1800L),
                arrayOf("شامپو", 450L),
                arrayOf("ماسک درمانی", 900L),
            )
            materials.forEach { row ->
                db.execSQL(
                    "INSERT OR IGNORE INTO materials(name, price_per_unit) VALUES(?, ?)",
                    arrayOf(row[0], row[1]),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS users(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT NOT NULL UNIQUE,
                  salt TEXT NOT NULL,
                  password_hash TEXT NOT NULL,
                  created_at TEXT DEFAULT (datetime('now'))
                )
                """.trimIndent(),
            )
            val columns = db.rawQuery("PRAGMA table_info(visits)", null).use { c ->
                buildSet { while (c.moveToNext()) add(c.getString(c.getColumnIndexOrThrow("name"))) }
            }
            if ("duration_minutes" !in columns) db.execSQL("ALTER TABLE visits ADD COLUMN duration_minutes INTEGER")
            if ("assistant_count" !in columns) db.execSQL("ALTER TABLE visits ADD COLUMN assistant_count INTEGER NOT NULL DEFAULT 0")
            if ("assistant_cost" !in columns) db.execSQL("ALTER TABLE visits ADD COLUMN assistant_cost INTEGER NOT NULL DEFAULT 0")
        }
    }

    private fun Cursor.text(name: String): String {
        val idx = getColumnIndexOrThrow(name)
        return if (isNull(idx)) "" else getString(idx)
    }

    private fun Cursor.textOrNull(name: String): String? {
        val idx = getColumnIndexOrThrow(name)
        return if (isNull(idx)) null else getString(idx)
    }

    private fun Cursor.long(name: String): Long {
        val idx = getColumnIndexOrThrow(name)
        return if (isNull(idx)) 0L else getLong(idx)
    }

    private fun Cursor.int(name: String): Int {
        val idx = getColumnIndexOrThrow(name)
        return if (isNull(idx)) 0 else getInt(idx)
    }

    private fun Cursor.doubleOrNull(name: String): Double? {
        val idx = getColumnIndexOrThrow(name)
        return if (isNull(idx)) null else getDouble(idx)
    }

    private fun Cursor.bool(name: String): Boolean = long(name) != 0L

    private val appointmentSelect = """
        SELECT a.id, a.customer_id, c.first_name, c.last_name, c.phone,
               a.service_id, s.name AS service_name, a.appointment_date,
               a.appointment_time, a.deposit, a.status, a.notes,
               v.id AS visit_id, COALESCE(v.final_amount, 0) AS final_amount,
               COALESCE((SELECT SUM(mu.amount * mu.unit_price)
                         FROM material_usages mu WHERE mu.visit_id = v.id), 0) AS material_cost
        FROM appointments a
        JOIN customers c ON c.id = a.customer_id
        LEFT JOIN services s ON s.id = a.service_id
        LEFT JOIN visits v ON v.appointment_id = a.id
    """.trimIndent()

    private fun appointmentFrom(c: Cursor) = AppointmentItem(
        id = c.long("id"),
        customerId = c.long("customer_id"),
        firstName = c.text("first_name"),
        lastName = c.text("last_name"),
        phone = c.text("phone"),
        serviceId = if (c.isNull(c.getColumnIndexOrThrow("service_id"))) null
        else c.long("service_id"),
        serviceName = c.textOrNull("service_name"),
        date = c.text("appointment_date"),
        time = c.text("appointment_time"),
        deposit = c.long("deposit"),
        status = AppointmentStatus.from(c.text("status")),
        notes = c.textOrNull("notes"),
        visitId = if (c.isNull(c.getColumnIndexOrThrow("visit_id"))) null
        else c.long("visit_id"),
        finalAmount = c.long("final_amount"),
        materialCost = c.long("material_cost"),
    )

    fun listAppointments(
        status: AppointmentStatus? = null,
        search: String? = null,
        date: String? = null,
        from: String? = null,
        customerId: Long? = null,
    ): List<AppointmentItem> {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (status != null) {
            where += "a.status = ?"
            args += status.value
        }
        if (!search.isNullOrBlank()) {
            where += "(c.first_name LIKE ? OR c.last_name LIKE ? OR c.phone LIKE ?)"
            val q = "%${search.trim()}%"
            args += q
            args += q
            args += q
        }
        if (date != null) {
            where += "a.appointment_date = ?"
            args += date
        }
        if (from != null) {
            where += "a.appointment_date >= ?"
            args += from
        }
        if (customerId != null) {
            where += "a.customer_id = ?"
            args += customerId.toString()
        }
        val sql = buildString {
            append(appointmentSelect)
            if (where.isNotEmpty()) append(" WHERE ").append(where.joinToString(" AND "))
            append(" ORDER BY a.appointment_date ASC, a.appointment_time ASC")
        }
        return readableDatabase.rawQuery(sql, args.toTypedArray()).use { c ->
            buildList { while (c.moveToNext()) add(appointmentFrom(c)) }
        }
    }

    fun getAppointment(id: Long): AppointmentItem? =
        readableDatabase.rawQuery("$appointmentSelect WHERE a.id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) appointmentFrom(c) else null
        }

    fun saveAppointment(
        appointmentId: Long?,
        firstName: String,
        lastName: String,
        phone: String,
        serviceId: Long?,
        date: String,
        time: String,
        deposit: Long,
        notes: String?,
        status: AppointmentStatus,
    ): Pair<Long?, String?> {
        val db = writableDatabase
        val first = firstName.trim()
        val last = lastName.trim()
        val mobile = phone.trim()
        if (first.isEmpty() || last.isEmpty() || mobile.isEmpty()) {
            return null to "نام، نام خانوادگی و شماره تماس الزامی است"
        }
        if (date.isBlank() || time.isBlank()) return null to "تاریخ و ساعت نوبت الزامی است"

        var existingCustomerId: Long? = null
        if (appointmentId != null) {
            db.rawQuery("SELECT customer_id FROM appointments WHERE id = ?", arrayOf(appointmentId.toString())).use { c ->
                if (c.moveToFirst()) existingCustomerId = c.getLong(0)
            }
        }
        val byPhone = customerByPhone(mobile)
        val customerId: Long
        if (byPhone != null) {
            customerId = byPhone
            val values = ContentValues().apply {
                put("first_name", first)
                put("last_name", last)
            }
            db.update("customers", values, "id = ?", arrayOf(customerId.toString()))
        } else if (existingCustomerId != null) {
            customerId = existingCustomerId!!
            val values = ContentValues().apply {
                put("first_name", first)
                put("last_name", last)
                put("phone", mobile)
            }
            db.update("customers", values, "id = ?", arrayOf(customerId.toString()))
        } else {
            val values = ContentValues().apply {
                put("first_name", first)
                put("last_name", last)
                put("phone", mobile)
            }
            customerId = db.insertOrThrow("customers", null, values)
        }

        val values = ContentValues().apply {
            put("customer_id", customerId)
            if (serviceId == null) putNull("service_id") else put("service_id", serviceId)
            put("appointment_date", date)
            put("appointment_time", time)
            put("deposit", deposit)
            put("status", status.value)
            if (notes.isNullOrBlank()) putNull("notes") else put("notes", notes.trim())
        }
        return try {
            val id = if (appointmentId == null) {
                db.insertOrThrow("appointments", null, values)
            } else {
                values.put("updated_at", java.time.LocalDateTime.now().toString())
                db.update("appointments", values, "id = ?", arrayOf(appointmentId.toString()))
                appointmentId
            }
            id to null
        } catch (e: Exception) {
            null to (e.message ?: "ثبت نوبت انجام نشد")
        }
    }

    private fun customerByPhone(phone: String): Long? =
        readableDatabase.rawQuery("SELECT id FROM customers WHERE phone = ? LIMIT 1", arrayOf(phone)).use { c ->
            if (c.moveToFirst()) c.getLong(0) else null
        }

    fun setAppointmentStatus(id: Long, status: AppointmentStatus) {
        val values = ContentValues().apply {
            put("status", status.value)
            put("updated_at", java.time.LocalDateTime.now().toString())
        }
        writableDatabase.update("appointments", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteAppointment(id: Long) {
        writableDatabase.delete("appointments", "id = ?", arrayOf(id.toString()))
    }

    private val visitSelect = """
        SELECT v.*, c.first_name || ' ' || c.last_name AS customer_name, c.phone,
               s.name AS service_name,
               COALESCE((SELECT SUM(mu.amount * mu.unit_price)
                         FROM material_usages mu WHERE mu.visit_id = v.id), 0) AS material_cost
        FROM visits v
        JOIN customers c ON c.id = v.customer_id
        LEFT JOIN services s ON s.id = v.service_id
    """.trimIndent()

    private fun visitFrom(c: Cursor) = Visit(
        id = c.long("id"),
        appointmentId = c.long("appointment_id"),
        customerId = c.long("customer_id"),
        serviceId = if (c.isNull(c.getColumnIndexOrThrow("service_id"))) null else c.long("service_id"),
        performedDate = c.text("performed_date"),
        finalAmount = c.long("final_amount"),
        durationMinutes = if (c.isNull(c.getColumnIndexOrThrow("duration_minutes"))) null
        else c.getInt(c.getColumnIndexOrThrow("duration_minutes")),
        assistantCount = c.int("assistant_count"),
        assistantCost = c.long("assistant_cost"),
        hairLengthCm = c.doubleOrNull("hair_length_cm"),
        hairVolume = c.textOrNull("hair_volume"),
        hairCondition = c.textOrNull("hair_condition"),
        workDone = c.textOrNull("work_done"),
        notes = c.textOrNull("notes"),
        materialCost = c.long("material_cost"),
        customerName = c.text("customer_name"),
        phone = c.text("phone"),
        serviceName = c.textOrNull("service_name"),
    )

    fun getVisitForAppointment(appointmentId: Long): Visit? =
        readableDatabase.rawQuery("$visitSelect WHERE v.appointment_id = ?", arrayOf(appointmentId.toString())).use { c ->
            if (c.moveToFirst()) visitFrom(c) else null
        }

    fun getVisit(id: Long): Visit? =
        readableDatabase.rawQuery("$visitSelect WHERE v.id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) visitFrom(c) else null
        }

    fun listCustomerVisits(customerId: Long): List<Visit> =
        readableDatabase.rawQuery(
            "$visitSelect WHERE v.customer_id = ? ORDER BY v.performed_date DESC, v.id DESC",
            arrayOf(customerId.toString()),
        ).use { c -> buildList { while (c.moveToNext()) add(visitFrom(c)) } }

    fun usages(visitId: Long): List<MaterialUsage> =
        readableDatabase.rawQuery(
            """
            SELECT mu.id, mu.visit_id, mu.material_id, m.name AS material_name,
                   mu.amount, mu.unit_price, mu.unit
            FROM material_usages mu
            JOIN materials m ON m.id = mu.material_id
            WHERE mu.visit_id = ?
            ORDER BY mu.id
            """.trimIndent(),
            arrayOf(visitId.toString()),
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    MaterialUsage(
                        id = c.long("id"),
                        visitId = c.long("visit_id"),
                        materialId = c.long("material_id"),
                        materialName = c.text("material_name"),
                        amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                        unitPrice = c.long("unit_price"),
                        unit = c.text("unit"),
                    ),
                )
            }
        }

    fun saveVisit(
        appointmentId: Long,
        serviceId: Long?,
        performedDate: String,
        finalAmount: Long,
        durationMinutes: Int?,
        assistantCount: Int,
        assistantCost: Long,
        hairLengthCm: Double?,
        hairVolume: String?,
        hairCondition: String?,
        workDone: String?,
        notes: String?,
        materials: List<MaterialInput>,
    ): String? {
        val appointment = getAppointment(appointmentId) ?: return "نوبت پیدا نشد"
        val serviceName = serviceId?.let { id ->
            services(true).firstOrNull { it.id == id }?.name
        }
        val splitEnd = isSplitEndService(serviceName)
        if (assistantCount < 0 || assistantCost < 0) return "تعداد دستیار و هزینه دستیار نمی‌تواند منفی باشد"
        if (splitEnd) {
            if (hairLengthCm == null || hairLengthCm <= 0) return "برای موخوره‌گیری، قد مو الزامی است"
            if (hairVolume.isNullOrBlank()) return "برای موخوره‌گیری، حجم مو الزامی است"
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val current = ContentValues().apply {
                put("customer_id", appointment.customerId)
                if (serviceId == null) putNull("service_id") else put("service_id", serviceId)
                put("performed_date", performedDate)
                put("final_amount", finalAmount)
                if (durationMinutes == null || durationMinutes <= 0) putNull("duration_minutes")
                else put("duration_minutes", durationMinutes)
                put("assistant_count", assistantCount)
                put("assistant_cost", assistantCost)
                if (hairLengthCm == null || hairLengthCm <= 0) putNull("hair_length_cm")
                else put("hair_length_cm", hairLengthCm)
                if (hairVolume.isNullOrBlank()) putNull("hair_volume") else put("hair_volume", hairVolume)
                if (hairCondition.isNullOrBlank()) putNull("hair_condition") else put("hair_condition", hairCondition.trim())
                if (workDone.isNullOrBlank()) putNull("work_done") else put("work_done", workDone.trim())
                if (notes.isNullOrBlank()) putNull("notes") else put("notes", notes.trim())
            }
            val existingId = db.rawQuery(
                "SELECT id FROM visits WHERE appointment_id = ?",
                arrayOf(appointmentId.toString()),
            ).use { c -> if (c.moveToFirst()) c.getLong(0) else null }
            val visitId: Long
            if (existingId == null) {
                current.put("appointment_id", appointmentId)
                visitId = db.insertOrThrow("visits", null, current)
            } else {
                current.put("updated_at", java.time.LocalDateTime.now().toString())
                db.update("visits", current, "id = ?", arrayOf(existingId.toString()))
                visitId = existingId
            }
            db.delete("material_usages", "visit_id = ?", arrayOf(visitId.toString()))
            if (!splitEnd) {
                for (item in materials) {
                    if (item.amount <= 0) continue
                    val material = materialById(item.materialId) ?: continue
                    val usage = ContentValues().apply {
                        put("visit_id", visitId)
                        put("material_id", material.id)
                        put("amount", item.amount)
                        put("unit_price", material.pricePerUnit)
                        put("unit", material.unit)
                    }
                    db.insertOrThrow("material_usages", null, usage)
                }
            }
            val update = ContentValues().apply {
                put("status", AppointmentStatus.DONE.value)
                if (serviceId != null) put("service_id", serviceId)
                put("updated_at", java.time.LocalDateTime.now().toString())
            }
            db.update("appointments", update, "id = ?", arrayOf(appointmentId.toString()))
            db.setTransactionSuccessful()
            return null
        } catch (e: Exception) {
            return e.message ?: "ثبت نتیجه انجام نشد"
        } finally {
            db.endTransaction()
        }
    }

    fun deleteVisitByAppointment(appointmentId: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("visits", "appointment_id = ?", arrayOf(appointmentId.toString()))
            val values = ContentValues().apply {
                put("status", AppointmentStatus.WAITING.value)
                put("updated_at", java.time.LocalDateTime.now().toString())
            }
            db.update("appointments", values, "id = ?", arrayOf(appointmentId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listCustomers(search: String? = null): List<CustomerItem> {
        val where = if (search.isNullOrBlank()) "" else
            "WHERE c.first_name LIKE ? OR c.last_name LIKE ? OR c.phone LIKE ?"
        val args = if (search.isNullOrBlank()) emptyArray() else {
            val q = "%${search.trim()}%"
            arrayOf(q, q, q)
        }
        val sql = """
            SELECT c.id, c.first_name, c.last_name, c.phone, c.notes,
                   (SELECT COUNT(*) FROM visits v WHERE v.customer_id = c.id) AS visits_count,
                   (SELECT COUNT(*) FROM appointments a WHERE a.customer_id = c.id) AS appointments_count,
                   COALESCE((SELECT SUM(v.final_amount) FROM visits v WHERE v.customer_id = c.id), 0) AS total_spent
            FROM customers c
            $where
            ORDER BY c.first_name ASC, c.last_name ASC
        """.trimIndent()
        return readableDatabase.rawQuery(sql, args).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    CustomerItem(
                        id = c.long("id"),
                        firstName = c.text("first_name"),
                        lastName = c.text("last_name"),
                        phone = c.text("phone"),
                        notes = c.textOrNull("notes"),
                        visitsCount = c.int("visits_count"),
                        appointmentsCount = c.int("appointments_count"),
                        totalSpent = c.long("total_spent"),
                    ),
                )
            }
        }
    }

    fun getCustomer(id: Long): Customer? =
        readableDatabase.rawQuery("SELECT * FROM customers WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (!c.moveToFirst()) null else Customer(
                id = c.long("id"),
                firstName = c.text("first_name"),
                lastName = c.text("last_name"),
                phone = c.text("phone"),
                notes = c.textOrNull("notes"),
                createdAt = c.textOrNull("created_at"),
            )
        }

    fun saveCustomer(
        id: Long?,
        firstName: String,
        lastName: String,
        phone: String,
        notes: String?,
    ): Pair<Long?, String?> {
        val first = firstName.trim()
        val last = lastName.trim()
        val mobile = phone.trim()
        if (first.isEmpty() || last.isEmpty() || mobile.isEmpty()) {
            return null to "نام، نام خانوادگی و شماره تماس الزامی است"
        }
        val duplicate = customerByPhone(mobile)
        if (duplicate != null && duplicate != id) {
            return null to "این شماره برای مشتری دیگری ثبت شده است"
        }
        val values = ContentValues().apply {
            put("first_name", first)
            put("last_name", last)
            put("phone", mobile)
            if (notes.isNullOrBlank()) putNull("notes") else put("notes", notes.trim())
        }
        return try {
            if (id == null) {
                val newId = writableDatabase.insertOrThrow("customers", null, values)
                newId to null
            } else {
                writableDatabase.update("customers", values, "id = ?", arrayOf(id.toString()))
                id to null
            }
        } catch (e: Exception) {
            null to (e.message ?: "ذخیره مشتری انجام نشد")
        }
    }

    fun deleteCustomer(id: Long) {
        writableDatabase.delete("customers", "id = ?", arrayOf(id.toString()))
    }

    fun services(activeOnly: Boolean = true): List<Service> {
        val sql = if (activeOnly) "SELECT * FROM services WHERE active = 1 ORDER BY sort, id"
        else "SELECT * FROM services ORDER BY sort, id"
        return readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    Service(
                        id = c.long("id"),
                        name = c.text("name"),
                        sort = c.int("sort"),
                        active = c.bool("active"),
                    ),
                )
            }
        }
    }

    fun listMaterials(includeInactive: Boolean = false): List<Material> {
        val sql = if (includeInactive) "SELECT * FROM materials ORDER BY id"
        else "SELECT * FROM materials WHERE active = 1 ORDER BY id"
        return readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    Material(
                        id = c.long("id"),
                        name = c.text("name"),
                        unit = c.text("unit"),
                        pricePerUnit = c.long("price_per_unit"),
                        notes = c.textOrNull("notes"),
                        active = c.bool("active"),
                    ),
                )
            }
        }
    }

    fun materialById(id: Long): Material? =
        readableDatabase.rawQuery("SELECT * FROM materials WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (!c.moveToFirst()) null else Material(
                id = c.long("id"),
                name = c.text("name"),
                unit = c.text("unit"),
                pricePerUnit = c.long("price_per_unit"),
                notes = c.textOrNull("notes"),
                active = c.bool("active"),
            )
        }

    fun saveMaterial(
        id: Long?,
        name: String,
        pricePerUnit: Long,
        unit: String,
        notes: String?,
    ): Pair<Long?, String?> {
        val materialName = name.trim()
        if (materialName.isEmpty()) return null to "نام ماده الزامی است"
        val values = ContentValues().apply {
            put("name", materialName)
            put("price_per_unit", pricePerUnit)
            put("unit", unit.ifBlank { "ml" })
            if (notes.isNullOrBlank()) putNull("notes") else put("notes", notes.trim())
        }
        return try {
            if (id == null) {
                val newId = writableDatabase.insertOrThrow("materials", null, values)
                newId to null
            } else {
                writableDatabase.update("materials", values, "id = ?", arrayOf(id.toString()))
                id to null
            }
        } catch (e: Exception) {
            null to (e.message ?: "ذخیره ماده انجام نشد")
        }
    }

    fun setMaterialActive(id: Long, active: Boolean) {
        val values = ContentValues().apply { put("active", if (active) 1 else 0) }
        writableDatabase.update("materials", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteMaterial(id: Long) {
        val db = writableDatabase
        val used = db.rawQuery(
            "SELECT COUNT(*) FROM material_usages WHERE material_id = ?",
            arrayOf(id.toString()),
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        if (used > 0) {
            setMaterialActive(id, false)
        } else {
            db.delete("materials", "id = ?", arrayOf(id.toString()))
        }
    }

    fun monthSummary(jy: Int, jm: Int): MonthSummary {
        val (start, end) = monthRange(jy, jm)
        var income = 0L
        var materialCost = 0L
        var assistantCost = 0L
        var visits = 0
        readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(v.final_amount), 0) AS income,
                   COALESCE(SUM((SELECT COALESCE(SUM(mu.amount * mu.unit_price), 0)
                                 FROM material_usages mu WHERE mu.visit_id = v.id)), 0) AS material_cost,
                   COALESCE(SUM(v.assistant_cost), 0) AS assistant_cost,
                   COUNT(*) AS visit_count
            FROM visits v
            WHERE v.performed_date BETWEEN ? AND ?
            """.trimIndent(),
            arrayOf(start, end),
        ).use { c ->
            if (c.moveToFirst()) {
                income = c.long("income")
                materialCost = c.long("material_cost")
                assistantCost = c.long("assistant_cost")
                visits = c.int("visit_count")
            }
        }
        val deposit = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(deposit), 0) FROM appointments WHERE appointment_date BETWEEN ? AND ? AND status != 'canceled'",
            arrayOf(start, end),
        ).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }
        return MonthSummary(jy, jm, income, deposit, materialCost, assistantCost, visits)
    }

    fun yearSummary(jy: Int): List<MonthSummary> = (1..12).map { monthSummary(jy, it) }

    fun serviceBreakdown(jy: Int): List<ServiceBreakdown> {
        val (start, end) = yearRange(jy)
        return readableDatabase.rawQuery(
            """
            SELECT COALESCE(s.name, 'بدون خدمت') AS name, COUNT(*) AS visit_count,
                   COALESCE(SUM(v.final_amount), 0) AS income
            FROM visits v
            LEFT JOIN services s ON s.id = v.service_id
            WHERE v.performed_date BETWEEN ? AND ?
            GROUP BY s.name
            ORDER BY income DESC
            """.trimIndent(),
            arrayOf(start, end),
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    ServiceBreakdown(c.text("name"), c.int("visit_count"), c.long("income")),
                )
            }
        }
    }

    fun materialBreakdown(jy: Int): List<MaterialBreakdown> {
        val (start, end) = yearRange(jy)
        return readableDatabase.rawQuery(
            """
            SELECT m.name, mu.unit, COALESCE(SUM(mu.amount), 0) AS amount,
                   COALESCE(SUM(mu.amount * mu.unit_price), 0) AS cost
            FROM material_usages mu
            JOIN materials m ON m.id = mu.material_id
            JOIN visits v ON v.id = mu.visit_id
            WHERE v.performed_date BETWEEN ? AND ?
            GROUP BY m.name, mu.unit
            ORDER BY cost DESC
            """.trimIndent(),
            arrayOf(start, end),
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    MaterialBreakdown(
                        c.text("name"),
                        c.text("unit"),
                        c.getDouble(c.getColumnIndexOrThrow("amount")),
                        c.long("cost"),
                    ),
                )
            }
        }
    }

    fun dashboardToday(): List<AppointmentItem> = listAppointments(date = todayIso())

    fun dashboardWaiting(): List<AppointmentItem> =
        listAppointments(status = AppointmentStatus.WAITING, from = todayIso()).take(6)

    fun customerCount(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM customers", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    fun yearOptions(): List<Int> {
        val current = currentJalaliYear()
        return (0..5).map { current - it }
    }

    fun hasUser(): Boolean =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM users", null).use { c ->
            c.moveToFirst() && c.getInt(0) > 0
        }

    fun currentUsername(): String? =
        readableDatabase.rawQuery("SELECT username FROM users ORDER BY id LIMIT 1", null).use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    fun setupUser(username: String, password: String): String? {
        if (hasUser()) return "حساب مدیر قبلاً ساخته شده است"
        val name = username.trim().lowercase(java.util.Locale.US)
        if (name.length < 3) return "نام کاربری حداقل ۳ کاراکتر باشد"
        if (password.length < 6) return "رمز عبور حداقل ۶ کاراکتر باشد"
        val salt = PasswordHasher.newSalt()
        val values = ContentValues().apply {
            put("username", name)
            put("salt", salt)
            put("password_hash", PasswordHasher.hash(password, salt))
        }
        return try {
            writableDatabase.insertOrThrow("users", null, values)
            null
        } catch (e: Exception) {
            e.message ?: "ساخت حساب انجام نشد"
        }
    }

    fun verifyUser(username: String, password: String): Boolean {
        val name = username.trim().lowercase(java.util.Locale.US)
        return readableDatabase.rawQuery(
            "SELECT salt, password_hash FROM users WHERE username = ? LIMIT 1",
            arrayOf(name),
        ).use { c ->
            c.moveToFirst() && PasswordHasher.verify(password, c.getString(0), c.getString(1))
        }
    }

    fun changePassword(currentPassword: String, newPassword: String): String? {
        val name = currentUsername() ?: return "حسابی وجود ندارد"
        if (!verifyUser(name, currentPassword)) return "رمز فعلی درست نیست"
        if (newPassword.length < 6) return "رمز جدید حداقل ۶ کاراکتر باشد"
        val salt = PasswordHasher.newSalt()
        val values = ContentValues().apply {
            put("salt", salt)
            put("password_hash", PasswordHasher.hash(newPassword, salt))
        }
        writableDatabase.update("users", values, "username = ?", arrayOf(name))
        return null
    }

    fun backupInfo(): String = "داده‌ها روی همین گوشی ذخیره می‌شوند"

    fun seedExtraServiceIfMissing() {
        val sort = services(false).size
        writableDatabase.execSQL(
            "INSERT OR IGNORE INTO services(name, sort) VALUES(?, ?)",
            arrayOf(SPLIT_END_SERVICE_NAME, sort),
        )
    }

    fun monthLabel(jy: Int, jm: Int): String = "${com.keratinsadeghi.salon.util.MONTH_NAMES[jm - 1]} ${jy}"

    fun todayLabel(): String = "${
        com.keratinsadeghi.salon.util.formatJalali(todayIso(), true)
    }"

    fun nextDates(): List<String> = (0..13).map { offset ->
        java.time.LocalDate.now().plusDays(offset.toLong()).toString()
    }

    fun dateForMonth(jy: Int, jm: Int, day: Int): String = jalaliToIso(jy, jm, day)
}
