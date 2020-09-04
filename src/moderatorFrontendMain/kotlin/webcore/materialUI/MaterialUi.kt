package webcore.materialUI

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import kotlin.reflect.KClass


@JsModule("@material-ui/core/styles/colorManipulator")
private external val importedColorManipulator: dynamic
val fade: (color: dynamic, opacity: dynamic) -> dynamic = importedColorManipulator.fade

@JsModule("@material-ui/core/colors/red")
private external val importedRed: dynamic
val redColor: dynamic = importedRed.default

@JsModule("@material-ui/core/colors/blue")
private external val importedBlue: dynamic
val blueColor: dynamic = importedBlue.default

@JsModule("@material-ui/core/colors/yellow")
private external val importedYellow: dynamic
val yellowColor: dynamic = importedYellow.default

@JsModule("@material-ui/core/colors/purple")
private external val importedPurple: dynamic
val purpleColor: dynamic = importedPurple.default

@JsModule("@material-ui/core/colors/green")
private external val importedGreen: dynamic
val greenColor: dynamic = importedGreen.default

@JsModule("@material-ui/core/colors/orange")
private external val importedOrange: dynamic
val orangeColor: dynamic = importedOrange.default

@JsModule("@material-ui/core/colors/grey")
private external val importedGrey: dynamic
val greyColor: dynamic = importedGrey.default

@JsModule("@material-ui/core/colors/brown")
private external val importedBrown: dynamic
val brownColor: dynamic = importedBrown.default

@JsModule("@material-ui/core/colors/cyan")
private external val importedCyan: dynamic
val cyanColor: dynamic = importedCyan.default

@JsModule("@material-ui/core/colors/teal")
private external val importedTeal: dynamic
val tealColor: dynamic = importedTeal.default

@JsModule("@material-ui/core/AppBar")
private external val importedAppBar: dynamic

interface AppBarProps : RProps {
  var position: String
  var color: String
  var className: String
  var elevation: Int
}

val appBar: RClass<AppBarProps> = importedAppBar.default


@JsModule("@material-ui/core/Toolbar")
private external val importedToolbar: dynamic

interface ToolbarProps : RProps {
  var disableGutters: Boolean
  var variant: String // "regular" "dense"
  var className: String
  var classes: dynamic
}

val toolbar: RClass<ToolbarProps> = importedToolbar.default


@JsModule("@material-ui/core/IconButton")
private external val importedIconButton: dynamic

interface IconButtonProps : RProps {
  var disabled: Boolean
  var label: String
  var size: String
  var color: String // 'default' | 'inherit' | 'primary' | 'secondary'
  var className: String
  var onClick: (Event) -> Unit
  var classes: dynamic
  var disableRipple: Boolean
  var component: dynamic
  var buttonRef: (HTMLButtonElement) -> Unit
}

val iconButton: RClass<IconButtonProps> = importedIconButton.default


@JsModule("@material-ui/icons/Menu")
private external val importedMenuIcon: dynamic
val menuIcon: RClass<IconProps> = importedMenuIcon.default

@JsModule("@material-ui/icons/ExpandMore")
private external val importedExpandMoreIcon: dynamic
val expandMoreIcon: RClass<IconProps> = importedExpandMoreIcon.default

@JsModule("@material-ui/icons/List")
private external val importedListIcon: dynamic
val listIcon: RClass<IconProps> = importedListIcon.default

interface IconProps : RProps {
  var color: String
  var className: String
  var fontSize: String
  var style: dynamic
  var classes: dynamic
}

@JsModule("@material-ui/icons/Domain")
private external val importedDomainIcon: dynamic
val domainIcon: RClass<IconProps> = importedDomainIcon.default

@JsModule("@material-ui/icons/Notes")
private external val importedNotesIcon: dynamic
val notesIcon: RClass<IconProps> = importedNotesIcon.default

@JsModule("@material-ui/icons/Delete")
private external val importedDeleteIcon: dynamic
val deleteIcon: RClass<IconProps> = importedDeleteIcon.default

@JsModule("@material-ui/icons/Save")
private external val importedSaveIcon: dynamic
val saveIcon: RClass<IconProps> = importedSaveIcon.default

@JsModule("@material-ui/icons/FilterList")
private external val importedFilterListIcon: dynamic
val filterListIcon: RClass<IconProps> = importedFilterListIcon.default

@JsModule("@material-ui/icons/Visibility")
private external val importedVisibilityIcon: dynamic
val visibilityIcon: RClass<IconProps> = importedVisibilityIcon.default

@JsModule("@material-ui/icons/Public")
private external val importedPublicIcon: dynamic
val publicIcon: RClass<IconProps> = importedPublicIcon.default

@JsModule("@material-ui/icons/DeleteForeverRounded")
private external val importedDeleteForeverRoundedIcon: dynamic
val deleteForeverRoundedIcon: RClass<IconProps> = importedDeleteForeverRoundedIcon.default

@JsModule("@material-ui/icons/VisibilityOff")
private external val importedVisibilityOffIcon: dynamic
val visibilityOffIcon: RClass<IconProps> = importedVisibilityOffIcon.default

@JsModule("@material-ui/icons/RemoveRedEye")
private external val importedRemoveRedEyeIcon: dynamic
val removeRedEyeIcon: RClass<IconProps> = importedRemoveRedEyeIcon.default

@JsModule("@material-ui/icons/ShoppingCart")
private external val importedShoppingCart: dynamic
val shoppingCartIcon: RClass<IconProps> = importedShoppingCart.default

@JsModule("@material-ui/icons/CreditCard")
private external val importedCreditCard: dynamic
val creditCardIcon: RClass<IconProps> = importedCreditCard.default

@JsModule("@material-ui/icons/TimerOffOutlined")
private external val importedTimerOffOutlined: dynamic
val timerOffOutlinedIcon: RClass<IconProps> = importedTimerOffOutlined.default

@JsModule("@material-ui/icons/Receipt")
private external val importedReceipt: dynamic
val receiptIcon: RClass<IconProps> = importedReceipt.default

@JsModule("@material-ui/icons/AspectRatioOutlined")
private external val importedAspectRatioIcon: dynamic
val aspectRatioIcon: RClass<IconProps> = importedAspectRatioIcon.default

@JsModule("@material-ui/icons/LocalGasStation")
private external val importedLocalGasStationIcon: dynamic
val localGasStationIcon: RClass<IconProps> = importedLocalGasStationIcon.default

@JsModule("@material-ui/icons/Edit")
private external val importedEditIcon: dynamic
val editIcon: RClass<IconProps> = importedEditIcon.default

@JsModule("@material-ui/icons/LocationOn")
private external val importedLocationOn: dynamic
val locationOnIcon: RClass<IconProps> = importedLocationOn.default

@JsModule("@material-ui/icons/HourglassEmpty")
private external val importedHourGlassEmptyIcon: dynamic
val hourGlassEmptyIcon: RClass<IconProps> = importedHourGlassEmptyIcon.default

@JsModule("@material-ui/icons/Refresh")
private external val importedRefreshIcon: dynamic
val refreshIcon: RClass<IconProps> = importedRefreshIcon.default

@JsModule("@material-ui/icons/Timelapse")
private external val importedTimelapseIcon: dynamic
val timelapseIcon: RClass<IconProps> = importedTimelapseIcon.default

@JsModule("@material-ui/icons/Payment")
private external val importedPaymentIcon: dynamic
val paymentIcon: RClass<IconProps> = importedPaymentIcon.default

@JsModule("@material-ui/icons/Assessment")
private external val importedAssessmentIcon: dynamic
val assessmentIcon: RClass<IconProps> = importedAssessmentIcon.default

