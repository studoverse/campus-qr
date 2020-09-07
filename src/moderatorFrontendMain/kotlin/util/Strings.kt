package util

import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.UserType
import com.studo.campusqr.common.utils.LocalizedString

object Strings {

  val edit = LocalizedString(
    "Edit",
    "Bearbeiten"
  )

  val delete = LocalizedString(
    "Delete",
    "Löschen"
  )

  val copy = LocalizedString(
    "Copy",
    "Kopieren"
  )

  val duplicate = LocalizedString(
    "Duplicate",
    "Duplizieren"
  )

  val locations_element_download_qr_code = LocalizedString(
    "Print / download QR code",
    "QR-Code drucken / herunterladen"
  )

  val locations_element_simulate_scan = LocalizedString(
    "Simulate QR code scan",
    "Scan des QR-Codes simulieren"
  )

  val locations_element_download_csv = LocalizedString(
    "Download check ins as Excel .csv file",
    "Check-Ins als Excel .csv Datei herunterladen"
  )

  val error_try_again = LocalizedString(
    "Something went wrong. Please try again.",
    "Etwas lief schief. Bitte versuchen Sie es nochmal."
  )

  val locations = LocalizedString(
    "Locations",
    "Orte"
  )

  val location_delete = LocalizedString(
    "Delete location",
    "Ort löschen"
  )

  val location_import = LocalizedString(
    "Import locations",
    "Orte importieren"
  )

  val print_all_qrcodes = LocalizedString(
    "Print all QR Codes",
    "Alle QR Codes drucken"
  )

  val location_import_details = LocalizedString(
    "If locations are accessible from the Campus Management System or are available as Excel list, " +
        "they can be added automatically as mass import by Studo. " +
        "Please contact the Studo team for more information.",
    "Sofern Orte vom Campus Management System abrufbar sind oder als Excel-Liste verfügbar sind, " +
        "können diese automatisch als Massenimport von Studo hinzugefügt werden. " +
        "Bitte kontaktieren Sie dazu das Studo Team für mehr Informationen."
  )

  val more_about_studo = LocalizedString(
    "More about Studo",
    "Mehr über Studo"
  )

  val location_create = LocalizedString(
    "Create new location",
    "Neuen Ort hinzufügen"
  )

  val location_created = LocalizedString(
    "Location successfully created",
    "Ort erfolgreich erstellt"
  )

  val location_name = LocalizedString(
    "Location",
    "Ort"
  )

  val location_access_type = LocalizedString(
    "Access Type",
    "Zugangsart"
  )

  val location_check_in_count = LocalizedString(
    "Total check ins",
    "Gesamtanzahl der Check-Ins"
  )

  val actions = LocalizedString(
    "Actions",
    "Aktionen"
  )

  val location_add = LocalizedString(
    "Create location",
    "Ort erstellen"
  )

  val location_edit = LocalizedString(
    "Edit location",
    "Ort bearbeiten"
  )

  val location_update = LocalizedString(
    "Save location",
    "Ort speichern"
  )

  val location_no_locations_title = LocalizedString(
    "No locations yet",
    "Noch keine Orte"
  )

  val location_access_type_free = LocalizedString(
    "Free",
    "Frei"
  )

  val location_access_type_restricted = LocalizedString(
    "Restricted",
    "Beschränkt"
  )

  val location_no_locations_subtitle = LocalizedString(
    "Click on \"create new location\" button in the top right corner to create the first location.",
    "Klicken Sie rechts oben auf \"Neuen Ort hinzufügen\" um den ersten Ort zu erstellen."
  )

  val location_create_name_cannot_be_empty = LocalizedString(
    "Name cannot be empty",
    "Name kann nicht leer sein"
  )
  val name = LocalizedString(
    "Name",
    "Name"
  )

  val parameter_cannot_be_empty_format = LocalizedString(
    "%s cannot be empty",
    "%s kann nicht leer sein"
  )

  val parameter_too_short_format = LocalizedString(
    "%s is too short",
    "%s ist zu kurz"
  )

  val report = LocalizedString(
    "Report Infection",
    "Infektion melden"
  )

  val report_email = LocalizedString(
    "E-mail address of the infected person",
    "E-Mail Adresse der infizierten Person"
  )

  val report_email_tip = LocalizedString(
    "Multiple e-mail addresses can be searched for by separating them with a comma.",
    "Mehrere E-Mail Adressen können gesucht werden, indem sie durch einen Beistrich getrennt angegeben werden."
  )

