package com.studo.campusqr.auth

import com.moshbit.katerbase.MongoMainEntry
import com.moshbit.katerbase.equal
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.serverScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NameNotFoundException
import javax.naming.NamingEnumeration
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

class LdapAuth(private val ldapUrl: String) : AuthProvider {
  private lateinit var ldapSearchFilterList: List<String>
  private lateinit var ldapApplicationUserPrincipal: String
  private lateinit var ldapApplicationUserCredentials: String
  private lateinit var ldapGroupAttributeName: String
  private lateinit var ldapGroupFilter: String
  private lateinit var ldapGroupRegex: String
  private var ldapPrintDebugLogs: Boolean = false
  private var ldapTimeoutMs: Int = 0
  private lateinit var ldapDefaultUserPermissions: List<UserPermission>

  override suspend fun init() {
    runOnDb {
      ldapSearchFilterList = (getConfig("ldapSearchFilter") as String).split(";")
      ldapApplicationUserPrincipal = getConfig("ldapApplicationUserPrincipal")
      ldapApplicationUserCredentials = getConfig("ldapApplicationUserCredentials")
      ldapGroupAttributeName = getConfig("ldapGroupAttributeName")
      ldapGroupFilter = getConfig("ldapGroupFilter")
      ldapGroupRegex = getConfig("ldapGroupRegex")
      ldapPrintDebugLogs = getConfig("ldapPrintDebugLogs")
      ldapTimeoutMs = getConfig("ldapTimeoutMs")
      ldapDefaultUserPermissions =
        getConfig<String>("ldapDefaultUserPermissions").split(",").map { UserPermission.valueOf(it.trim()) }
    }

    debugLog("Search filters: $ldapSearchFilterList")

    automaticUserDisabling()
  }

  private fun debugLog(message: String) {
    if (ldapPrintDebugLogs) {
      println("LDAP debug log: $message")
    }
  }

  private fun getContextEnvironment(userPrincipal: String, password: String): Hashtable<String, Any?> {
    debugLog("Create context for $userPrincipal")

    return Hashtable<String, Any?>().apply {
      this[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
      this[Context.PROVIDER_URL] = ldapUrl
      this[Context.SECURITY_AUTHENTICATION] = "simple"
      this[Context.SECURITY_PRINCIPAL] = userPrincipal
      this[Context.SECURITY_CREDENTIALS] = password
      this["com.sun.jndi.ldap.read.timeout"] = ldapTimeoutMs.toString()
    }
  }

  private fun findUser(userPrincipal: String, context: InitialDirContext): Boolean {
    val controls = SearchControls().apply {
      returningAttributes = arrayOf(ldapGroupAttributeName)
      searchScope = SearchControls.OBJECT_SCOPE
    }

    try {
      val answer: NamingEnumeration<*> = context.search(userPrincipal, ldapGroupFilter, controls)
      debugLog("Username $userPrincipal found. Search groups with filter $ldapGroupFilter")

      var userIsInGroup = false

      while (answer.hasMore()) {
        val result = answer.next() as SearchResult
        debugLog("User found. Got group search result: $result")
        val groups: List<*> = result.attributes.get(ldapGroupAttributeName).all.toList()
        debugLog("Groups: $groups")
        if (groups.any { (it as? String)?.contains(ldapGroupRegex) == true }) {
          debugLog("Found group --> Allow login")
          userIsInGroup = true
        }
      }

      if (!userIsInGroup) {
        debugLog("No matching group found")
      }

      return userIsInGroup
    } catch (e: NameNotFoundException) {
      debugLog("Username $userPrincipal not found")
      return false // Username not found
    }
  }

  override suspend fun login(email: String, password: String): AuthProvider.Result {
    // Find user by one ldapSearchFilter
    var valid = false
    for (ldapSearchFilter in ldapSearchFilterList) {
      val userPrincipal = ldapSearchFilter.format(ldapEscape(email))

      valid = try {
        // Use provided user to authenticate
        val context = InitialDirContext(getContextEnvironment(userPrincipal, password))

        val userFound = findUser(userPrincipal, context)
        context.close()
        userFound

      } catch (e: AuthenticationException) {
        debugLog("Wrong username or password for $userPrincipal")
        false // Wrong username or password
      }

      if (valid) {
        break // We found the user in one ldapSearchFilter
      }
    }

    return if (!valid) {
      debugLog("Username/Password invalid for $email --> Don't allow login")
      AuthProvider.Result.InvalidCredentials
    } else {
      // Insert user if not yet created
      debugLog("Username/Password valid for $email --> Allow login")
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
            permissions = ldapDefaultUserPermissions.toSet()
          )
        }
      }
      AuthProvider.Result.Success(user)
    }
  }

  // Synchronize users so if a user in LDAP is disabled it gets also deleted in the campus-qr database
  private suspend fun automaticUserDisabling() = serverScope.launch {
    while (true) {
      try {
        // Use the service user to authenticate
        val context = InitialDirContext(getContextEnvironment(ldapApplicationUserPrincipal, ldapApplicationUserCredentials))

        var stillEnabledUsers = 0
        val disabledUsers = mutableListOf<String>()
        try {
          runOnDb {
            getCollection<BackendUser>().find().forEach { user ->

              // Find user by one ldapSearchFilter
              var valid = false
              for (ldapSearchFilter in ldapSearchFilterList) {
                val userPrincipal = ldapSearchFilter.format(ldapEscape(user.email))
                valid = findUser(userPrincipal, context)

                if (valid) {
                  break // We found the user in one ldapSearchFilter
                }
              }

              if (valid) {
                stillEnabledUsers++
              } else {
                // User does not exist any more or has not the given group any more -> Delete sessions and user
                MainDatabase.getCollection<SessionToken>().deleteMany(SessionToken::userId equal user._id)
                getCollection<BackendUser>().deleteOne(BackendUser::_id equal user._id)

                disabledUsers.add(user.email)
              }
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
    0x21, 0x20 -> false // exclamation and space
    else -> true
  }
}