@JsModule("@material-ui/icons/Poll")
private external val importedPollIcon: dynamic
val pollIcon: RClass<IconProps> = importedPollIcon.default

@JsModule("@material-ui/icons/Toc")
private external val importedTocIcon: dynamic
val tocIcon: RClass<IconProps> = importedTocIcon.default

@JsModule("@material-ui/icons/CheckCircleOutline")
private external val importedCheckCircleOutlineIcon: dynamic
val checkCircleOutlineIcon: RClass<IconProps> = importedCheckCircleOutlineIcon.default

@JsModule("@material-ui/icons/CheckCircle")
private external val importedCheckCircleIcon: dynamic
val checkCircleIcon: RClass<IconProps> = importedCheckCircleIcon.default

@JsModule("@material-ui/icons/RemoveCircleOutline")
private external val importedRemoveCircleOutline: dynamic
val removeCircleOutlineIcon: RClass<IconProps> = importedRemoveCircleOutline.default

@JsModule("@material-ui/icons/Check")
private external val importedCheckIcon: dynamic
val checkIcon: RClass<IconProps> = importedCheckIcon.default

@JsModule("@material-ui/icons/Warning")
private external val importedWarningIcon: dynamic
val warningIcon: RClass<IconProps> = importedWarningIcon.default

@JsModule("@material-ui/icons/Info")
private external val importedInfoIcon: dynamic
val infoIcon: RClass<IconProps> = importedInfoIcon.default

@JsModule("@material-ui/icons/InfoOutlined")
private external val importedInfoOutlinedIcon: dynamic
val infoOutlinedIcon: RClass<IconProps> = importedInfoOutlinedIcon.default

@JsModule("@material-ui/icons/ImageRounded")
private external val importedImageRoundedIcon: dynamic
val imageRoundedIcon: RClass<IconProps> = importedImageRoundedIcon.default

@JsModule("@material-ui/icons/Fullscreen")
private external val importedFullscreenIcon: dynamic
val fullscreenIcon: RClass<IconProps> = importedFullscreenIcon.default

@JsModule("@material-ui/icons/Cancel")
private external val importedCancel: dynamic
val cancelIcon: RClass<IconProps> = importedCancel.default

@JsModule("@material-ui/icons/CancelOutlined")
private external val importedCancelOutlined: dynamic
val cancelOutlinedIcon: RClass<IconProps> = importedCancelOutlined.default


@JsModule("@material-ui/icons/FlashOn")
private external val importedFlashOn: dynamic
val flashOnIcon: RClass<IconProps> = importedFlashOn.default


@JsModule("@material-ui/icons/Search")
private external val importedSearchIcon: dynamic
val searchIcon: RClass<IconProps> = importedSearchIcon.default

@JsModule("@material-ui/icons/MoreVert")
private external val importedMoreVertIcon: dynamic
val moreVertIcon: RClass<IconProps> = importedMoreVertIcon.default

@JsModule("@material-ui/icons/MoreHoriz")
private external val importedMoreHorizIcon: dynamic
val moreHorizIcon: RClass<IconProps> = importedMoreHorizIcon.default

@JsModule("@material-ui/icons/BookmarkBorder")
private external val importedBookmarkBorderIcon: dynamic
val bookmarkBorderIcon: RClass<IconProps> = importedBookmarkBorderIcon.default

@JsModule("@material-ui/icons/Chat")
private external val importedChatIcon: dynamic
val chatIcon: RClass<IconProps> = importedChatIcon.default

@JsModule("@material-ui/icons/Clear")
private external val importedClearIcon: dynamic
val clearIcon: RClass<IconProps> = importedClearIcon.default

@JsModule("@material-ui/icons/ArrowBack")
private external val importedArrowBackIcon: dynamic
val arrowBackIcon: RClass<IconProps> = importedArrowBackIcon.default

@JsModule("@material-ui/icons/Settings")
private external val importedSettingsIcon: dynamic
val settingsIcon: RClass<IconProps> = importedSettingsIcon.default

@JsModule("@material-ui/icons/Launch")
private external val importedLaunchIcon: dynamic
val launchIcon: RClass<IconProps> = importedLaunchIcon.default

@JsModule("@material-ui/icons/Cake")
private external val importedCakeIcon: dynamic
val cakeIcon: RClass<IconProps> = importedCakeIcon.default

@JsModule("@material-ui/icons/Send")
private external val importedSendIcon: dynamic
val sendIcon: RClass<IconProps> = importedSendIcon.default

@JsModule("@material-ui/icons/AddCircleOutline")
private external val importedAddCircleOutlineIcon: dynamic
val addCircleOutlineIcon: RClass<IconProps> = importedAddCircleOutlineIcon.default


@JsModule("@material-ui/icons/Add")
private external val importedAddIcon: dynamic
val addIcon: RClass<IconProps> = importedAddIcon.default

@JsModule("@material-ui/icons/AccountCircle")
private external val importedAccountCircleIcon: dynamic
val accountCircleIcon: RClass<IconProps> = importedAccountCircleIcon.default

@JsModule("@material-ui/icons/CalendarToday")
private external val importedCalendarTodayIcon: dynamic
val calendarIcon: RClass<IconProps> = importedCalendarTodayIcon.default

@JsModule("@material-ui/icons/PowerSettingsNew")
private external val importedPowerSettingsNewIcon: dynamic
val logoutIcon: RClass<IconProps> = importedPowerSettingsNewIcon.default

@JsModule("@material-ui/icons/Share")
private external val importedShareIcon: dynamic
val shareIcon: RClass<IconProps> = importedShareIcon.default

@JsModule("@material-ui/icons/Error")
private external val importedErrorIcon: dynamic
val errorIcon: RClass<IconProps> = importedErrorIcon.default

@JsModule("@material-ui/icons/BugReport")
private external val importedBugReportIcon: dynamic
val bugReportIcon: RClass<IconProps> = importedBugReportIcon.default

@JsModule("@material-ui/icons/People")
private external val importedPeopleIcon: dynamic
val peopleIcon: RClass<IconProps> = importedPeopleIcon.default

@JsModule("@material-ui/icons/Business")
private external val importedBusinessIcon: dynamic
val businessIcon: RClass<IconProps> = importedBusinessIcon.default

@JsModule("@material-ui/icons/PeopleOutline")
private external val importedPeopleOutlineIcon: dynamic
val peopleOutlineIcon: RClass<IconProps> = importedPeopleOutlineIcon.default

@JsModule("@material-ui/icons/Redeem")
private external val importedRedeemIcon: dynamic
val redeemIcon: RClass<IconProps> = importedRedeemIcon.default

@JsModule("@material-ui/icons/Restaurant")
private external val importedRestaurantIcon: dynamic
val restaurantIcon: RClass<IconProps> = importedRestaurantIcon.default

@JsModule("@material-ui/icons/ArtTrack")
private external val importedArtTrackIcon: dynamic
val artTrackIcon: RClass<IconProps> = importedArtTrackIcon.default

@JsModule("@material-ui/icons/PagesRounded")
private external val importedPagesRoundedIcon: dynamic
val pagesRoundedIcon: RClass<IconProps> = importedPagesRoundedIcon.default

@JsModule("@material-ui/icons/MeetingRoom")
private external val importedMeetingRoomIcon: dynamic
val meetingRoomIcon: RClass<IconProps> = importedMeetingRoomIcon.default

@JsModule("@material-ui/icons/Equalizer")
private external val importedEqualizerIcon: dynamic
val equalizerIcon: RClass<IconProps> = importedEqualizerIcon.default