  val report_infection_date = LocalizedString(
    "Infection date",
    "Datum der Infektion"
  )

  val report_search = LocalizedString(
    "Trace contacts",
    "Kontakte zurückverfolgen"
  )

  val report_checkins = LocalizedString(
    "Check ins of the person",
    "Check-Ins der Person"
  )

  val report_checkin_email = LocalizedString(
    "E-mail Address",
    "E-Mail Adresse"
  )

  val report_checkin_date = LocalizedString(
    "Check in Date",
    "Check-In Zeitpunkt"
  )

  val report_checkin_location = LocalizedString(
    "Location",
    "Ort"
  )

  val report_affected_people = LocalizedString(
    "%s people between %s and %s were potentially in contact with the infected person.",
    "%s Personen zwischen %s und %s waren potentiell in Kontakt mit der infizierten Person."
  )

  val report_export_via_mail = LocalizedString(
    "Contact all affected people via e-mail",
    "Alle betroffene Personen per E-Mail kontaktieren"
  )

  val report_export_via_csv = LocalizedString(
    "Export all affected people via Excel .csv file",
    "Alle betroffene Personen als Excel .csv Datei exportieren"
  )

  val report_export_infected_user_checkins_csv = LocalizedString(
    "Export all check-ins of the infected person via Excel .csv file",
    "Alle Check-ins der infizierten Person als Excel .csv Datei exportieren"
  )

  val account_settings = LocalizedString(
    "My Account",
    "Mein Konto"
  )

  val logout = LocalizedString(
    "Logout",
    "Abmelden"
  )

  val back = LocalizedString(
    "Back",
    "Zurück"
  )

  val submit = LocalizedString(
    "Submit",
    "OK"
  )

  val next = LocalizedString(
    "Next",
    "Weiter"
  )

  val email_address = LocalizedString(
    "Email address",
    "E-Mail Adresse"
  )

  val network_error = LocalizedString(
    "Network error",
    "Netzwerkfehler"
  )

  val network_error_description = LocalizedString(
    "Could not establish a connection. Please ensure a good internet connection and reload the page.",
    "Verbindung konnte nicht hergestellt werden. Bitte stellen Sie eine gute Internetverbindung her und aktualisiere Sie die Seite."
  )

  val login_info = LocalizedString(
    "Information about Campus QR",
    "Informationen über Campus QR"
  )

  val login_unknown_error = LocalizedString(
    "An unknown error has occoured. Please try again or contact your administrator.",
    "Ein unbekannter Fehler ist aufgetreten. Bitte versuchen Sie es erneut oder kontaktieren Sie ihren Administrator."
  )

  val login_wrong_email_and_pw = LocalizedString(
    "Wrong email or password",
    "E-Mail Adresse oder Passwort ungültig"
  )

  val login_email_form_body = LocalizedString(
    "Staff login",
    "Login für Bedienstete"
  )

  val login_email_form_pw_label = LocalizedString(
    "Password",
    "Passwort"
  )

  val login_email_form_new_pw_label = LocalizedString(
    "New password",
    "Neues Passwort"
  )

  val login_login_button = LocalizedString(
    "Login",
    "Einloggen"
  )

  val login_forgot_pw_text = LocalizedString(
    "Forgot your password? Contact an administrator to have your password reset.",
    "Passwort vergessen? Wenden Sie sich an einen Administrator, um Ihr Passwort zurückzusetzen."
  )

  val login_reset_pw_link = LocalizedString(
    "Reset password",
    "Passwort zurücksetzen"
  )

  val login_create_account_link = LocalizedString(
    "Create new partner account",
    "Neuen Partner-Account erstellen"
  )

  val login_register_email_not_valid = LocalizedString(
    "Email address is not valid.",
    "E-Mail Adresse ungültig."
  )

  val user_management = LocalizedString(
    "User Management",
    "Userverwaltung"
  )

  val user_administration_hint1 = LocalizedString(
    "Access managers can view, assign and edit access permissions to existing locations to enable a university access control system.",
    "Zugangsverwalter können Zugangsberechtigungen zu bestehenden Orten einsehen, vergeben und bearbeiten, um eine Zugangskontrolle an der Hochschule zu ermöglichen."
  )

  val user_administration_hint2 = LocalizedString(
    "Moderators can additionally list and edit locations, download check-in data and report infections.",
    "Moderatoren können zusätzlich Orte auflisten und bearbeiten, Check-In-Daten herunterladen und Infektionen melden."
  )

