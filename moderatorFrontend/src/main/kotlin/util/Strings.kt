package util

import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.UserPermission
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

  val undefined = LocalizedString(
    "Not set",
    "Nicht angegeben"
  )

  val apply = LocalizedString(
    "Apply",
    "Anwenden",
  )

  val other = LocalizedString(
    "Other",
    "Sonstige",
  )

  val invalid_email = LocalizedString(
    "Invalid email address",
    "Ungültige E-Mail Adresse",
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

  val location_delete_are_you_sure = LocalizedString(
    "Are you sure you want to delete this location? This will also delete all the access permissions linked with this location.",
    "Sind Sie sicher, dass Sie diesen Ort löschen möchten? Dadurch werden auch alle Zugangsberechtigungen gelöscht, die mit diesem Ort verknüpft sind."
  )

  val location_import = LocalizedString(
    "Import locations",
    "Orte importieren"
  )

  val print_checkout_code = LocalizedString(
    "Print check out QR Code",
    "Check-out QR Code drucken"
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

  val location_edited = LocalizedString(
    "Location successfully edited",
    "Ort erfolgreich bearbeitet"
  )

  val location_deleted = LocalizedString(
    "Location successfully deleted",
    "Ort erfolgreich gelöscht"
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

  val location_number_of_seats = LocalizedString(
    "Seat count",
    "Anzahl der Sitzplätze"
  )

  val location_number_of_seats_hint = LocalizedString(
    "Seat count at this location",
    "Anzahl der Sitzplätze an diesem Ort"
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

  val report_infection_date_tip = LocalizedString(
    "Search for check-ins between tracing start date and now.",
    "Durchsuche Check-Ins zwischen dem Tracing Start Datum und jetzt."
  )

  val report_infection_date = LocalizedString(
    "Tracing start date",
    "Tracing Start Datum"
  )

  val report_search = LocalizedString(
    "Trace contacts",
    "Kontakte zurückverfolgen"
  )

  val check_in = LocalizedString(
    "Check in",
    "Check-In"
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

  val report_checkin_seat = LocalizedString(
    "Seat",
    "Sitzplatz",
  )

  val report_checkin_filter = LocalizedString(
    "Filter",
    "Filter"
  )

  val report_checkin_seat_filter = LocalizedString(
    "Seat filter",
    "Sitzplatzfilter",
  )

  val report_checkin_add_filter_title = LocalizedString(
    "Add seat filter",
    "Sitzplatzfilter hinzufügen"
  )

  val report_checkin_add_filter_content = LocalizedString(
    "Please select those seats that are close to the seat of the infected person.",
    "Bitte wählen Sie jene Sitzplätze aus, die nahe zum Sitz der infizierten Person liegen."
  )

  val report_impacted_people = LocalizedString(
    "Potential contacts",
    "Potentielle Kontakte"
  )

  val report_affected_people = LocalizedString(
    "%s people (%s contacts) between %s and %s were potentially in contact with the infected person.",
    "%s Personen (%s Kontakte) zwischen %s und %s waren potentiell in Kontakt mit der infizierten Person."
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

  val login_register_email_not_valid = LocalizedString(
    "Email address is not valid.",
    "E-Mail Adresse ungültig."
  )

  val user_management = LocalizedString(
    "User Management",
    "Userverwaltung"
  )

  val user_administration_external_auth_provider = LocalizedString(
    "Users managed by LDAP.",
    "User werden durch LDAP verwaltet."
  )

  val user_name = LocalizedString(
    "Name",
    "Name"
  )

  val user_permissions = LocalizedString(
    "Permissions",
    "Berechtigungen"
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

  val user_delete_are_you_sure = LocalizedString(
    "Are you sure you want to delete this user? This will not delete locations or access permissions created by this user.",
    "Sind Sie sicher, dass Sie diesen Benutzer löschen möchten? Dies löscht keine vom Benutzer erstellen Orte oder Zugangsberechtigungen."
  )

  val user_created = LocalizedString(
    "User successfully created",
    "User erfolgreich erstellt"
  )

  val user_already_exists = LocalizedString(
    "User with this email already exists.",
    "Ein User mit dieser E-Mail Addresse existiert bereits."
  )

  val user_permission_edit_users = LocalizedString(
    "User & permission management",
    "Benutzer- & Rechteverwaltung"
  )

  val user_type_admin_action = LocalizedString(
    "Administration",
    "Administration"
  )

  val user_permission_edit_location = LocalizedString(
    "Location management",
    "Raumverwaltung"
  )

  val user_permission_view_checkins = LocalizedString(
    "View check-ins",
    "Check-ins einsehen"
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

  val user_permission_edit_own_access = LocalizedString(
    "Access management & guest check-in",
    "Zugangsverwaltung & Gast Check-In"
  )

  val user_permission_edit_all_access = LocalizedString(
    "Administration of all access managements & guest check-ins",
    "Administration von allen Zugangsverwaltungen & Gast Check-Ins"
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

  val guest_checkin = LocalizedString(
    "Guest check in",
    "Gast Check-In",
  )

  val guest_checkin_add_guest = LocalizedString(
    "Check in guest",
    "Gast einchecken",
  )

  val guest_checkin_not_yet_added_title = LocalizedString(
    "Currently no guests are checked in.",
    "Momentan sind keine Gäste eingecheckt.",
  )

  val guest_checkin_not_yet_added_subtitle = LocalizedString(
    "Click on \"check in guest\" in the top right corner to check in a guest.",
    "Klicken Sie rechts oben auf \"Gast einchecken\" um einen Gast einzuchecken.",
  )

  val guest_checkin_checkout_are_you_sure = LocalizedString(
    "Check out %s now?",
    "%s jetzt auschecken?",
  )

  val guest_checkin_email_must_not_be_empty = LocalizedString(
    "Please add the email address of the guest",
    "Bitte fügen Sie die E-Mail Adresse des Gasts hinzu",
  )

  val guest_checkin_select_seat = LocalizedString(
    "Please select a seat",
    "Bitte wählen Sie einen Sitzplatz",
  )

  val guest_checkin_checkout_successful = LocalizedString(
    "Check out successful",
    "Check-Out erfolgreich",
  )

  val guest_checkin_check_out = LocalizedString(
    "Check out",
    "Check-Out",
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
        "Die Authentifizierung kann desweiteren durch LDAP erfolgen. " +
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

  val user_created_account_details = LocalizedString(
    "Created new account",
    "Account wurde erstellt"
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
    "All configuration flags that are in the MongoDB database listed in collection \"configuration\" " +
        "can be changed by the administrator. " +
        "Those flags are persisted in the database even across application restarts and updates of this application. " +
        "Examples for configuration flags are the imprint URL, the base URL for scanning the QR codes " +
        "or the number of days after the check-in data is automatically deleted. " +
        "Please contact the Studo team for further assistance or if you need more configuration parameters.",
    "Alle Konfigurationsflags die in der MongoDB-Datenbank in Collection \"configuration\" " +
        "gelistet sind können vom Administrator geändert werden. " +
        "Diese Flags bleiben auch bei Neustarts und Aktualisierungen dieser Applikation in der Datenbank erhalten. " +
        "Beispiele für Konfigurationsflags sind die Impressums-URL, die Basis-URL für das Einscannen der QR Codes " +
        "oder die Anzahl der Tage nachdem die Check-In Daten automatisch gelöscht werden. " +
        "Bitte kontaktieren Sie das Studo Team für weitere Unterstützung oder falls Sie weitere " +
        "Konfigurationsparameter wünschen."
  )

  val live_check_ins = LocalizedString(
    "Live - currently checked in",
    "Live - derzeit eingecheckt",
  )

}

val UserPermission.localizedString: LocalizedString
  get() = when (this) {
    UserPermission.EDIT_USERS -> Strings.user_permission_edit_users
    UserPermission.EDIT_LOCATIONS -> Strings.user_permission_edit_location
    UserPermission.VIEW_CHECKINS -> Strings.user_permission_view_checkins
    UserPermission.EDIT_OWN_ACCESS -> Strings.user_permission_edit_own_access
    UserPermission.EDIT_ALL_ACCESS -> Strings.user_permission_edit_all_access
  }

val LocationAccessType.localizedString: LocalizedString
  get() = when (this) {
    LocationAccessType.FREE -> Strings.location_access_type_free
    LocationAccessType.RESTRICTED -> Strings.location_access_type_restricted
  }