@JsModule("@material-ui/icons/CloudDownload")
private external val importedCloudDownloadIcon: dynamic
val cloudDownloadIcon: RClass<IconProps> = importedCloudDownloadIcon.default

@JsModule("@material-ui/icons/WorkOutline")
private external val importedWorkOutlineIcon: dynamic
val workOutlineIcon: RClass<IconProps> = importedWorkOutlineIcon.default

@JsModule("@material-ui/icons/ImageOutlined")
private external val importedImageOutlinedIcon: dynamic
val imageOutlinedIcon: RClass<IconProps> = importedImageOutlinedIcon.default

@JsModule("@material-ui/icons/Link")
private external val importedLinkIcon: dynamic
val linkIcon: RClass<IconProps> = importedLinkIcon.default

@JsModule("@material-ui/icons/PlaylistAddCheck")
private external val importedPlaylistAddCheckIcon: dynamic
val playlistAddCheckIcon: RClass<IconProps> = importedPlaylistAddCheckIcon.default

@JsModule("@material-ui/icons/LinkOff")
private external val importedLinkOffIcon: dynamic
val linkOffIcon: RClass<IconProps> = importedLinkOffIcon.default

@JsModule("@material-ui/icons/InsertEmoticonOutlined")
private external val importedInsertEmoticonOutlinedIcon: dynamic
val insertEmoticonOutlinedIcon: RClass<IconProps> = importedInsertEmoticonOutlinedIcon.default

@JsModule("@material-ui/icons/TrendingUp")
private external val importedTrendingUpIcon: dynamic
val trendingUpIcon: RClass<IconProps> = importedTrendingUpIcon.default

@JsModule("@material-ui/icons/Close")
private external val importedCloseIcon: dynamic
val closeIcon: RClass<IconProps> = importedCloseIcon.default

@JsModule("@material-ui/icons/RssFeed")
private external val importedRssFeedIcon: dynamic
val rssFeedIcon: RClass<IconProps> = importedRssFeedIcon.default

@JsModule("@material-ui/icons/NotificationsOutlined")
private external val importedNotificationsOutlinedIcon: dynamic
val notificationsOutlinedIcon: RClass<IconProps> = importedNotificationsOutlinedIcon.default

@JsModule("@material-ui/icons/Notifications")
private external val importedNotificationsIcon: dynamic
val notificationsIcon: RClass<IconProps> = importedNotificationsIcon.default

@JsModule("@material-ui/icons/Undo")
private external val importedUndoIcon: dynamic
val undoIcon: RClass<IconProps> = importedUndoIcon.default

@JsModule("@material-ui/icons/NavigateNext")
private external val importedNavigateNextIcon: dynamic
val navigateNextIcon: RClass<IconProps> = importedNavigateNextIcon.default

@JsModule("@material-ui/icons/VerifiedUser")
private external val importedVerifiedUserIcon: dynamic
val verifiedUserIcon: RClass<IconProps> = importedVerifiedUserIcon.default

@JsModule("@material-ui/icons/Build")
private external val importedBuildIcon: dynamic
val buildIcon: RClass<IconProps> = importedBuildIcon.default

@JsModule("@material-ui/icons/FileCopyOutlined")
private external val importedFileCopyOutlinedIcon: dynamic
val fileCopyOutlinedIcon: RClass<IconProps> = importedFileCopyOutlinedIcon.default

@JsModule("@material-ui/icons/History")
private external val importedHistoryIcon: dynamic
val historyIcon: RClass<IconProps> = importedHistoryIcon.default

@JsModule("@material-ui/icons/Schedule")
private external val importedScheduleIcon: dynamic
val scheduleIcon: RClass<IconProps> = importedScheduleIcon.default

@JsModule("@material-ui/icons/Publish")
private external val importedPublishIcon: dynamic
val publishIcon: RClass<IconProps> = importedPublishIcon.default

@JsModule("@material-ui/icons/HighlightOff")
private external val importedHighlightOffIcon: dynamic
val highlightOffIcon: RClass<IconProps> = importedHighlightOffIcon.default

@JsModule("@material-ui/icons/School")
private external val importedSchoolIcon: dynamic
val schoolIcon: RClass<IconProps> = importedSchoolIcon.default

@JsModule("@material-ui/icons/Assignment")
private external val importedAssignmentIcon: dynamic
val assignmentIcon: RClass<IconProps> = importedAssignmentIcon.default

@JsModule("@material-ui/icons/ContactMail")
private external val importedContactMailIcon: dynamic
val contactMailIcon: RClass<IconProps> = importedContactMailIcon.default

@JsModule("@material-ui/icons/CloudUpload")
private external val importedCloudUploadIcon: dynamic
val cloudUploadIcon: RClass<IconProps> = importedCloudUploadIcon.default

@JsModule("@material-ui/icons/NotInterested")
private external val importedNotInterestedIcon: dynamic
val notInterestedIcon: RClass<IconProps> = importedNotInterestedIcon.default

@JsModule("@material-ui/icons/ArrowForward")
private external val importedArrowForwardIcon: dynamic
val arrowForwardIcon: RClass<IconProps> = importedArrowForwardIcon.default

@JsModule("@material-ui/icons/ArrowUpward")
private external val importedArrowUpwardIcon: dynamic
val arrowUpwardIcon: RClass<IconProps> = importedArrowUpwardIcon.default

@JsModule("@material-ui/icons/ArrowDownward")
private external val importedArrowDownwardIcon: dynamic
val arrowDownwardIcon: RClass<IconProps> = importedArrowDownwardIcon.default

@JsModule("@material-ui/icons/Web")
private external val importedWebIcon: dynamic
val webIcon: RClass<IconProps> = importedWebIcon.default

@JsModule("@material-ui/icons/AccessTime")
private external val importedAccessTimeIcon: dynamic
val accessTimeIcon: RClass<IconProps> = importedAccessTimeIcon.default

@JsModule("@material-ui/icons/Sync")
private external val importedSyncIcon: dynamic
val syncIcon: RClass<IconProps> = importedSyncIcon.default

@JsModule("@material-ui/icons/Sort")
private external val importedSortIcon: dynamic
val sortIcon: RClass<IconProps> = importedSortIcon.default

@JsModule("@material-ui/icons/PhoneAndroid")
private external val importedPhoneAndroidIcon: dynamic
val phoneAndroidIcon: RClass<IconProps> = importedPhoneAndroidIcon.default

@JsModule("@material-ui/icons/Crop")
private external val importedCropIcon: dynamic
val cropIcon: RClass<IconProps> = importedCropIcon.default

@JsModule("@material-ui/icons/RotateRight")
private external val importedRotateRightIcon: dynamic
val rotateRightIcon: RClass<IconProps> = importedRotateRightIcon.default

@JsModule("@material-ui/core/Typography")
private external val importedTypography: dynamic

interface TypographyProps : RProps {
  var align: String // 'inherit', 'left', 'center', 'right', 'justify'
  var color: String // 'default', 'error', 'inherit', 'primary', 'secondary', 'textPrimary', 'textSecondary'
  var variant: String // Can be one of the following:

  // 'display4', 'display3', 'display2', 'display1', 'headline', 'title', 'subheading',
  // 'body2', 'body1', 'caption', 'button', 'srOnly', 'inherit'
  var gutterBottom: Boolean
  var paragraph: Boolean
  var noWrap: Boolean
  var component: dynamic
  var headlineMapping: dynamic
  var classes: dynamic
  var className: String
  var style: dynamic
  var display: String
}

val typography: RClass<TypographyProps> = importedTypography.default