  val user_administration_hint3 = LocalizedString(
    "Administrators can additionally create, delete and edit users (access managers, moderators and other administrators).",
    "Administratoren können zusätzlich Benutzer (Zugangsveralter, Moderatoren und weitere Administratoren) erstellen, löschen und bearbeiten."
  )

  val user_administration_external_auth_provider = LocalizedString(
    "Users managed by LDAP.",
    "User werden durch LDAP verwaltet."
  )

  val user_name = LocalizedString(
    "Name",
    "Name"
  )

  val user_permission = LocalizedString(
    "Permission",
    "Berechtigung"
  )

  val user_first_login_date = LocalizedString(
    "First login date",
    "Datum des ersten Logins"
  )

  val user_add = LocalizedString(
    "Create user",
    "User erstellen"
  )

  val user_edit = LocalizedString(
    "Edit user",
    "User bearbeiten"
  )

  val user_update = LocalizedString(
    "Save user",
    "User speichern"
  )

  val user_delete = LocalizedString(
    "Delete user",
    "User löschen"
  )

  val user_created = LocalizedString(
    "User successfully created",
    "User erfolgreich erstellt"
  )

  val user_already_exists = LocalizedString(
    "User with this email already exists.",
    "Ein User mit dieser E-Mail Addresse existiert bereits."
  )

  val user_type_admin = LocalizedString(
    "Administrator",
    "Administrator"
  )

  val user_type_admin_action = LocalizedString(
    "Administration",
    "Administration"
  )

  val user_type_moderator = LocalizedString(
    "Moderator",
    "Moderator"
  )

  val user_type_moderator_action = LocalizedString(
    "Moderation",
    "Moderation"
  )

  val access_control = LocalizedString(
    "Access control",
    "Zugangskontrolle"
  )

  val access_control_my = LocalizedString(
    "My authorizations",
    "Meine Genehmigungen"
  )

  val access_control_manager = LocalizedString(
    "Access manager",
    "Zugangsverwalter"
  )

  val access_control_export = LocalizedString(
    "Visitor list",
    "Besucherliste"
  )

  val access_control_create = LocalizedString(
    "Create access control",
    "Zugangskontrolle erstellen"
  )

  val access_control_save = LocalizedString(
    "Save access control",
    "Zugangskontrolle speichern"
  )

  val access_control_not_configured_yet = LocalizedString(
    "Access control not configured yet.",
    "Die Zugangskontrolle ist noch nicht konfiguriert."
  )

  val access_control_not_configured_yet_subtitle = LocalizedString(
    "Click on \"create access control\" button in the top right corner to create an access control.",
    "Klicken Sie rechts oben auf \"Zugangskontrolle erstellen\" um eine Zugangskontrolle zu erstellen."
  )

  val access_control_permitted_person = LocalizedString(
    "Permitted person",
    "Zugelassene Person"
  )

  val access_control_permitted_people = LocalizedString(
    "Permitted people",
    "Zugelassene Personen"
  )

  val access_control_note = LocalizedString(
    "Note",
    "Notiz"
  )

  val access_control_reason = LocalizedString(
    "Reason",
    "Begründung"
  )

  val access_control_time_slot = LocalizedString(
    "Time slot",
    "Zeitfenster"
  )

  val access_control_time_slots = LocalizedString(
    "Time slots",
    "Zeitfenster"
  )

  val access_control_time_slot_add = LocalizedString(
    "Add time slot",
    "Zeitfenster hinzufügen"
  )

  val access_control_time_slot_remove = LocalizedString(
    "Remove this timeslot",
    "Dieses Zeitfenster entfernen"
  )

  val access_control_from = LocalizedString(
    "From",
    "Von"
  )

  val access_control_to = LocalizedString(
    "To",
    "Bis"
  )

  val access_control_add_permitted_people = LocalizedString(
    "Add person",
    "Person hinzufügen"
  )

  val access_control_add_permitted_people_tip = LocalizedString(
    "Multiple e-mail addresses can be added by separating them with a comma.",
    "Mehrere E-Mail Adressen können hinzugefügt werden, indem sie durch einen Beistrich getrennt angegeben werden."
  )

  val access_control_created_successfully = LocalizedString(
    "Access control created successfully!",
    "Zugangskontrolle erfolgreich erstellt!"
  )

