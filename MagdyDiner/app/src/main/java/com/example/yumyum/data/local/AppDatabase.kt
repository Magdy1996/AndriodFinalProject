package com.example.yumyum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.yumyum.data.local.entities.OrderEntity
import com.example.yumyum.data.local.entities.UserEntity

@Database(entities = [OrderEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
}