@JsModule("@material-ui/core/Menu")
private external val importedMenu: dynamic

interface MenuProps : RProps {
  var id: String
  var classes: dynamic
  var anchorEl: dynamic
  var disableAutoFocusItem: Boolean
  var menuListProps: dynamic
  var onClose: (event: Event) -> Unit
  var onEnter: () -> Unit
  var onEntered: () -> Unit
  var onEntering: () -> Unit
  var onExit: () -> Unit
  var onExited: () -> Unit
  var onExiting: () -> Unit
  var open: Boolean
  var anchorOrigin: dynamic
  var transformOrigin: dynamic

  // Set this to null if you wan't to change anchor origin. Ref: https://stackoverflow.com/a/52551100
  var getContentAnchorEl: dynamic
}

val menu: RClass<MenuProps> = importedMenu.default


@JsModule("@material-ui/core/MenuItem")
private external val importedMenuItem: dynamic


interface MenuItemProps : RProps {
  var value: String
  var onClick: (event: Event) -> Unit
  var disabled: Boolean
  var selected: Boolean
  var classes: dynamic
}

val menuItem: RClass<MenuItemProps> = importedMenuItem.default

@JsModule("@material-ui/core/Box")
private external val importedBox: dynamic

interface FlexboxProps : RProps {
  var flexDirection: String
  var justifyContent: String
  var flexWrap: String
  var alignContent: String
  var alignItems: String
  var order: String
  var flex: String
  var alignSelf: String
  var flexGrow: String
  var flexShrink: String
}

interface DisplayProps : RProps {
  var displayPrint: String
  var display: String
  var overflow: String
  var textOverflow: String
  var visibility: String
  var flex: String
  var whiteSpace: String
}

interface BoxProps : FlexboxProps, DisplayProps {
  var className: String
  var onClick: (event: Event) -> Unit
}

val box: RClass<BoxProps> = importedBox.default


@JsModule("@material-ui/core/Drawer")
private external val importedDrawer: dynamic

interface DrawerProps : RProps {
  var className: String
  var anchor: String // 'left' | 'top' | 'right' | 'bottom'
  var elevation: Int
  var ModalProps: dynamic
  var onClose: (Event) -> Unit
  var open: Boolean
  var PaperProps: dynamic
  var SlideProps: dynamic
  var transitionDuration: dynamic // int or object { enter: xx, exit: xx}
  var variant: String // 'permanent' | 'persistent' | 'temporary'
  var classes: dynamic
}

val drawer: RClass<DrawerProps> = importedDrawer.default


@JsModule("@material-ui/core/List")
private external val importedList: dynamic

interface ListProps : RProps {
  var component: dynamic
  var dense: Boolean
  var disablePadding: Boolean
  var subheader: dynamic

}

val list: RClass<ListProps> = importedList.default

@JsModule("@material-ui/core/Modal")
private external val importedModal: dynamic

interface ModalProps : RProps {
  var open: Boolean
  var closeAfterTransition: Boolean
  var disableAutoFocus: Boolean
  var disableBackdropClick: Boolean
  var disableEnforceFocus: Boolean
  var disableEscapeKeyDown: Boolean
  var keepMounted: Boolean
  var disablePortal: Boolean
  var disableRestoreFocus: Boolean
  var disableScrollLock: Boolean
  var hideBackdrop: Boolean
}

val modal: RClass<ModalProps> = importedList.default

@JsModule("@material-ui/core/ListSubheader")
private external val importedListSubheader: dynamic

interface ListSubheaderProps : RProps

val listSubheader: RClass<ListProps> = importedListSubheader.default

@JsModule("@material-ui/core/ListItem")
private external val importedListItem: dynamic

interface ListItemProps : RProps {
  var button: Boolean
  var component: dynamic
  var ContainerComponent: dynamic
  var ContainerProps: dynamic
  var dense: Boolean
  var disabled: Boolean
  var disableGutters: Boolean
  var divider: Boolean
  var selected: Boolean
  var onClick: (event: Event) -> Unit
}

val listItem: RClass<ListItemProps> = importedListItem.default


@JsModule("@material-ui/core/ListItemIcon")
private external val importedListItemIcon: dynamic

interface ListItemIconProps : RProps {
  var classes: dynamic
}

val listItemIcon: RClass<ListItemIconProps> = importedListItemIcon.default

@JsModule("@material-ui/core/ListItemText")
private external val importedListItemText: dynamic

interface ListItemTextProps : RProps {
  var classes: dynamic
  var className: String
  var disableTypography: Boolean
  var inset: Boolean
  var primary: dynamic
  var primaryTypographyProps: dynamic
  var secondary: dynamic
  var secondaryTypographyProps: dynamic
}

val listItemText: RClass<ListItemTextProps> = importedListItemText.default


@JsModule("@material-ui/core/Divider")
private external val importedDivider: dynamic

interface DividerProps : RProps {
  var absolute: Boolean
  var component: dynamic
  var inset: Boolean
  var light: Boolean
  var variant: dynamic
  var className: String

  // must update material ui to latest version in order to use them (kept for future use)
  var flexItem: Boolean
  var orientation: String

}

val divider: RClass<DividerProps> = importedDivider.default

@JsModule("@material-ui/core/Backdrop")
private external val importedBackdrop: dynamic

interface BackdropProps : RProps {
  var invisible: Boolean
  var classes: dynamic
  var open: Boolean
  var className: String
  var transitionDuration: Int
}

val backdrop: RClass<BackdropProps> = importedBackdrop.default

@JsModule("@material-ui/core/styles/createMuiTheme")
private external val importedCreateMuiTheme: dynamic
val createMuiTheme: (dynamic) -> dynamic = importedCreateMuiTheme.default

@JsModule("@material-ui/core/styles/createPalette")
private external val importedCreatePalette: dynamic
val createPalette: (dynamic) -> dynamic = importedCreatePalette.default

@JsModule("@material-ui/styles/useTheme")
external val importedUseTheme: dynamic
fun useTheme() = importedUseTheme.default()


@JsModule("@material-ui/core/styles/withStyles")
external val importedWithStyles: dynamic

private fun <T : RClass<*>> importedWithStyles(styles: dynamic, options: dynamic = undefined): (T) -> T =
    importedWithStyles.default(styles)


fun <P : RProps, T : RClass<P>> withStyles(
    styles: dynamic,
    component: T,
    options: dynamic = undefined
): T {
  return ((importedWithStyles<T>(styles, options))(component))
}

fun <P : RProps, T : RClass<P>> RBuilder.withStyles(
    styles: dynamic,
    component: T,
    options: dynamic = undefined,
    handler: RElementBuilder<P>.() -> Unit
): T {
  return ((importedWithStyles<T>(styles, options))(component)).also { it.invoke(handler) }
}

fun <P : RProps, C : Component<P, *>> withStyles_compilerBug(
    styles: dynamic,
    options: dynamic = undefined,
    kClass: KClass<C>
): RClass<P> {
  val higherOrderComponent: RClass<P> = importedWithStyles.default(styles, options)(kClass.js)
  return higherOrderComponent
}

inline fun <P : RProps, reified C : Component<P, *>> withStyles(
    styles: dynamic,
    options: dynamic = undefined
): RClass<P> {
  return withStyles_compilerBug(styles, options, C::class)
}

@JsModule("@material-ui/core/styles")
private external val importedMuiThemeProvider: dynamic

@JsModule("@material-ui/styles/ThemeProvider")
private external val importedThemeProvider: dynamic
val themeProvider: RClass<MuiThemeProviderProps> = importedThemeProvider.default


