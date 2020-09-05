package com.studo.campusqr.auth

import com.studo.campusqr.common.UserType
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.serverScope
import com.studo.katerbase.MongoMainEntry
import com.studo.katerbase.equal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NameNotFoundException
import javax.naming.directory.InitialDirContext


class LdapAuth(val ldapUrl: String) : AuthProvider {
  lateinit var ldapSearchFilter: String
  lateinit var ldapApplicationUserPrincipal: String
  lateinit var ldapApplicationUserCredentials: String

  override suspend fun init() {
    runOnDb {
      ldapSearchFilter = getConfig("ldapSearchFilter")
      ldapApplicationUserPrincipal = getConfig("ldapApplicationUserPrincipal")
      ldapApplicationUserCredentials = getConfig("ldapApplicationUserCredentials")
    }
    automaticUserDisabling()
  }

  override suspend fun login(email: String, password: String): AuthProvider.Result {
    // Use provided user to authenticate
    val env = Hashtable<String, Any?>()
    env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
    env[Context.PROVIDER_URL] = ldapUrl
    env[Context.SECURITY_AUTHENTICATION] = "simple"
    env[Context.SECURITY_PRINCIPAL] = ldapSearchFilter.format(ldapEscape(email))
    env[Context.SECURITY_CREDENTIALS] = password

    val valid = try {
      val context = InitialDirContext(env)
      context.close()
      true
    } catch (e: NameNotFoundException) {
      false // Username not found
    } catch (e: AuthenticationException) {
      false // Password wrong
    }

    return if (!valid) {
      AuthProvider.Result.InvalidCredentials
    } else {
      // Insert user if not yet created
      val userId = MongoMainEntry.generateId(email)
      val user = runOnDb {
        getCollection<BackendUser>().findOneOrInsert(BackendUser::_id equal userId) {
          BackendUser(
            userId = userId,
            email = email,
            name = email
              .substringBefore("@")
              .replace(".", " ")
              .split(" ")
              .joinToString(separator = " ", transform = { it.capitalize() }),
            type = UserType.MODERATOR // TODO
          ).apply {

          }
        }
      }
      AuthProvider.Result.Success(user)
    }
  }

  suspend fun automaticUserDisabling() = serverScope.launch {
    while (true) {
      try {
        // Use the service user to authenticate
        val env = Hashtable<String, Any?>()
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        env[Context.PROVIDER_URL] = ldapUrl
        env[Context.SECURITY_AUTHENTICATION] = "simple"
        env[Context.SECURITY_PRINCIPAL] = ldapApplicationUserPrincipal
        env[Context.SECURITY_CREDENTIALS] = ldapApplicationUserCredentials

        val context = InitialDirContext(env)
        var stillEnabledUsers = 0
        val disabledUsers = mutableListOf<String>()
        try {
          listOf("gauss", "asdfasdfasdf").forEach { email -> // TODO iterate over all users
            try {
              context.lookup(ldapSearchFilter.format(ldapEscape(email)))
              stillEnabledUsers++
            } catch (e: NameNotFoundException) {
              // TODO disable user
              disabledUsers.add(email)
            }
          }
        } finally {
          context.close()
        }
        println("$stillEnabledUsers LDAP users active. The following users were automatically disabled: $disabledUsers")
      } catch (e: Exception) {
        println(e) // Don't crash on short-term database connection problems but make sure we still run the automaticDataDeletion
      }

      delay(runOnDb { getConfig("ldapUserDisablingIntervalMinutes", Int::class) * 60_000L })

    }
  }
}

private fun ldapEscape(string: String): String {
  for (index in 0..string.lastIndex) {
    val character = string[index]
    if (character.shouldEscape()) {
      return ldapEscapeImpl(string, index)
    }
  }

  return string
}

private fun ldapEscapeImpl(string: String, firstIndex: Int): String = buildString {
  var lastIndex = 0
  for (index in firstIndex..string.lastIndex) {
    val character = string[index]
    if (character.shouldEscape()) {
      append(string, lastIndex, index)
      if (character in ESCAPE_CHARACTERS) {
        append('\\')
        append(character)
      } else {
        character.toString().toByteArray().let { encoded ->
          for (byteIndex in 0 until encoded.size) {
            val unsignedValue = encoded[byteIndex].toInt() and 0xff
            append('\\')
            append(unsignedValue.toString(16).padStart(2, '0'))
          }
        }
      }

      lastIndex = index + 1
    }
  }

  append(string, lastIndex, string.length)
}

private val ESCAPE_CHARACTERS = charArrayOf(' ', '"', '#', '+', ',', ';', '<', '=', '>', '\\')

private fun Char.shouldEscape(): Boolean = this.toInt().let { codepoint ->
  when (codepoint) {
    in 0x3f..0x7e -> codepoint == 0x5c // the only forbidden character is backslash
    in 0x2d..0x3a -> false // minus, point, slash (allowed), digits + colon :
    in 0x24..0x2a -> false // $%&'()*
    0x21 -> false // exclamation
    else -> true
  }
}
