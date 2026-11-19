package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.OrderStatus
import com.example.data.model.PartCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppTypeConverters {
    @TypeConverter
    fun fromCategory(value: PartCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): PartCategory =
        PartCategory.entries.firstOrNull { it.name == value } ?: PartCategory.OTHER

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus =
        OrderStatus.entries.firstOrNull { it.name == value } ?: OrderStatus.PENDING
}

@Database(
    entities = [
        PartEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partDao(): PartDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "mobile_parts_store.db"
            )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(appContext))
                        }
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(appContext))
                        }
                    }
                })
                .build()
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val partDao = database.partDao()
            if (partDao.getPartsCount() > 0) return

            val now = System.currentTimeMillis()
            val seedParts = listOf(
                // شاشات
                PartEntity(
                    name = "شاشة آيفون 15 برو ماكس OLED",
                    compatibleModels = "iPhone 15 Pro Max",
                    category = PartCategory.SCREENS,
                    price = 135.0,
                    stockQuantity = 12,
                    qualityGrade = "أصلي مسحوب / Super OLED",
                    warrantyDays = 90,
                    description = "شاشة OLED كاملة مع الإطار ودعم معدل التحديث 120Hz و True Tone.",
                    createdAt = now - 12_000
                ),
                PartEntity(
                    name = "شاشة سامسونج S24 الترا أموليد",
                    compatibleModels = "Galaxy S24 Ultra (S928B)",
                    category = PartCategory.SCREENS,
                    price = 145.0,
                    stockQuantity = 7,
                    qualityGrade = "Service Pack أصلي",
                    warrantyDays = 90,
                    description = "شاشة Dynamic AMOLED 2X أصلية مع فريم وبصمة سريعة الاستجابة.",
                    createdAt = now - 11_000
                ),
                PartEntity(
                    name = "شاشة آيفون 13 برو / 13 عادي",
                    compatibleModels = "iPhone 13 / 13 Pro",
                    category = PartCategory.SCREENS,
                    price = 68.0,
                    stockQuantity = 18,
                    qualityGrade = "GX Hard OLED",
                    warrantyDays = 60,
                    description = "سطوع عالي وألوان دقيقة مع طبقة حماية مقاومة للخدش ومطابقة للأصلية.",
                    createdAt = now - 10_000
                ),
                PartEntity(
                    name = "شاشة شاومي ريدمي نوت 13 برو",
                    compatibleModels = "Redmi Note 13 Pro 4G/5G",
                    category = PartCategory.SCREENS,
                    price = 38.0,
                    stockQuantity = 22,
                    qualityGrade = "AMOLED درجة أولى",
                    warrantyDays = 45,
                    description = "شاشة AMOLED ممتازة متوافقة مع مستشعر البصمة تحت الشاشة.",
                    createdAt = now - 9_000
                ),

                // بطاريات
                PartEntity(
                    name = "بطارية آيفون 14 برو ماكس",
                    compatibleModels = "iPhone 14 Pro Max (4323 mAh)",
                    category = PartCategory.BATTERIES,
                    price = 24.0,
                    stockQuantity = 25,
                    qualityGrade = "أصلي مع IC بدون رسالة",
                    warrantyDays = 180,
                    description = "بطارية عالية السعة 4323mAh مزودة بشريحة تحكم مستقرة وصفر دورات شحن.",
                    createdAt = now - 8_000
                ),
                PartEntity(
                    name = "بطارية سامسونج A54 / S23",
                    compatibleModels = "Galaxy A54 5G (EB-BA546ABY)",
                    category = PartCategory.BATTERIES,
                    price = 17.5,
                    stockQuantity = 30,
                    qualityGrade = "أصلي 5000mAh",
                    warrantyDays = 180,
                    description = "بطارية ليثيوم بوليمر أصلية تدعم الشحن السريع 25W مع لاصق التثبيت.",
                    createdAt = now - 7_000
                ),
                PartEntity(
                    name = "بطارية آيفون 11 / XR محسّنة",
                    compatibleModels = "iPhone 11 (3110 mAh)",
                    category = PartCategory.BATTERIES,
                    price = 15.0,
                    stockQuantity = 35,
                    qualityGrade = "High Capacity +15%",
                    warrantyDays = 120,
                    description = "بطارية بسعة إضافية وموثوقية عالية لأداء يوم كامل.",
                    createdAt = now - 6_000
                ),

                // فلاتات
                PartEntity(
                    name = "فلاتة شحن وميكروفون آيفون 15",
                    compatibleModels = "iPhone 15 / 15 Plus (USB-C)",
                    category = PartCategory.FLEX_CABLES,
                    price = 18.0,
                    stockQuantity = 20,
                    qualityGrade = "أصلي OEM",
                    warrantyDays = 60,
                    description = "فلاتة منفذ Type-C كاملة مع الميكروفون السفلي ودعم نقل البيانات والشحن السريع.",
                    createdAt = now - 5_000
                ),
                PartEntity(
                    name = "فلاتة توصيل شاشة سامسونج A53/A73",
                    compatibleModels = "Galaxy A53 / A73 5G",
                    category = PartCategory.FLEX_CABLES,
                    price = 8.5,
                    stockQuantity = 40,
                    qualityGrade = "أصلي",
                    warrantyDays = 30,
                    description = "فلاتة ربط رئيسية بين البوردة والشاشة بموصلات ذهبية دقيقة.",
                    createdAt = now - 4_000
                ),
                PartEntity(
                    name = "فلاتة زر باور وبصمة شاومي نوت 12",
                    compatibleModels = "Redmi Note 12 / Note 12S",
                    category = PartCategory.FLEX_CABLES,
                    price = 6.5,
                    stockQuantity = 28,
                    qualityGrade = "درجة أولى",
                    warrantyDays = 30,
                    description = "فلاتة تشغيل وبصمة جانبية سريعة الاستجابة وسهلة التركيب.",
                    createdAt = now - 3_000
                ),

                // قطع أخرى
                PartEntity(
                    name = "كاميرا خلفية رئيسية آيفون 14 برو",
                    compatibleModels = "iPhone 14 Pro / 14 Pro Max",
                    category = PartCategory.OTHER,
                    price = 62.0,
                    stockQuantity = 8,
                    qualityGrade = "أصلي مسحوب 48MP",
                    warrantyDays = 90,
                    description = "وحدة كاميرا خلفية ثلاثية العدسات مجربة ومختبرة بتركيز تلقائي ممتاز.",
                    createdAt = now - 2_000
                ),
                PartEntity(
                    name = "سماعة أذن علوية مع فلاتة سنسور",
                    compatibleModels = "iPhone 13 / 13 Mini",
                    category = PartCategory.OTHER,
                    price = 14.0,
                    stockQuantity = 16,
                    qualityGrade = "أصلي",
                    warrantyDays = 60,
                    description = "سماعة مكالمات علوية نقية الصوت مع مستشعر الإضاءة والقرب.",
                    createdAt = now - 1_000
                ),
                PartEntity(
                    name = "غطاء ظهر زجاجي أصلي (فتحة واسعة)",
                    compatibleModels = "iPhone 13 Pro Max - أسود جرافيت",
                    category = PartCategory.OTHER,
                    price = 11.5,
                    stockQuantity = 24,
                    qualityGrade = "زجاج مقوى",
                    warrantyDays = 14,
                    description = "باك جلاس بفتحة كاميرا كبيرة لتركيب سريع دون الحاجة لفك حلقة الكاميرا.",
                    createdAt = now - 500
                )
            )
            partDao.insertParts(seedParts)

            // Seed 1 sample order so Admin Orders Dashboard displays immediate structure
            val orderDao = database.orderDao()
            val sampleOrderId = orderDao.insertOrder(
                OrderEntity(
                    orderNumber = "ORD-1041",
                    customerName = "أحمد السعيد",
                    customerPhone = "0791234567",
                    workshopName = "ورشة النخبة لصيانة الجوال",
                    deliveryAddress = "عمّان - شارع الجاردنز - مجمع التقنية محل 14",
                    notes = "التغليف الجيد للشاشة واختبار الفلاتة قبل الإرسال",
                    status = OrderStatus.PROCESSING,
                    totalAmount = 153.0,
                    totalItems = 2,
                    createdAt = now - 3_600_000L,
                    updatedAt = now - 1_800_000L
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItemEntity(
                        orderId = sampleOrderId,
                        partId = 1L,
                        partName = "شاشة آيفون 15 برو ماكس OLED",
                        compatibleModels = "iPhone 15 Pro Max",
                        category = PartCategory.SCREENS,
                        unitPrice = 135.0,
                        quantity = 1
                    ),
                    OrderItemEntity(
                        orderId = sampleOrderId,
                        partId = 8L,
                        partName = "فلاتة شحن وميكروفون آيفون 15",
                        compatibleModels = "iPhone 15 / 15 Plus (USB-C)",
                        category = PartCategory.FLEX_CABLES,
                        unitPrice = 18.0,
                        quantity = 1
                    )
                )
            )
        }
    }
}