interface ButtonBaseProps : RProps {
  var onClick: (dynamic) -> Unit
  var component: dynamic
  var disabled: Boolean
  var disableRipple: Boolean
  var disableTouchRipple: Boolean
  var focusRipple: Boolean
  var focusVisibleClassName: String
  var onFocusVisible: (dynamic) -> Unit
  var TouchRippleProps: dynamic
  var type: String
}

@JsModule("@material-ui/core/Button")
private external val importedButton: dynamic

interface ButtonProps : ButtonBaseProps {
  var color: String
  var fullWidth: Boolean
  var href: String
  var target: String
  var mini: Boolean
  var size: String
  var variant: String
  var className: String
  var classes: dynamic
  var style: dynamic
}

val muiButton: RClass<ButtonProps> = importedButton.default

@JsModule("@material-ui/core/Collapse")
private external val importedCollapse: dynamic

interface CollapseProps : RProps {
  var classes: dynamic
  var collapsedHeight: String
  var timeout: dynamic // can be a number or "auto"

  @JsName("in")
  var inProp: Boolean
}

val collapse: RClass<CollapseProps> = importedCollapse.default

@JsModule("@material-ui/core/Link")
private external val importedLink: dynamic

interface LinkProps : TypographyProps {
  var href: String
  var target: String
  var underline: String  //'none'| 'hover'| 'always'
  var rel: String
  var type: String // forwarded to button (if you choose component="button")
}

val muiLink: RClass<LinkProps> = importedLink.default


interface MuiThemeProviderProps : RProps {
  var theme: dynamic
}

val muiThemeProvider: RClass<MuiThemeProviderProps> = importedMuiThemeProvider.MuiThemeProvider

@JsModule("@material-ui/core/Hidden")
private external val importedHidden: dynamic

interface HiddenProps : RProps {
  var implementation: String
  var only: String
  var initialWidth: String
  var lgDown: Boolean
  var lgUp: Boolean
  var mdDown: Boolean
  var mdUp: Boolean
  var smDown: Boolean
  var smUp: Boolean
  var xlDown: Boolean
  var xlUp: Boolean
  var xsDown: Boolean
  var xsUp: Boolean
}

val hidden: RClass<HiddenProps> = importedHidden.default

@JsModule("@material-ui/core/MobileStepper")
private external val importedMobileStepper: dynamic

interface MobileStepperProps : RProps {
  var activeStep: Int
  var backButton: ReactElement
  var LinearProgressProps: dynamic
  var classes: dynamic
  var nextButton: ReactElement
  var position: String
  var steps: Int
  var variant: String
}

val mobileStepper: RClass<MobileStepperProps> = importedMobileStepper.default

@JsModule("@material-ui/core/Stepper")
private external val importedStepper: dynamic

interface StepperProps : RProps {
  var activeStep: Int
  var alternativeLabel: Boolean
  var connector: dynamic
  var nonLinear: Boolean
  var orientation: String
}

val stepper: RClass<StepperProps> = importedStepper.default


@JsModule("@material-ui/core/RadioGroup")
private external val importedRadioGroup: dynamic

interface RadioGroupProps : RProps {
  var className: String
  var defaultValue: dynamic
  var value: dynamic
  var onChange: (Event) -> Unit
  var name: String
}

val radioGroup: RClass<RadioGroupProps> = importedRadioGroup.default

@JsModule("@material-ui/core/Radio")
private external val importedRadio: dynamic

interface RadioProps : RProps {
  var className: String
  var checked: Boolean
  var required: Boolean
  var checkedIcon: dynamic
  var onChange: (Event) -> Unit
  var classes: dynamic
  var value: dynamic
  var color: String
  var name: String
  var disabled: Boolean
  var icon: RClass<IconProps>
  var disableRipple: Boolean
}

val radio: RClass<RadioProps> = importedRadio.default

@JsModule("@material-ui/core/Step")
private external val importedStep: dynamic

interface StepProps : RProps {
  var active: Boolean
  var completed: Boolean
  var disabled: Boolean
  var stepIcon: dynamic
}

val step: RClass<StepProps> = importedStep.default

@JsModule("@material-ui/core/StepLabel")
private external val importedStepLabel: dynamic

interface StepLabelProps : RProps {
  var disabled: Boolean
  var error: Boolean
  var icon: dynamic
  var optional: dynamic
  var StepIconComponent: dynamic
  var StepIconProps: dynamic
}

val stepLabel: RClass<StepLabelProps> = importedStepLabel.default

@JsModule("@material-ui/core/StepContent")
private external val importedStepContent: dynamic

interface StepContentProps : RProps {
  var classes: dynamic
}

val stepContent: RClass<StepContentProps> = importedStepContent.default

@JsModule("@material-ui/core/StepIcon")
private external val importedStepIcon: dynamic

interface StepIconProps : RProps {
  var classes: dynamic
  var icon: dynamic
}

val stepIcon: RClass<StepIconProps> = importedStepIcon.default

@JsModule("@material-ui/core/StepConnector")
private external val importedStepConnector: dynamic

interface StepConnectorProps : RProps {
  var classes: dynamic
}

val stepConnector: RClass<StepConnectorProps> = importedStepConnector.default


@JsModule("@material-ui/core/StepButton")
private external val importedStepButton: dynamic

interface StepButtonProps : RProps {
  var icon: dynamic
  var completed: Boolean
  var optional: dynamic
  var onClick: (Event) -> Unit
}

val stepButton: RClass<StepButtonProps> = importedStepButton.default

@JsModule("@material-ui/core/Tabs")
private external val importedTabs: dynamic

interface TabsProps : RProps {
  var action: (actions: dynamic) -> Unit
  var centered: Boolean
  var classes: dynamic
  var component: dynamic
  var fullWidth: Boolean
  var indicatorColor: String
  var onChange: (event: dynamic, value: Int) -> Unit
  var scrollable: Boolean
  var ScrollButtonComponent: dynamic
  var scrollButtons: String
  var TabIndicatorProps: dynamic
  var textColor: String
  var value: dynamic
}

val tabs: RClass<TabsProps> = importedTabs.default


@JsModule("@material-ui/core/Tab")
private external val importedTab: dynamic

interface TabProps : RProps {
  var classes: dynamic
  var disabled: Boolean
  var icon: dynamic
  var label: dynamic
  var value: dynamic
}

val tab: RClass<TabProps> = importedTab.default


@JsModule("@material-ui/core/Switch")
private external val importedSwitch: dynamic

interface SwitchProps : RProps {
  var checked: Boolean
  var checkedIcon: dynamic
  var classes: dynamic
  var color: String
  var disabled: Boolean
  var disableRipple: Boolean
  var icon: dynamic
  var id: String
  var inputProps: dynamic
  var inputRef: dynamic
  var onChange: (event: dynamic, checked: Boolean) -> Unit
  var type: String
  var value: Boolean
}

val switch: RClass<SwitchProps> = importedSwitch.default

@JsModule("@material-ui/core/FormGroup")
private external val importedFormGroup: dynamic

interface FormGroupProps : RProps {
  var className: String
  var row: Boolean
  var children: dynamic
  var classes: dynamic
}

val formGroup: RClass<FormGroupProps> = importedFormGroup.default

@JsModule("@material-ui/core/FormControlLabel")
private external val importedFormControlLabel: dynamic

interface FormControlLabelProps : RProps {
  var checked: Boolean
  var classes: dynamic
  var control: dynamic
  var disabled: Boolean
  var inputRef: dynamic
  var label: dynamic
  var labelPlacement: String
  var name: String
  var onChange: (dynamic) -> Unit
  var value: String
}

