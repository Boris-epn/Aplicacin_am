package com.example.interfaces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interfaces.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users")
    fun count(): Int

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND pin = :pin LIMIT 1")
    fun login(email: String, pin: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getById(userId: Long): UserEntity?
}
