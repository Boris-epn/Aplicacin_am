package com.example.interfaces.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.interfaces.data.local.dao.AppointmentDao
import com.example.interfaces.data.local.dao.DoctorDao
import com.example.interfaces.data.local.dao.SpecialtyDao
import com.example.interfaces.data.local.dao.UserDao
import com.example.interfaces.data.local.entity.AppointmentEntity
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.local.entity.SpecialtyEntity
import com.example.interfaces.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SpecialtyEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VitusDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: VitusDatabase? = null

        fun getInstance(context: Context): VitusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VitusDatabase::class.java,
                    "vitus_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