val formControlLabel: RClass<FormControlLabelProps> = importedFormControlLabel.default

@JsModule("@material-ui/core/InputBase")
private external val importedInputBase: dynamic

interface InputBaseProps : RProps {
  var autoComplete: String
  var autoFocus: Boolean
  var classes: dynamic
  var className: String
  var defaultValue: dynamic
  var disabled: Boolean
  var error: Boolean
  var fullWidth: Boolean
  var endAdornment: ReactElement
  var id: String
  var inputProps: dynamic
  var color: String // "primary" or "secondary"
  var inputComponent: String
  var inputRef: (inputRef: HTMLInputElement) -> Unit
  var margin: String // "dense" or "none"
  var multiline: Boolean
  var name: String
  var onChange: (dynamic) -> Unit
  var onBlur: (dynamic) -> Unit
  var onKeyDown: (dynamic) -> Unit
  var readOnly: Boolean
  var required: Boolean
  var rows: dynamic
  var rowsMax: dynamic
  var rowsMin: dynamic
  var startAdornment: ReactElement
  var placeholder: String
  var type: String
  var value: dynamic
}

val inputBase: RClass<InputBaseProps> = importedInputBase.default


@JsModule("@material-ui/core/TextField")
private external val importedTextField: dynamic

interface TextFieldProps : InputBaseProps {
  var FormHelperTextProps: dynamic
  var helperText: dynamic
  var InputLabelProps: dynamic
  var label: dynamic
  var select: Boolean
  var SelectProps: dynamic
  var variant: String
  var style: dynamic
  var InputProps: dynamic
}

val textField: RClass<TextFieldProps> = importedTextField.default


@JsModule("@material-ui/lab/Autocomplete")
private external val importedAutocomplete: dynamic

interface AutocompleteProps<T> : RProps {
  var autoHighlight: Boolean
  var autoSelect: Boolean
  var blurOnSelect: dynamic // 'mouse' | 'touch' | bool
  var ChipProps: dynamic
  var classes: dynamic
  var className: String
  var clearOnBlur: Boolean
  var clearOnEscape: Boolean
  var clearText: String // def: 'clear'
  var closeIcon: dynamic
  var closeText: String // def: 'Close'
  var debug: Boolean
  var defaultValue: Array<T>
  var disableClearable: Boolean
  var disableCloseOnSelect: Boolean
  var disabled: Boolean
  var disabledItemsFocusable: Boolean
  var disableListWrap: Boolean
  var disablePortal: Boolean
  var filterOptions: (Array<T>, dynamic) -> Unit
  var filterSelectedOptions: Boolean
  var forcePopupIcon: dynamic // 'auto' | bool
  var freeSolo: Boolean
  var fullWidth: Boolean
  var getLimitTagsText: (Int) -> dynamic // (number of truncated items) -> ReactNode
  var getOptionDisabled: (T) -> Boolean
  var getOptionLabel: (T) -> String
  var getOptionSelected: (option: T, value: T) -> Boolean
  var groupBy: (T) -> String
  var handleHomeEndKeys: Boolean
  var includeInputInList: Boolean
  var limitTags: Int // -1 to disable
  var ListboxComponent: String // def: 'ul'
  var ListboxProps: dynamic
  var loading: Boolean
  var loadingText: String // def: 'Loading...'
  var multiple: Boolean
  var noOptionsText: String // def: 'No options'
  var onChange: (event: dynamic, value: Array<T>, reason: String) -> Unit // reason: create-option", "select-option", "remove-option", "blur", "clear"
  var onClose: (event: dynamic, reason: String) -> Unit // reason: "toggleInput" | "escape" | "select-option" | "blur"
  var onHighlightChange: (event: dynamic, option: T, reason: String) -> Unit // reason: "keyboard", "auto", "mouse"
  var onInputChange: (event: dynamic, value: String, reason: String) -> Unit // reason: "input" | "reset" | "clear"
  var onOpen: (event: dynamic) -> Unit
  var open: Boolean
  var openOnFocus: Boolean
  var openText: String // def: 'Open'
  var options: Array<T>
  var PaperComponent: dynamic
  var PopperComponent: dynamic
  var popupIcon: dynamic
  var renderGroup: (option: dynamic) -> dynamic
  var renderInput: ((params: dynamic) -> dynamic)?
  var renderOption: (option: T, state: dynamic) -> dynamic
  var renderTags: (value: Array<T>, getTagProps: dynamic) -> dynamic
  var selectOnFocus: Boolean
  var size: String // 'medium' (def) | 'small'
  var value: T
}

val muiAutocomplete: RClass<AutocompleteProps<dynamic>> = importedAutocomplete.default


@JsModule("@material-ui/core/InputAdornment")
private external val importedInputAdornment: dynamic

interface InputAdornmentProps : RProps {
  var classes: dynamic
  var component: dynamic
  var disableTypography: Boolean
  var position: String
}

val inputAdornment: RClass<InputAdornmentProps> = importedInputAdornment.default

@JsModule("@material-ui/core/CircularProgress")
private external val importedCircularProgress: dynamic

interface CircularProgressProps : RProps {
  var classes: dynamic
  var className: dynamic
  var color: String
  var disableShrink: Boolean
  var size: dynamic
  var thickness: Double
  var value: Int
  var variant: String // 'determinate' | 'indeterminate' | 'static'
}

val circularProgress: RClass<CircularProgressProps> = importedCircularProgress.default


@JsModule("@material-ui/core/LinearProgress")
private external val importedLinearProgress: dynamic

interface LinearProgressProps : RProps {
  var className: dynamic
  var color: String
  var value: Number
  var valueBuffer: Number
  var variant: String
}

val linearProgress: RClass<LinearProgressProps> = importedLinearProgress.default


@JsModule("@material-ui/core/Select")
private external val importedSelect: dynamic

interface SelectProps : RProps {
  var autoWidth: Boolean
  var children: dynamic
  var classes: dynamic
  var displayEmpty: Boolean
  var iconComponent: dynamic
  var input: dynamic
  var inputProps: dynamic
  var MenuProps: dynamic
  var multiple: Boolean
  var native: Boolean
  var onChange: (event: dynamic) -> Unit
  var onClose: (event: dynamic) -> Unit
  var onOpen: (event: dynamic) -> Unit
  var open: Boolean
  var renderValue: (value: dynamic) -> dynamic
  var SelectDisplayProps: dynamic
  var defaultValue: String
  var value: String
  var variant: String
  var label: String
}

val muiSelect: RClass<SelectProps> = importedSelect.default

@JsModule("@material-ui/core/FormControl")
private external val importedFormControl: dynamic

interface FormControlProps : RProps {
  var children: dynamic
  var classes: dynamic
  var component: dynamic
  var disabled: Boolean
  var error: Boolean
  var fullWidth: Boolean
  var margin: String
  var required: Boolean
  var variant: String
  var className: String
}

val formControl: RClass<FormControlProps> = importedFormControl.default


@JsModule("@material-ui/core/InputLabel")
private external val importedInputLabel: dynamic

interface InputLabelProps : RProps {
  var htmlFor: String
  var children: dynamic
  var classes: dynamic
  var className: dynamic
  var disableAnimation: Boolean
  var disabled: Boolean
  var error: Boolean
  var focused: Boolean
  var FormLabelClasses: dynamic
  var margin: String
  var required: Boolean
  var shrink: Boolean
  var variant: String
}

val inputLabel: RClass<InputLabelProps> = importedInputLabel.default

@JsModule("@material-ui/core/OutlinedInput")
private external val importedOutlinedInput: dynamic

