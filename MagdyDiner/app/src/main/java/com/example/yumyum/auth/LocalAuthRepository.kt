package com.example.yumyum.auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.room.Room
import com.example.yumyum.data.local.AppDatabase
import com.example.yumyum.data.local.UserDao
import com.example.yumyum.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Very small local auth repository used for this simple app.
 * It stores the current user id in SharedPreferences and can create a simple user record.
 * 0 means guest (no user).
 */
class LocalAuthRepository(
    private val context: Context,
    initialUserDao: UserDao
) : AuthRepository {

    // Use a mutable reference to the DAO so we can swap it to a rebuilt DB's DAO if needed
    private var daoRef: UserDao = initialUserDao

    // A background scope for repository-internal tasks
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Kick off an asynchronous health check of the DB so we can recover early if it's corrupt/closed
        repoScope.launch {
            ensureDbHealthy()
        }
    }

    // Attempt a lightweight read to ensure the DAO/database is usable; if not, attempt rebuild and swap daoRef
    private suspend fun ensureDbHealthy() {
        try {
            // Use id = 0 as a lightweight probe; DAO methods may throw if DB is closed/corrupt
            try {
                daoRef.getById(0L)
                // healthy
                return
            } catch (probeEx: Exception) {
                if (!probeEx.message.indicatesCorruptionOrClosed()) {
                    // Not a corruption/closed error; nothing we can proactively fix here
                    return
                }
                // Attempt to remove the DB file and rebuild it (matches the signUp recovery path)
                try {
                    context.deleteDatabase("app_db")
                } catch (_: Exception) {
                    // ignore
                }
                try {
                    val rebuiltDb = Room.databaseBuilder(context, AppDatabase::class.java, "app_db").fallbackToDestructiveMigration().build()
                    val rebuiltUserDao = rebuiltDb.userDao()
                    // Quick sanity insert-read to ensure it's working
                    val testId = try {
                        rebuiltUserDao.insert(UserEntity(email = "__probe__@local", displayName = "Probe"))
                    } catch (_: Exception) {
                        -1L
                    }
                    if (testId > 0) {
                        daoRef = rebuiltUserDao
                        return
                    }
                } catch (_: Exception) {
                    // ignore; we'll leave the original daoRef and surface errors later when used
                }
            }
        } catch (_: Exception) {
            // swallow; health check should not crash app
        }
    }

    private val prefs by lazy { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    override suspend fun getCurrentUserId(): Long = withContext(Dispatchers.IO) {
        prefs.getLong("current_user_id", 0L)
    }

    override suspend fun setCurrentUserId(id: Long) = withContext(Dispatchers.IO) {
        prefs.edit { putLong("current_user_id", id) }
        // ensure user exists in DB if id != 0
        if (id != 0L) {
            try {
                val existing = daoRef.getById(id)
                if (existing == null) {
                    // insert a placeholder user (no email) to reserve the id
                    daoRef.insert(UserEntity(id = id, email = "user_$id@local", displayName = "User $id"))
                }
            } catch (_: Exception) {
                // ignore DB exceptions for this minimal local implementation
            }
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Helper to detect corruption/closed DB messages (copied logic from OrderRepository)
    private fun String?.indicatesCorruptionOrClosed(): Boolean {
        val msg = this?.lowercase() ?: return false
        return msg.contains("closed") || msg.contains("verify the data integrity") || msg.contains("disk image is malformed") || msg.contains("malformed")
    }

    override suspend fun signUp(username: String, password: String, displayName: String?, email: String, phoneNumber: String?, address: String?): Long = withContext(Dispatchers.IO) {
        try {
            Log.d("LocalAuthRepo", "signUp called: username='${username}', email='${email}'")

            // Don't allow duplicate username or email
            val byUsername = daoRef.getByUsername(username)
            if (byUsername != null) {
                Log.d("LocalAuthRepo", "signUp failed: username exists (id=${byUsername.id})")
                // return existing id (or we could indicate error). For now, return 0 to indicate failure
                return@withContext 0L
            }
            val byEmail = daoRef.getByEmail(email)
            if (byEmail != null) {
                Log.d("LocalAuthRepo", "signUp failed: email exists (id=${byEmail.id})")
                return@withContext 0L
            }

            val pwHash = hashPassword(password)
            val newUser = UserEntity(
                email = email,
                username = username,
                passwordHash = pwHash,
                displayName = displayName,
                phoneNumber = phoneNumber,
                address = address
            )

            // Try inserting using the current DAO first
            val id = try {
                daoRef.insert(newUser)
            } catch (e: Exception) {
                Log.e("LocalAuthRepo", "Exception inserting user via current DAO", e)
                // If the exception suggests the DB is closed/corrupt, attempt to recreate the disk DB and retry
                if (e.message.indicatesCorruptionOrClosed()) {
                    Log.w("LocalAuthRepo", "Detected closed/corrupt DB while inserting user; attempting to recreate DB and retry")
                    try {
                        // Delete the possibly-corrupt DB file; this makes Room recreate it
                        context.deleteDatabase("app_db")
                    } catch (delEx: Exception) {
                        Log.w("LocalAuthRepo", "Failed to delete app_db file", delEx)
                    }
                    try {
                        val rebuiltDb = Room.databaseBuilder(context, AppDatabase::class.java, "app_db").fallbackToDestructiveMigration().build()
                        val rebuiltUserDao = rebuiltDb.userDao()
                        val retryId = try {
                            rebuiltUserDao.insert(newUser)
                        } catch (retryEx: Exception) {
                            Log.e("LocalAuthRepo", "Retry insert failed on rebuilt DB", retryEx)
                            -1L
                        }
                        Log.d("LocalAuthRepo", "rebuild insert returned id=$retryId")
                        if (retryId > 0) {
                            // swap the DAO ref so future calls use the rebuilt DB
                            daoRef = rebuiltUserDao
                            return@withContext retryId
                        }
                        // If retry failed, fall through and return 0
                    } catch (rebuildEx: Exception) {
                        Log.e("LocalAuthRepo", "Failed to rebuild disk DB", rebuildEx)
                    }
                }
                -1L
            }

            Log.d("LocalAuthRepo", "insert returned id=$id")
            // Do NOT auto-login on sign up; let the user explicitly sign in.
            return@withContext if (id > 0) id else 0L
        } catch (e: Exception) {
            Log.e("LocalAuthRepo", "signUp unexpected failure", e)
            return@withContext 0L
        }
    }

    override suspend fun signIn(username: String, password: String): Long = withContext(Dispatchers.IO) {
        try {
            Log.d("LocalAuthRepo", "signIn called: username='${username}'")

            val existing = daoRef.getByUsername(username)
            Log.d("LocalAuthRepo", "getByUsername returned: ${existing?.id}")
            if (existing != null && existing.passwordHash != null) {
                val pwHash = hashPassword(password)
                if (existing.passwordHash == pwHash) {
                    prefs.edit { putLong("current_user_id", existing.id) }
                    Log.d("LocalAuthRepo", "signIn successful: id=${existing.id}")
                    return@withContext existing.id
                }
                Log.d("LocalAuthRepo", "signIn failed: password mismatch")
                return@withContext 0L
            }
            Log.d("LocalAuthRepo", "signIn failed: user not found or no password hash")
            return@withContext 0L
        } catch (e: Exception) {
            Log.e("LocalAuthRepo", "signIn exception: ${e.javaClass.simpleName}: ${e.message}", e)
            // If DB is closed or corrupt, try to recover
            if (e.message.indicatesCorruptionOrClosed()) {
                try {
                    context.deleteDatabase("app_db")
                    val rebuiltDb = Room.databaseBuilder(context, AppDatabase::class.java, "app_db").fallbackToDestructiveMigration().build()
                    daoRef = rebuiltDb.userDao()
                    Log.w("LocalAuthRepo", "Database rebuilt after signIn error")
                } catch (rebuildEx: Exception) {
                    Log.e("LocalAuthRepo", "Failed to rebuild DB in signIn", rebuildEx)
                }
            }
            return@withContext 0L
        }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        prefs.edit { putLong("current_user_id", 0L) }
    }

    override suspend fun getUserById(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        try {
            daoRef.getById(id)
        } catch (_: Exception) {
            null
        }
    }

    // Check whether a username exists in the DB
    suspend fun usernameExistsInternal(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            daoRef.getByUsername(username) != null
        } catch (_: Exception) {
            false
        }
    }

    // Update a user's password: verify old password, then write new hashed password
    suspend fun updatePasswordInternal(username: String, oldPassword: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = daoRef.getByUsername(username)
            if (existing == null || existing.passwordHash == null) return@withContext false
            val oldHash = hashPassword(oldPassword)
            if (existing.passwordHash != oldHash) return@withContext false
            val newHash = hashPassword(newPassword)
            val updated = daoRef.updatePasswordHashByUsername(username, newHash)
            return@withContext updated > 0
        } catch (e: Exception) {
            Log.e("LocalAuthRepo", "updatePasswordInternal failed", e)
            return@withContext false
        }
    }

    // --- Added implementations for AuthRepository abstract members ---
    // Delegate to the internal helpers so existing logic (DB rebuild attempts, hashing) is reused.
    override suspend fun usernameExists(username: String): Boolean = usernameExistsInternal(username)

    override suspend fun updatePassword(username: String, oldPassword: String, newPassword: String): Boolean = updatePasswordInternal(username, oldPassword, newPassword)
}