  val access_control_duplicated_successfully = LocalizedString(
    "Access control duplicated successfully!",
    "Zugangskontrolle erfolgreich dupliziert!"
  )

  val access_control_deleted_successfully = LocalizedString(
    "Access control deleted successfully!",
    "Zugangskontrolle erfolgreich gelöscht!"
  )

  val access_control_edited_successfully = LocalizedString(
    "Access control edited successfully!",
    "Zugangskontrolle erfolgreich bearbeitet!"
  )

  val access_control_delete_are_your_sure = LocalizedString(
    "Are you sure you want to delete this access control?",
    "Sind Sie sicher, dass Sie diese Zugangskontrolle löschen möchten?"
  )

  val access_control_please_select_location = LocalizedString(
    "Please select a location",
    "Bitte wählen Sie einen Ort"
  )

  val access_control_end_date_before_start_date = LocalizedString(
    "End date cannot be before start date",
    "Das Enddatum kann nicht vor dem Anfangsdatum liegen"
  )

  val user_sso_info = LocalizedString(
    "Single Sign On",
    "Single Sign On"
  )

  val user_sso_info_details1 = LocalizedString(
    "To connect this web application to the Single Sign On system used at the university or to enable an " +
        "SSO login via the Campus Management System, " +
        "please contact the developers of this application directly. " +
        "The authentication can also be done by LDAP. " +
        "The Studo team can integrate a customized SSO solution in coordination with the university.",
    "Um diese Web Applikation mit dem an der Hochschule verwendeten Single Sign On System zu verbinden bzw. " +
        "einen SSO-Login über das Campus Management System zu ermöglichen, " +
        "kontaktieren Sie bitte direkt die Entwickkler dieser Applikation. " +
        "Die Authentifizierung kann desweiteren durch LDAP erfolgen." +
        "Das Studo Team kann in Abstimmung der Hochschule eine für Sie zugeschnitte SSO-Lösung integrieren."
  )

  val user_sso_info_details2 = LocalizedString(
    "Further customization requests for your university can also be ordered directly from your Studo contact person.",
    "Weiter Anpassungswünsche für Ihre Hochschule können auch direkt bei Ihrem Studo Ansprechpartner beauftragt werden."
  )

  val user_updated_account_details = LocalizedString(
    "Updated account details",
    "Account Details wurden aktualisiert"
  )

  val admin_info = LocalizedString(
    "App Configuration",
    "App-Konfiguration"
  )

  val admin_info_configuration = LocalizedString(
    "Configuration flags",
    "Konfigurations-Flags"
  )

  val admin_info_configuration_details = LocalizedString(
    "All configration flags that are in the MongoDB database listed in collection \"configuration\" " +
        "can be changed by the administrator. " +
        "Those flags are persisted in the database even across application restarts and updates of this application. " +
        "Examples for configuration flags are the imprint URL, the base URL for scanning the QR codes " +
        "or the number of days after the check-in data is automatically deleted. " +
        "Please contact the Studo team for further assistance or if you need more configration parameters.",
    "Alle Konfigurationsflags die in der MongoDB-Datenbank in Collection \"configuration\" " +
        "gelistet sind können vom Administrator geändert werden. " +
        "Diese Flags bleiben auch bei Neustarts und Aktualisierungen dieser Applikation in der Datenbank erhalten. " +
        "Beispiele für Konfigurationsflags sind die Impressums-URL, die Basis-URL für das Einscannen der QR Codes " +
        "oder die Anzahl der Tage nachdem die Check-In Daten automatisch gelöscht werden. " +
        "Bitte kontaktieren Sie das Studo Team für weitere Unterstützung oder falls Sie weitere " +
        "Konfigurationsparameter wünschen."
  )
}

val UserType.localizedString: LocalizedString
  get() = when (this) {
    UserType.ADMIN -> Strings.user_type_admin
    UserType.MODERATOR -> Strings.user_type_moderator
    UserType.ACCESS_MANAGER -> Strings.access_control_manager
  }

val UserType.localizedStringAction: LocalizedString
  get() = when (this) {
    UserType.ADMIN -> Strings.user_type_admin_action
    UserType.MODERATOR -> Strings.user_type_moderator_action
    UserType.ACCESS_MANAGER -> Strings.access_control
  }

val LocationAccessType.localizedString: LocalizedString
  get() = when (this) {
    LocationAccessType.FREE -> Strings.location_access_type_free
    LocationAccessType.RESTRICTED -> Strings.location_access_type_restricted
  }