interface InputOutlinedInputProps : RProps {
  var labelWidth: Int
  var name: String
  var id: String
  var fullWidth: Boolean
}

val outlinedInput: RClass<InputOutlinedInputProps> = importedOutlinedInput.default

@JsModule("@material-ui/core/Paper")
private external val importedPaper: dynamic

interface PaperProps : RProps {
  var classes: dynamic
  var className: dynamic
  var component: dynamic
  var elevation: Int
  var square: Boolean
  var style: dynamic
}

val muiPaper: RClass<PaperProps> = importedPaper.default


@JsModule("@material-ui/core/Card")
private external val importedCard: dynamic

interface CardProps : PaperProps {
  var raised: Boolean
}

val muiCard: RClass<CardProps> = importedCard.default

@JsModule("@material-ui/core/CardHeader")
private external val importedCardHeader: dynamic

interface CardHeaderProps : RProps {
  var classes: dynamic
  var className: dynamic
  var title: ReactElement
  var subheader: ReactElement
  var disableTypography: dynamic
}

val muiCardHeader: RClass<CardHeaderProps> = importedCardHeader.default

@JsModule("@material-ui/core/CardContent")
private external val importedCardContent: dynamic

interface CardContentProps : RProps {
  var classes: dynamic
  var className: dynamic
}

val muiCardContent: RClass<CardContentProps> = importedCardContent.default


@JsModule("@material-ui/core/CardActions")
private external val importedCardActions: dynamic

interface CardActionsProps : RProps {
  var classes: dynamic
  var className: dynamic
  var children: dynamic
  var disableActionSpacing: Boolean
}

val muiCardActions: RClass<PaperProps> = importedCardActions.default

@JsModule("@material-ui/core/SnackbarContent")
private external val importedSnackbarContent: dynamic

interface SnackbarContentProps : RProps {
  var action: dynamic
  var classes: dynamic
  var className: String
  var message: ReactElement
}

val snackbarContent: RClass<SnackbarContentProps> = importedSnackbarContent.default

@JsModule("@material-ui/core/Snackbar")
private external val importedSnackbar: dynamic


interface SnackbarProps : RProps {
  var action: dynamic
  var anchorOrigin: dynamic
  var autoHideDuration: Int?
  var children: dynamic
  var ClickAwayListenerProps: dynamic
  var ContentProps: dynamic
  var disableWindowBlurListener: Boolean
  var message: dynamic
  var onClose: (event: dynamic) -> Unit
  var onEnter: (event: dynamic) -> Unit
  var onEntered: (event: dynamic) -> Unit
  var onEntering: (event: dynamic) -> Unit
  var onExit: (event: dynamic) -> Unit
  var onExiting: (event: dynamic) -> Unit
  var onExited: (event: dynamic) -> Unit
  var open: Boolean
  var resumeHideDuration: Int
  var TransitionComponent: dynamic
  var transitionDuration: dynamic
  var TransitionProps: dynamic
  var style: dynamic
  var classes: dynamic
}

val snackbar: RClass<SnackbarProps> = importedSnackbar.default

@JsModule("@material-ui/core/Slide")
private external val importedSlide: dynamic

interface SlideProps : RProps {
  var children: dynamic
  var direction: String

  @JsName("in")
  var in_: Boolean
  var timeout: Number
}

val slide: RClass<SlideProps> = importedSlide.default

@JsModule("@material-ui/core/Slider")
private external val importedSlider: dynamic

interface SliderProps : RProps {
  var classes: dynamic
  var color: String
  var defaultValue: dynamic
  var ValueLabelComponent: dynamic
  var marks: dynamic
  var value: dynamic
  var valueLabelDisplay: String
  var valueLabelFormat: (value: dynamic) -> String
  var onChange: (event: Event, value: dynamic) -> Unit
  var orientation: String
  var step: Int?
  var track: String
}

val slider: RClass<SliderProps> = importedSlider.default

@JsModule("@material-ui/core/Accordion")
private external val importedExpansionPanel: dynamic

interface AccordionProps : RProps {
  var CollapseProps: dynamic
  var TransitionProps: dynamic
  var defaultExpanded: Boolean
  var disabled: Boolean
  var expanded: Boolean
  var onChange: (event: dynamic, expanded: Boolean) -> Unit
}

val accordion: RClass<AccordionProps> = importedExpansionPanel.default


@JsModule("@material-ui/core/AccordionSummary")
private external val importedAccordionSummary: dynamic

interface AccordionSummaryProps : RProps {
  var expandIcon: dynamic
  var IconButtonProps: dynamic
}

val accordionSummary: RClass<AccordionSummaryProps> = importedAccordionSummary.default

@JsModule("@material-ui/core/AccordionDetails")
private external val importedAccordionDetails: dynamic

interface AccordionDetailsProps : RProps {
  var CollapseProps: dynamic
  var defaultExpanded: Boolean
  var disabled: Boolean
  var expanded: Boolean
  var className: String
  var onChange: (event: dynamic, expanded: Boolean) -> Unit
}

val accordionDetails: RClass<AccordionDetailsProps> = importedAccordionDetails.default

@JsModule("@material-ui/core/ExpansionPanelActions")
private external val importedExpansionPanelActions: dynamic

interface ExpansionPanelActionsProps : RProps

val expansionPanelActions: RClass<ExpansionPanelActionsProps> = importedExpansionPanelActions.default

@JsModule("@material-ui/core/Grid/Grid")
private external val gridImport: dynamic

// Props copied from: https://gitlab.com/AnimusDesign/KotlinReactMaterialUI
external interface GridProps : RProps {
  var className: String?
  var classes: dynamic
  var alignContent: dynamic /* String /* "stretch" */ | String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "space-between" */ | String /* "space-around" */ */ get() = definedExternally; set(value) = definedExternally
  var alignItems: dynamic /* String /* "stretch" */ | String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "baseline" */ */ get() = definedExternally; set(value) = definedExternally
  var component: dynamic /* String | React.ComponentType<Omit<GridProps, dynamic /* String /* "container" */ | String /* "item" */ | String /* "hidden" */ | String /* "classes" */ | String /* "className" */ | String /* "component" */ | String /* "alignContent" */ | String /* "alignItems" */ | String /* "direction" */ | String /* "spacing" */ | String /* "justify" */ | String /* "wrap" */ | String /* "xs" */ | String /* "sm" */ | String /* "md" */ | String /* "lg" */ | String /* "xl" */ */>> */ get() = definedExternally; set(value) = definedExternally
  var container: Boolean? get() = definedExternally; set(value) = definedExternally
  var direction: dynamic /* String /* "row" */ | String /* "row-reverse" */ | String /* "column" */ | String /* "column-reverse" */ */ get() = definedExternally; set(value) = definedExternally
  var item: Boolean? get() = definedExternally; set(value) = definedExternally
  var justify: dynamic /* String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "space-between" */ | String /* "space-around" */ */ get() = definedExternally; set(value) = definedExternally
  var spacing: dynamic /* Number /* 0 */ | Number /* 8 */ | Number /* 16 */ | Number /* 24 */ | Number /* 32 */ | Number /* 40 */ */ get() = definedExternally; set(value) = definedExternally
  var wrap: dynamic /* String /* "wrap" */ | String /* "nowrap" */ | String /* "wrap-reverse" */ */ get() = definedExternally; set(value) = definedExternally
  var zeroMinWidth: Boolean? get() = definedExternally; set(value) = definedExternally

  // values : auto , true, false and 1 - 12
  var xs: dynamic
  var sm: dynamic
  var md: dynamic
  var lg: dynamic
  var xl: dynamic
}

val grid: RClass<GridProps> = gridImport.default

@JsModule("@material-ui/core/Dialog")
private external val dialogImport: dynamic

interface DialogProps : RProps, ModalProps {
  var className: String
  var onClose: () -> Unit
  var fullWidth: Boolean
  var maxWidth: dynamic
  var classes: dynamic
  var fullScreen: Boolean
}

val muiDialog: RClass<DialogProps> = dialogImport.default


@JsModule("@material-ui/core/DialogTitle")
private external val dialogTitleImport: dynamic

val muiDialogTitle: RClass<RProps> = dialogTitleImport.default


@JsModule("@material-ui/core/DialogContent")
private external val dialogContentImport: dynamic

val muiDialogContent: RClass<RProps> = dialogContentImport.default

@JsModule("@material-ui/core/DialogContentText")
private external val dialogContentTextImport: dynamic

val muiDialogContentText: RClass<RProps> = dialogContentTextImport.default


@JsModule("@material-ui/core/DialogActions")
private external val dialogActionsImport: dynamic

val muiDialogActions: RClass<RProps> = dialogActionsImport.default

@JsModule("@material-ui/core/FormLabel")
private external val formLabelImport: dynamic

val muiFormLabel: RClass<RProps> = formLabelImport.default


@JsModule("@material-ui/core/Table/Table")
external val TableImport: dynamic

external interface TableProps : RProps {
  var className: String?
  var classes: dynamic
  var size: String?
  var padding: String
  var component: RComponent<RProps, RState>? get() = definedExternally; set(value) = definedExternally
  var stickyHeader: Boolean?
}


var mTable: RClass<TableProps> = TableImport.default

@JsModule("@material-ui/core/TableBody/TableBody")
external val TableBodyImport: dynamic

external interface TableBodyProps : RProps {
  var component: RComponent<RProps, RState>? get() = definedExternally; set(value) = definedExternally
}

var mTableBody: RClass<TableBodyProps> = TableBodyImport.default


@JsModule("@material-ui/core/TableHead/TableHead")
external val TableHeadImport: dynamic

external interface TableHeadProps : RProps {
  var component: String? get() = definedExternally; set(value) = definedExternally
}

var mTableHead: RClass<TableHeadProps> = TableHeadImport.default


@JsModule("@material-ui/core/TableRow/TableRow")
external val TableRowImport: dynamic


external interface TableRowProps : RProps {
  var component: RComponent<RProps, RState>? get() = definedExternally; set(value) = definedExternally
  var hover: Boolean? get() = definedExternally; set(value) = definedExternally
  var selected: Boolean? get() = definedExternally; set(value) = definedExternally
}

var mTableRow: RClass<TableRowProps> = TableRowImport.default

@JsModule("@material-ui/core/TableCell/TableCell")
external val TableCellImport: dynamic

external interface TableCellProps : RProps {
  var className: dynamic
  var component: String? get() = definedExternally; set(value) = definedExternally

  @Deprecated("Use align instead")
  var numeric: Boolean?
    get() = definedExternally
    set(value) = definedExternally
  var padding: dynamic /* String /* "default" */ | String /* "checkbox" */ | String /* "dense" */ | String /* "none" */ */ get() = definedExternally; set(value) = definedExternally
  var sortDirection: dynamic /* Boolean | String /* "asc" */ | String /* "desc" */ */ get() = definedExternally; set(value) = definedExternally
  var type: dynamic /* String /* "head" */ | String /* "body" */ | String /* "footer" */ */ get() = definedExternally; set(value) = definedExternally
  var align: String? get() = definedExternally; set(value) = definedExternally
  var colSpan: Int? get() = definedExternally; set(value) = definedExternally
  var rowSpan: String? get() = definedExternally; set(value) = definedExternally
  var variant: String
  var style: dynamic
  var width: String
  var scope: String
}

var mTableCell: RClass<TableCellProps> = TableCellImport.default

@JsModule("@material-ui/core/TableFooter/TableFooter")
external val TableFooterImport: dynamic

external interface TableFooterProps : RProps {
  var className: String
  var classes: dynamic
}

var mTableFooter: RClass<TableFooterProps> = TableFooterImport.default

@JsModule("@material-ui/core/TablePagination/TablePagination")
external val TablePaginationImport: dynamic

external interface TablePaginationProps : RProps {
  var rowsPerPage: Int
  var page: Int
  var onChangePage: (Event, Int) -> Unit
  var count: Int
  var rowsPerPageOptions: Array<Int>
  var onChangeRowsPerPage: (Event) -> Unit
}

var mTablePagination: RClass<TablePaginationProps> = TablePaginationImport.default

@JsModule("@material-ui/core/TableSortLabel")
external val TableSortLabelImport: dynamic

external interface TableSortLabelProps : RProps {
  var active: Boolean
  var classes: dynamic
  var direction: String
  var hideSortIcon: Boolean
  var onClick: (dynamic) -> Unit
}

var mTableSortLabel: RClass<TableSortLabelProps> = TableSortLabelImport.default


@JsModule("@material-ui/core/Chip")
external val ChipImport: dynamic

external interface ChipProps : RProps {
  var className: String?
  var classes: dynamic
  var avatar: ReactElement?
  var clickable: Boolean? get() = definedExternally; set(value) = definedExternally
  var color: String? get() = definedExternally; set(value) = definedExternally
  var deleteIcon: ReactElement?
  var icon: ReactElement?
  var label: String? get() = definedExternally; set(value) = definedExternally
  var onClick: (dynamic) -> Unit
  var onDelete: ((Event) -> Unit)?
  var tabIndex: dynamic
  var size: String
  var style: dynamic
  var variant: String?
}

var mChip: RClass<ChipProps> = ChipImport.default

@JsModule("@material-ui/core/Avatar")
external val AvatarImport: dynamic
var avatar: RClass<CheckboxProps> = AvatarImport.default

@JsModule("@material-ui/core/Checkbox/Checkbox")
external val CheckboxImport: dynamic

interface CheckboxProps : RProps {
  var checked: Boolean
  var value: dynamic
  var size: String
  var required: Boolean
  var color: String
  var icon: dynamic
  var checkedIcon: dynamic
  var disabled: Boolean

  var onChange: (event: dynamic, checked: Boolean) -> Unit
}

var mCheckbox: RClass<CheckboxProps> = CheckboxImport.default


@JsModule("@material-ui/core/Popover")
external val PopoverImport: dynamic

interface PopoverProps : RProps {
  var id: String
  var classes: dynamic
  var anchorEl: dynamic
  var onClose: (event: Event) -> Unit
  var onEnter: () -> Unit
  var onEntered: () -> Unit
  var onEntering: () -> Unit
  var onExit: () -> Unit
  var onExited: () -> Unit
  var onExiting: () -> Unit
  var open: Boolean
  var anchorOrigin: dynamic
  var transformOrigin: dynamic
}

var popover: RClass<PopoverProps> = PopoverImport.default

@JsModule("@material-ui/core/Tooltip")
external val TooltipImport: dynamic

interface TooltipProps : RProps {
  var title: String
  var open: Boolean?
  var enterDelay: Int
  var leaveDelay: Int
  var placement: String
  var classes: dynamic
}

var muiTooltip: RClass<TooltipProps> = TooltipImport.default


@JsModule("@material-ui/core/Grow")
external val GrowImport: dynamic

interface GrowProps : RProps {
  @JsName("in")
  var show: Boolean
  var timeout: dynamic
  var style: dynamic
  var onExited: (dynamic) -> Unit

}

var grow: RClass<GrowProps> = GrowImport.default