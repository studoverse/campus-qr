@file:Suppress("UnsafeCastFromDynamic", "unused", "PropertyName") // Don't warn about api design of external lib

package webcore.materialUI

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import kotlin.reflect.KClass


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

external interface AppBarProps : Props {
  var position: String
  var color: String
  var className: String
  var elevation: Int
}

val appBar: ComponentClass<AppBarProps> = importedAppBar.default


@JsModule("@material-ui/core/Toolbar")
private external val importedToolbar: dynamic

external interface ToolbarProps : Props {
  var disableGutters: Boolean
  var variant: String // "regular" "dense"
  var className: String
  var classes: dynamic
}

val toolbar: ComponentClass<ToolbarProps> = importedToolbar.default


@JsModule("@material-ui/core/IconButton")
private external val importedIconButton: dynamic

external interface IconButtonProps : Props {
  var disabled: Boolean
  var label: String
  var size: String
  var color: String // 'default' | 'inherit' | 'primary' | 'secondary'
  var className: String
  var onClick: (Event) -> Unit
  var classes: dynamic
  var disableRipple: Boolean
  var component: dynamic
  var ref: (HTMLButtonElement) -> Unit
}

val iconButton: ComponentClass<IconButtonProps> = importedIconButton.default


@JsModule("@material-ui/icons/Menu")
private external val importedMenuIcon: dynamic
val menuIcon: ComponentClass<IconProps> = importedMenuIcon.default

@JsModule("@material-ui/icons/ExpandMore")
private external val importedExpandMoreIcon: dynamic
val expandMoreIcon: ComponentClass<IconProps> = importedExpandMoreIcon.default

@JsModule("@material-ui/icons/List")
private external val importedListIcon: dynamic
val listIcon: ComponentClass<IconProps> = importedListIcon.default

external interface IconProps : Props {
  var color: String
  var className: String
  var fontSize: String
  var style: dynamic
  var classes: dynamic
}

@JsModule("@material-ui/icons/Domain")
private external val importedDomainIcon: dynamic
val domainIcon: ComponentClass<IconProps> = importedDomainIcon.default

@JsModule("@material-ui/icons/Notes")
private external val importedNotesIcon: dynamic
val notesIcon: ComponentClass<IconProps> = importedNotesIcon.default

@JsModule("@material-ui/icons/Delete")
private external val importedDeleteIcon: dynamic
val deleteIcon: ComponentClass<IconProps> = importedDeleteIcon.default

@JsModule("@material-ui/icons/Save")
private external val importedSaveIcon: dynamic
val saveIcon: ComponentClass<IconProps> = importedSaveIcon.default

@JsModule("@material-ui/icons/FilterList")
private external val importedFilterListIcon: dynamic
val filterListIcon: ComponentClass<IconProps> = importedFilterListIcon.default

@JsModule("@material-ui/icons/Visibility")
private external val importedVisibilityIcon: dynamic
val visibilityIcon: ComponentClass<IconProps> = importedVisibilityIcon.default

@JsModule("@material-ui/icons/Public")
private external val importedPublicIcon: dynamic
val publicIcon: ComponentClass<IconProps> = importedPublicIcon.default

@JsModule("@material-ui/icons/DeleteForeverRounded")
private external val importedDeleteForeverRoundedIcon: dynamic
val deleteForeverRoundedIcon: ComponentClass<IconProps> = importedDeleteForeverRoundedIcon.default

@JsModule("@material-ui/icons/VisibilityOff")
private external val importedVisibilityOffIcon: dynamic
val visibilityOffIcon: ComponentClass<IconProps> = importedVisibilityOffIcon.default

@JsModule("@material-ui/icons/RemoveRedEye")
private external val importedRemoveRedEyeIcon: dynamic
val removeRedEyeIcon: ComponentClass<IconProps> = importedRemoveRedEyeIcon.default

@JsModule("@material-ui/icons/ShoppingCart")
private external val importedShoppingCart: dynamic
val shoppingCartIcon: ComponentClass<IconProps> = importedShoppingCart.default

@JsModule("@material-ui/icons/CreditCard")
private external val importedCreditCard: dynamic
val creditCardIcon: ComponentClass<IconProps> = importedCreditCard.default

@JsModule("@material-ui/icons/TimerOffOutlined")
private external val importedTimerOffOutlined: dynamic
val timerOffOutlinedIcon: ComponentClass<IconProps> = importedTimerOffOutlined.default

@JsModule("@material-ui/icons/Receipt")
private external val importedReceipt: dynamic
val receiptIcon: ComponentClass<IconProps> = importedReceipt.default

@JsModule("@material-ui/icons/AspectRatioOutlined")
private external val importedAspectRatioIcon: dynamic
val aspectRatioIcon: ComponentClass<IconProps> = importedAspectRatioIcon.default

@JsModule("@material-ui/icons/LocalGasStation")
private external val importedLocalGasStationIcon: dynamic
val localGasStationIcon: ComponentClass<IconProps> = importedLocalGasStationIcon.default

@JsModule("@material-ui/icons/Edit")
private external val importedEditIcon: dynamic
val editIcon: ComponentClass<IconProps> = importedEditIcon.default

@JsModule("@material-ui/icons/LocationOn")
private external val importedLocationOn: dynamic
val locationOnIcon: ComponentClass<IconProps> = importedLocationOn.default

@JsModule("@material-ui/icons/HourglassEmpty")
private external val importedHourGlassEmptyIcon: dynamic
val hourGlassEmptyIcon: ComponentClass<IconProps> = importedHourGlassEmptyIcon.default

@JsModule("@material-ui/icons/Refresh")
private external val importedRefreshIcon: dynamic
val refreshIcon: ComponentClass<IconProps> = importedRefreshIcon.default

@JsModule("@material-ui/icons/Timelapse")
private external val importedTimelapseIcon: dynamic
val timelapseIcon: ComponentClass<IconProps> = importedTimelapseIcon.default

@JsModule("@material-ui/icons/Payment")
private external val importedPaymentIcon: dynamic
val paymentIcon: ComponentClass<IconProps> = importedPaymentIcon.default

@JsModule("@material-ui/icons/Assessment")
private external val importedAssessmentIcon: dynamic
val assessmentIcon: ComponentClass<IconProps> = importedAssessmentIcon.default

@JsModule("@material-ui/icons/Poll")
private external val importedPollIcon: dynamic
val pollIcon: ComponentClass<IconProps> = importedPollIcon.default

@JsModule("@material-ui/icons/Toc")
private external val importedTocIcon: dynamic
val tocIcon: ComponentClass<IconProps> = importedTocIcon.default

@JsModule("@material-ui/icons/CheckCircleOutline")
private external val importedCheckCircleOutlineIcon: dynamic
val checkCircleOutlineIcon: ComponentClass<IconProps> = importedCheckCircleOutlineIcon.default

@JsModule("@material-ui/icons/CheckCircle")
private external val importedCheckCircleIcon: dynamic
val checkCircleIcon: ComponentClass<IconProps> = importedCheckCircleIcon.default

@JsModule("@material-ui/icons/RemoveCircleOutline")
private external val importedRemoveCircleOutline: dynamic
val removeCircleOutlineIcon: ComponentClass<IconProps> = importedRemoveCircleOutline.default

@JsModule("@material-ui/icons/Check")
private external val importedCheckIcon: dynamic
val checkIcon: ComponentClass<IconProps> = importedCheckIcon.default

@JsModule("@material-ui/icons/Warning")
private external val importedWarningIcon: dynamic
val warningIcon: ComponentClass<IconProps> = importedWarningIcon.default

@JsModule("@material-ui/icons/Info")
private external val importedInfoIcon: dynamic
val infoIcon: ComponentClass<IconProps> = importedInfoIcon.default

@JsModule("@material-ui/icons/InfoOutlined")
private external val importedInfoOutlinedIcon: dynamic
val infoOutlinedIcon: ComponentClass<IconProps> = importedInfoOutlinedIcon.default

@JsModule("@material-ui/icons/ImageRounded")
private external val importedImageRoundedIcon: dynamic
val imageRoundedIcon: ComponentClass<IconProps> = importedImageRoundedIcon.default

@JsModule("@material-ui/icons/SettingsBackupRestore")
private external val importedSettingsBackupRestoreIcon: dynamic
val settingsBackupRestoreIcon: ComponentClass<IconProps> = importedSettingsBackupRestoreIcon.default

@JsModule("@material-ui/icons/Fullscreen")
private external val importedFullscreenIcon: dynamic
val fullscreenIcon: ComponentClass<IconProps> = importedFullscreenIcon.default

@JsModule("@material-ui/icons/Cancel")
private external val importedCancel: dynamic
val cancelIcon: ComponentClass<IconProps> = importedCancel.default

@JsModule("@material-ui/icons/CancelOutlined")
private external val importedCancelOutlined: dynamic
val cancelOutlinedIcon: ComponentClass<IconProps> = importedCancelOutlined.default


@JsModule("@material-ui/icons/FlashOn")
private external val importedFlashOn: dynamic
val flashOnIcon: ComponentClass<IconProps> = importedFlashOn.default


@JsModule("@material-ui/icons/Search")
private external val importedSearchIcon: dynamic
val searchIcon: ComponentClass<IconProps> = importedSearchIcon.default

@JsModule("@material-ui/icons/MoreVert")
private external val importedMoreVertIcon: dynamic
val moreVertIcon: ComponentClass<IconProps> = importedMoreVertIcon.default

@JsModule("@material-ui/icons/MoreHoriz")
private external val importedMoreHorizIcon: dynamic
val moreHorizIcon: ComponentClass<IconProps> = importedMoreHorizIcon.default

@JsModule("@material-ui/icons/BookmarkBorder")
private external val importedBookmarkBorderIcon: dynamic
val bookmarkBorderIcon: ComponentClass<IconProps> = importedBookmarkBorderIcon.default

@JsModule("@material-ui/icons/Chat")
private external val importedChatIcon: dynamic
val chatIcon: ComponentClass<IconProps> = importedChatIcon.default

@JsModule("@material-ui/icons/Clear")
private external val importedClearIcon: dynamic
val clearIcon: ComponentClass<IconProps> = importedClearIcon.default

@JsModule("@material-ui/icons/ArrowBack")
private external val importedArrowBackIcon: dynamic
val arrowBackIcon: ComponentClass<IconProps> = importedArrowBackIcon.default

@JsModule("@material-ui/icons/Settings")
private external val importedSettingsIcon: dynamic
val settingsIcon: ComponentClass<IconProps> = importedSettingsIcon.default

@JsModule("@material-ui/icons/Launch")
private external val importedLaunchIcon: dynamic
val launchIcon: ComponentClass<IconProps> = importedLaunchIcon.default

@JsModule("@material-ui/icons/Cake")
private external val importedCakeIcon: dynamic
val cakeIcon: ComponentClass<IconProps> = importedCakeIcon.default

@JsModule("@material-ui/icons/Send")
private external val importedSendIcon: dynamic
val sendIcon: ComponentClass<IconProps> = importedSendIcon.default

@JsModule("@material-ui/icons/AddCircleOutline")
private external val importedAddCircleOutlineIcon: dynamic
val addCircleOutlineIcon: ComponentClass<IconProps> = importedAddCircleOutlineIcon.default


@JsModule("@material-ui/icons/Add")
private external val importedAddIcon: dynamic
val addIcon: ComponentClass<IconProps> = importedAddIcon.default

@JsModule("@material-ui/icons/AccountCircle")
private external val importedAccountCircleIcon: dynamic
val accountCircleIcon: ComponentClass<IconProps> = importedAccountCircleIcon.default

@JsModule("@material-ui/icons/CalendarToday")
private external val importedCalendarTodayIcon: dynamic
val calendarIcon: ComponentClass<IconProps> = importedCalendarTodayIcon.default

@JsModule("@material-ui/icons/PowerSettingsNew")
private external val importedPowerSettingsNewIcon: dynamic
val logoutIcon: ComponentClass<IconProps> = importedPowerSettingsNewIcon.default

@JsModule("@material-ui/icons/Share")
private external val importedShareIcon: dynamic
val shareIcon: ComponentClass<IconProps> = importedShareIcon.default

@JsModule("@material-ui/icons/Error")
private external val importedErrorIcon: dynamic
val errorIcon: ComponentClass<IconProps> = importedErrorIcon.default

@JsModule("@material-ui/icons/BugReport")
private external val importedBugReportIcon: dynamic
val bugReportIcon: ComponentClass<IconProps> = importedBugReportIcon.default

@JsModule("@material-ui/icons/People")
private external val importedPeopleIcon: dynamic
val peopleIcon: ComponentClass<IconProps> = importedPeopleIcon.default

@JsModule("@material-ui/icons/Business")
private external val importedBusinessIcon: dynamic
val businessIcon: ComponentClass<IconProps> = importedBusinessIcon.default

@JsModule("@material-ui/icons/PeopleOutline")
private external val importedPeopleOutlineIcon: dynamic
val peopleOutlineIcon: ComponentClass<IconProps> = importedPeopleOutlineIcon.default

@JsModule("@material-ui/icons/Redeem")
private external val importedRedeemIcon: dynamic
val redeemIcon: ComponentClass<IconProps> = importedRedeemIcon.default

@JsModule("@material-ui/icons/Restaurant")
private external val importedRestaurantIcon: dynamic
val restaurantIcon: ComponentClass<IconProps> = importedRestaurantIcon.default

@JsModule("@material-ui/icons/ArtTrack")
private external val importedArtTrackIcon: dynamic
val artTrackIcon: ComponentClass<IconProps> = importedArtTrackIcon.default

@JsModule("@material-ui/icons/PagesRounded")
private external val importedPagesRoundedIcon: dynamic
val pagesRoundedIcon: ComponentClass<IconProps> = importedPagesRoundedIcon.default

@JsModule("@material-ui/icons/MeetingRoom")
private external val importedMeetingRoomIcon: dynamic
val meetingRoomIcon: ComponentClass<IconProps> = importedMeetingRoomIcon.default

@JsModule("@material-ui/icons/Equalizer")
private external val importedEqualizerIcon: dynamic
val equalizerIcon: ComponentClass<IconProps> = importedEqualizerIcon.default

@JsModule("@material-ui/icons/CloudDownload")
private external val importedCloudDownloadIcon: dynamic
val cloudDownloadIcon: ComponentClass<IconProps> = importedCloudDownloadIcon.default

@JsModule("@material-ui/icons/WorkOutline")
private external val importedWorkOutlineIcon: dynamic
val workOutlineIcon: ComponentClass<IconProps> = importedWorkOutlineIcon.default

@JsModule("@material-ui/icons/ImageOutlined")
private external val importedImageOutlinedIcon: dynamic
val imageOutlinedIcon: ComponentClass<IconProps> = importedImageOutlinedIcon.default

@JsModule("@material-ui/icons/Link")
private external val importedLinkIcon: dynamic
val linkIcon: ComponentClass<IconProps> = importedLinkIcon.default

@JsModule("@material-ui/icons/WhereToVote")
private external val importedWhereToVoteIcon: dynamic
val whereToVoteIcon: ComponentClass<IconProps> = importedWhereToVoteIcon.default

@JsModule("@material-ui/icons/Block")
private external val importedBlockIcon: dynamic
val blockIcon: ComponentClass<IconProps> = importedBlockIcon.default

@JsModule("@material-ui/icons/GroupAdd")
private external val importedGroupAddIcon: dynamic
val groupAddIcon: ComponentClass<IconProps> = importedGroupAddIcon.default

@JsModule("@material-ui/icons/BlurCircular")
private external val importedBlurCircularIcon: dynamic
val blurCircularIcon: ComponentClass<IconProps> = importedBlurCircularIcon.default

@JsModule("@material-ui/icons/LockOpen")
private external val importedLockOpenIcon: dynamic
val lockOpenIcon: ComponentClass<IconProps> = importedLockOpenIcon.default

@JsModule("@material-ui/icons/PlaylistAddCheck")
private external val importedPlaylistAddCheckIcon: dynamic
val playlistAddCheckIcon: ComponentClass<IconProps> = importedPlaylistAddCheckIcon.default

@JsModule("@material-ui/icons/LinkOff")
private external val importedLinkOffIcon: dynamic
val linkOffIcon: ComponentClass<IconProps> = importedLinkOffIcon.default

@JsModule("@material-ui/icons/InsertEmoticonOutlined")
private external val importedInsertEmoticonOutlinedIcon: dynamic
val insertEmoticonOutlinedIcon: ComponentClass<IconProps> = importedInsertEmoticonOutlinedIcon.default

@JsModule("@material-ui/icons/TrendingUp")
private external val importedTrendingUpIcon: dynamic
val trendingUpIcon: ComponentClass<IconProps> = importedTrendingUpIcon.default

@JsModule("@material-ui/icons/Close")
private external val importedCloseIcon: dynamic
val closeIcon: ComponentClass<IconProps> = importedCloseIcon.default

@JsModule("@material-ui/icons/RssFeed")
private external val importedRssFeedIcon: dynamic
val rssFeedIcon: ComponentClass<IconProps> = importedRssFeedIcon.default

@JsModule("@material-ui/icons/NotificationsOutlined")
private external val importedNotificationsOutlinedIcon: dynamic
val notificationsOutlinedIcon: ComponentClass<IconProps> = importedNotificationsOutlinedIcon.default

@JsModule("@material-ui/icons/Notifications")
private external val importedNotificationsIcon: dynamic
val notificationsIcon: ComponentClass<IconProps> = importedNotificationsIcon.default

@JsModule("@material-ui/icons/Undo")
private external val importedUndoIcon: dynamic
val undoIcon: ComponentClass<IconProps> = importedUndoIcon.default

@JsModule("@material-ui/icons/NavigateNext")
private external val importedNavigateNextIcon: dynamic
val navigateNextIcon: ComponentClass<IconProps> = importedNavigateNextIcon.default

@JsModule("@material-ui/icons/VerifiedUser")
private external val importedVerifiedUserIcon: dynamic
val verifiedUserIcon: ComponentClass<IconProps> = importedVerifiedUserIcon.default

@JsModule("@material-ui/icons/SupervisedUserCircle")
private external val importedSupervisedUserCircleIcon: dynamic
val supervisedUserCircleIcon: ComponentClass<IconProps> = importedSupervisedUserCircleIcon.default

@JsModule("@material-ui/icons/Person")
private external val importedPersonIcon: dynamic
val personIcon: ComponentClass<IconProps> = importedPersonIcon.default

@JsModule("@material-ui/icons/Build")
private external val importedBuildIcon: dynamic
val buildIcon: ComponentClass<IconProps> = importedBuildIcon.default

@JsModule("@material-ui/icons/FileCopyOutlined")
private external val importedFileCopyOutlinedIcon: dynamic
val fileCopyOutlinedIcon: ComponentClass<IconProps> = importedFileCopyOutlinedIcon.default

@JsModule("@material-ui/icons/History")
private external val importedHistoryIcon: dynamic
val historyIcon: ComponentClass<IconProps> = importedHistoryIcon.default

@JsModule("@material-ui/icons/Schedule")
private external val importedScheduleIcon: dynamic
val scheduleIcon: ComponentClass<IconProps> = importedScheduleIcon.default

@JsModule("@material-ui/icons/Publish")
private external val importedPublishIcon: dynamic
val publishIcon: ComponentClass<IconProps> = importedPublishIcon.default

@JsModule("@material-ui/icons/HighlightOff")
private external val importedHighlightOffIcon: dynamic
val highlightOffIcon: ComponentClass<IconProps> = importedHighlightOffIcon.default

@JsModule("@material-ui/icons/School")
private external val importedSchoolIcon: dynamic
val schoolIcon: ComponentClass<IconProps> = importedSchoolIcon.default

@JsModule("@material-ui/icons/Assignment")
private external val importedAssignmentIcon: dynamic
val assignmentIcon: ComponentClass<IconProps> = importedAssignmentIcon.default

@JsModule("@material-ui/icons/ContactMail")
private external val importedContactMailIcon: dynamic
val contactMailIcon: ComponentClass<IconProps> = importedContactMailIcon.default

@JsModule("@material-ui/icons/CloudUpload")
private external val importedCloudUploadIcon: dynamic
val cloudUploadIcon: ComponentClass<IconProps> = importedCloudUploadIcon.default

@JsModule("@material-ui/icons/NotInterested")
private external val importedNotInterestedIcon: dynamic
val notInterestedIcon: ComponentClass<IconProps> = importedNotInterestedIcon.default

@JsModule("@material-ui/icons/ArrowForward")
private external val importedArrowForwardIcon: dynamic
val arrowForwardIcon: ComponentClass<IconProps> = importedArrowForwardIcon.default

@JsModule("@material-ui/icons/ArrowUpward")
private external val importedArrowUpwardIcon: dynamic
val arrowUpwardIcon: ComponentClass<IconProps> = importedArrowUpwardIcon.default

@JsModule("@material-ui/icons/ArrowDownward")
private external val importedArrowDownwardIcon: dynamic
val arrowDownwardIcon: ComponentClass<IconProps> = importedArrowDownwardIcon.default

@JsModule("@material-ui/icons/Web")
private external val importedWebIcon: dynamic
val webIcon: ComponentClass<IconProps> = importedWebIcon.default

@JsModule("@material-ui/icons/AccessTime")
private external val importedAccessTimeIcon: dynamic
val accessTimeIcon: ComponentClass<IconProps> = importedAccessTimeIcon.default

@JsModule("@material-ui/icons/Sync")
private external val importedSyncIcon: dynamic
val syncIcon: ComponentClass<IconProps> = importedSyncIcon.default

@JsModule("@material-ui/icons/Sort")
private external val importedSortIcon: dynamic
val sortIcon: ComponentClass<IconProps> = importedSortIcon.default

@JsModule("@material-ui/icons/PhoneAndroid")
private external val importedPhoneAndroidIcon: dynamic
val phoneAndroidIcon: ComponentClass<IconProps> = importedPhoneAndroidIcon.default

@JsModule("@material-ui/icons/Crop")
private external val importedCropIcon: dynamic
val cropIcon: ComponentClass<IconProps> = importedCropIcon.default

@JsModule("@material-ui/icons/RotateRight")
private external val importedRotateRightIcon: dynamic
val rotateRightIcon: ComponentClass<IconProps> = importedRotateRightIcon.default

@JsModule("@material-ui/core/Typography")
private external val importedTypography: dynamic

external interface TypographyProps : Props {
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

val typography: ComponentClass<TypographyProps> = importedTypography.default

@JsModule("@material-ui/core/Menu")
private external val importedMenu: dynamic

external interface MenuProps : Props {
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

val menu: ComponentClass<MenuProps> = importedMenu.default


@JsModule("@material-ui/core/MenuItem")
private external val importedMenuItem: dynamic


external interface MenuItemProps : Props {
  var value: String
  var onClick: (event: Event) -> Unit
  var disabled: Boolean
  var selected: Boolean
  var classes: dynamic
}

val menuItem: ComponentClass<MenuItemProps> = importedMenuItem.default

@JsModule("@material-ui/core/Box")
private external val importedBox: dynamic

external interface FlexboxProps : Props {
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

external interface DisplayProps : Props {
  var displayPrint: String
  var display: String
  var overflow: String
  var textOverflow: String
  var visibility: String
  var flex: String
  var whiteSpace: String
}

external interface BoxProps : FlexboxProps, DisplayProps {
  var className: String
  var onClick: (event: Event) -> Unit
}

val box: ComponentClass<BoxProps> = importedBox.default


@JsModule("@material-ui/core/Drawer")
private external val importedDrawer: dynamic

external interface DrawerProps : Props {
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

val drawer: ComponentClass<DrawerProps> = importedDrawer.default


@JsModule("@material-ui/core/List")
private external val importedList: dynamic

external interface ListProps : Props {
  var component: dynamic
  var dense: Boolean
  var disablePadding: Boolean
  var subheader: dynamic

}

val list: ComponentClass<ListProps> = importedList.default

@JsModule("@material-ui/core/Modal")
private external val importedModal: dynamic

external interface ModalProps : Props {
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

val modal: ComponentClass<ModalProps> = importedList.default

@JsModule("@material-ui/core/ListSubheader")
private external val importedListSubheader: dynamic

external interface ListSubheaderProps : Props

val listSubheader: ComponentClass<ListProps> = importedListSubheader.default

@JsModule("@material-ui/core/ListItem")
private external val importedListItem: dynamic

external interface ListItemProps : Props {
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

val listItem: ComponentClass<ListItemProps> = importedListItem.default


@JsModule("@material-ui/core/ListItemIcon")
private external val importedListItemIcon: dynamic

external interface ListItemIconProps : Props {
  var classes: dynamic
}

val listItemIcon: ComponentClass<ListItemIconProps> = importedListItemIcon.default

@JsModule("@material-ui/core/ListItemText")
private external val importedListItemText: dynamic

external interface ListItemTextProps : Props {
  var classes: dynamic
  var className: String
  var disableTypography: Boolean
  var inset: Boolean
  var primary: dynamic
  var primaryTypographyProps: dynamic
  var secondary: dynamic
  var secondaryTypographyProps: dynamic
}

val listItemText: ComponentClass<ListItemTextProps> = importedListItemText.default


@JsModule("@material-ui/core/Divider")
private external val importedDivider: dynamic

external interface DividerProps : Props {
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

val divider: ComponentClass<DividerProps> = importedDivider.default

@JsModule("@material-ui/core/Backdrop")
private external val importedBackdrop: dynamic

external interface BackdropProps : Props {
  var invisible: Boolean
  var classes: dynamic
  var open: Boolean
  var className: String
  var transitionDuration: Int
}

val backdrop: ComponentClass<BackdropProps> = importedBackdrop.default

@JsModule("@material-ui/core/styles/createTheme")
private external val importedCreateMuiTheme: dynamic
val createTheme: (dynamic) -> dynamic = importedCreateMuiTheme.default

@JsModule("@material-ui/core/styles/createPalette")
private external val importedCreatePalette: dynamic
val createPalette: (dynamic) -> dynamic = importedCreatePalette.default

@JsModule("@material-ui/styles/useTheme")
external val importedUseTheme: dynamic
fun useTheme() = importedUseTheme.default()


@JsModule("@material-ui/core/styles/withStyles")
external val importedWithStyles: dynamic

@Suppress("UNUSED_PARAMETER")
private fun <T : ComponentClass<*>> importedWithStyles(styles: dynamic, options: dynamic = undefined): (T) -> T =
  importedWithStyles.default(styles)


fun <P : Props, T : ComponentClass<P>> withStyles(
  styles: dynamic,
  component: T,
  options: dynamic = undefined
): T {
  return ((importedWithStyles<T>(styles, options))(component))
}

// TODO: @mh Either fix or remove because after migration sx will be used for styling instead
fun <P : Props, T : ComponentClass<P>> RBuilder.withStyles(
  styles: dynamic,
  component: T,
  options: dynamic = undefined,
  handler: RElementBuilder<P>.() -> Unit
): T {
  return ((importedWithStyles<T>(styles, options))(component)).also { it.invoke(handler) }
}

@Suppress("UnnecessaryVariable", "FunctionName")
fun <P : Props, C : Component<P, *>> withStyles_compilerBug(
  styles: dynamic,
  options: dynamic = undefined,
  kClass: KClass<C>
): ComponentClass<P> {
  val higherOrderComponent: ComponentClass<P> = importedWithStyles.default(styles, options)(kClass.js)
  return higherOrderComponent
}

inline fun <P : Props, reified C : Component<P, *>> withStyles(
  styles: dynamic,
  options: dynamic = undefined
): ComponentClass<P> {
  return withStyles_compilerBug(styles, options, C::class)
}

@JsModule("@material-ui/core/styles")
private external val importedMuiThemeProvider: dynamic

@JsModule("@material-ui/styles/ThemeProvider")
private external val importedThemeProvider: dynamic
val themeProvider: ComponentClass<MuiThemeProviderProps> = importedThemeProvider.default


external interface ButtonBaseProps : Props {
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

external interface ButtonProps : ButtonBaseProps {
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

val muiButton: ComponentClass<ButtonProps> = importedButton.default

@JsModule("@material-ui/core/Collapse")
private external val importedCollapse: dynamic

external interface CollapseProps : Props {
  var classes: dynamic
  var collapsedHeight: String
  var timeout: dynamic // can be a number or "auto"

  @JsName("in")
  var inProp: Boolean
}

val collapse: ComponentClass<CollapseProps> = importedCollapse.default

@JsModule("@material-ui/core/Link")
private external val importedLink: dynamic

external interface LinkProps : TypographyProps {
  var href: String
  var target: String
  var underline: String  //'none'| 'hover'| 'always'
  var rel: String
  var type: String // forwarded to button (if you choose component="button")
}

val muiLink: ComponentClass<LinkProps> = importedLink.default


external interface MuiThemeProviderProps : Props {
  var theme: dynamic
}

val muiThemeProvider: ComponentClass<MuiThemeProviderProps> = importedMuiThemeProvider.MuiThemeProvider

@JsModule("@material-ui/core/Hidden")
private external val importedHidden: dynamic

external interface HiddenProps : Props {
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

val hidden: ComponentClass<HiddenProps> = importedHidden.default

@JsModule("@material-ui/core/MobileStepper")
private external val importedMobileStepper: dynamic

external interface MobileStepperProps : Props {
  var activeStep: Int
  var backButton: ReactNode?
  var LinearProgressProps: dynamic
  var classes: dynamic
  var nextButton: ReactNode?
  var position: String
  var steps: Int
  var variant: String
}

val mobileStepper: ComponentClass<MobileStepperProps> = importedMobileStepper.default

@JsModule("@material-ui/core/Stepper")
private external val importedStepper: dynamic

external interface StepperProps : Props {
  var activeStep: Int
  var alternativeLabel: Boolean
  var connector: dynamic
  var nonLinear: Boolean
  var orientation: String
}

val stepper: ComponentClass<StepperProps> = importedStepper.default


@JsModule("@material-ui/core/RadioGroup")
private external val importedRadioGroup: dynamic

external interface RadioGroupProps : Props {
  var className: String
  var defaultValue: dynamic
  var value: dynamic
  var onChange: (Event) -> Unit
  var name: String
}

val radioGroup: ComponentClass<RadioGroupProps> = importedRadioGroup.default

@JsModule("@material-ui/core/Radio")
private external val importedRadio: dynamic

external interface RadioProps : Props {
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
  var icon: ComponentClass<IconProps>
  var disableRipple: Boolean
}

val radio: ComponentClass<RadioProps> = importedRadio.default

@JsModule("@material-ui/core/Step")
private external val importedStep: dynamic

external interface StepProps : Props {
  var active: Boolean
  var completed: Boolean
  var disabled: Boolean
  var stepIcon: dynamic
}

val step: ComponentClass<StepProps> = importedStep.default

@JsModule("@material-ui/core/StepLabel")
private external val importedStepLabel: dynamic

external interface StepLabelProps : Props {
  var disabled: Boolean
  var error: Boolean
  var icon: dynamic
  var optional: dynamic
  var StepIconComponent: dynamic
  var StepIconProps: dynamic
}

val stepLabel: ComponentClass<StepLabelProps> = importedStepLabel.default

@JsModule("@material-ui/core/StepContent")
private external val importedStepContent: dynamic

external interface StepContentProps : Props {
  var classes: dynamic
}

val stepContent: ComponentClass<StepContentProps> = importedStepContent.default

@JsModule("@material-ui/core/StepIcon")
private external val importedStepIcon: dynamic

external interface StepIconProps : Props {
  var classes: dynamic
  var icon: dynamic
}

val stepIcon: ComponentClass<StepIconProps> = importedStepIcon.default

@JsModule("@material-ui/core/StepConnector")
private external val importedStepConnector: dynamic

external interface StepConnectorProps : Props {
  var classes: dynamic
}

val stepConnector: ComponentClass<StepConnectorProps> = importedStepConnector.default


@JsModule("@material-ui/core/StepButton")
private external val importedStepButton: dynamic

external interface StepButtonProps : Props {
  var icon: dynamic
  var completed: Boolean
  var optional: dynamic
  var onClick: (Event) -> Unit
}

val stepButton: ComponentClass<StepButtonProps> = importedStepButton.default

@JsModule("@material-ui/core/Tabs")
private external val importedTabs: dynamic

external interface TabsProps : Props {
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

val tabs: ComponentClass<TabsProps> = importedTabs.default


@JsModule("@material-ui/core/Tab")
private external val importedTab: dynamic

external interface TabProps : Props {
  var classes: dynamic
  var disabled: Boolean
  var icon: dynamic
  var label: dynamic
  var value: dynamic
}

val tab: ComponentClass<TabProps> = importedTab.default


@JsModule("@material-ui/core/Switch")
private external val importedSwitch: dynamic

external interface SwitchProps : Props {
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

val switch: ComponentClass<SwitchProps> = importedSwitch.default

@JsModule("@material-ui/core/FormGroup")
private external val importedFormGroup: dynamic

external interface FormGroupProps : Props {
  var className: String
  var row: Boolean
  var children: dynamic
  var classes: dynamic
}

val formGroup: ComponentClass<FormGroupProps> = importedFormGroup.default

@JsModule("@material-ui/core/FormControlLabel")
private external val importedFormControlLabel: dynamic

external interface FormControlLabelProps : Props {
  var checked: Boolean
  var classes: dynamic
  var control: ReactElement<*>
  var disabled: Boolean
  var inputRef: dynamic
  var label: dynamic
  var labelPlacement: String
  var name: String
  var onChange: (dynamic) -> Unit
  var value: String
}

val formControlLabel: ComponentClass<FormControlLabelProps> = importedFormControlLabel.default

@JsModule("@material-ui/core/InputBase")
private external val importedInputBase: dynamic

external interface InputBaseProps : Props {
  var autoComplete: String
  var autoFocus: Boolean
  var classes: dynamic
  var className: String
  var defaultValue: dynamic
  var disabled: Boolean
  var error: Boolean
  var fullWidth: Boolean
  var endAdornment: ReactNode?
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
  var startAdornment: ReactNode?
  var placeholder: String
  var type: String
  var value: dynamic
}

val inputBase: ComponentClass<InputBaseProps> = importedInputBase.default


@JsModule("@material-ui/core/TextField")
private external val importedTextField: dynamic

external interface TextFieldProps : InputBaseProps {
  var FormHelperTextProps: dynamic
  var helperText: dynamic
  var InputLabelProps: dynamic
  var label: dynamic
  var select: Boolean
  var SelectProps: dynamic

  /** See [TextFieldVariant] */
  var variant: String
  var style: dynamic
  var InputProps: dynamic
}

val textField: ComponentClass<TextFieldProps> = importedTextField.default

enum class TextFieldVariant(val value: String) {
  STANDARD("standard"), OUTLINED("outlined"), FILLED("filled")
}


@JsModule("@material-ui/lab/Autocomplete")
private external val importedAutocomplete: dynamic

external interface AutocompleteProps<T> : Props {
  var id: String
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
  var onChange: (event: dynamic, value: dynamic, reason: String) -> Unit // reason: create-option", "select-option", "remove-option", "blur", "clear"
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

val muiAutocomplete: ComponentClass<AutocompleteProps<dynamic>> = importedAutocomplete.default


@JsModule("@material-ui/core/InputAdornment")
private external val importedInputAdornment: dynamic

external interface InputAdornmentProps : Props {
  var classes: dynamic
  var component: dynamic
  var disableTypography: Boolean
  var position: String
}

val inputAdornment: ComponentClass<InputAdornmentProps> = importedInputAdornment.default

@JsModule("@material-ui/core/CircularProgress")
private external val importedCircularProgress: dynamic

external interface CircularProgressProps : Props {
  var classes: dynamic
  var className: dynamic
  var color: String
  var disableShrink: Boolean
  var size: dynamic
  var thickness: Double
  var value: Int
  var variant: String // 'determinate' | 'indeterminate' | 'static'
}

val circularProgress: ComponentClass<CircularProgressProps> = importedCircularProgress.default


@JsModule("@material-ui/core/LinearProgress")
private external val importedLinearProgress: dynamic

external interface LinearProgressProps : Props {
  var className: dynamic
  var color: String
  var value: Number
  var valueBuffer: Number
  var variant: String
}

val linearProgress: ComponentClass<LinearProgressProps> = importedLinearProgress.default


@JsModule("@material-ui/core/Select")
private external val importedSelect: dynamic

external interface SelectProps : Props {
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

val muiSelect: ComponentClass<SelectProps> = importedSelect.default

@JsModule("@material-ui/core/FormControl")
private external val importedFormControl: dynamic

external interface FormControlProps : Props {
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

val formControl: ComponentClass<FormControlProps> = importedFormControl.default


@JsModule("@material-ui/core/InputLabel")
private external val importedInputLabel: dynamic

external interface InputLabelProps : Props {
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

val inputLabel: ComponentClass<InputLabelProps> = importedInputLabel.default

@JsModule("@material-ui/core/OutlinedInput")
private external val importedOutlinedInput: dynamic

external interface InputOutlinedInputProps : Props {
  var labelWidth: Int
  var name: String
  var id: String
  var fullWidth: Boolean
}

val outlinedInput: ComponentClass<InputOutlinedInputProps> = importedOutlinedInput.default

@JsModule("@material-ui/core/Paper")
private external val importedPaper: dynamic

external interface PaperProps : Props {
  var classes: dynamic
  var className: dynamic
  var component: dynamic
  var elevation: Int
  var square: Boolean
  var style: dynamic
}

val muiPaper: ComponentClass<PaperProps> = importedPaper.default


@JsModule("@material-ui/core/Card")
private external val importedCard: dynamic

external interface CardProps : PaperProps {
  var raised: Boolean
}

val muiCard: ComponentClass<CardProps> = importedCard.default

@JsModule("@material-ui/core/CardHeader")
private external val importedCardHeader: dynamic

external interface CardHeaderProps : Props {
  var classes: dynamic
  var className: dynamic
  var title: ReactNode?
  var subheader: ReactNode?
  var disableTypography: dynamic
}

// TODO: @mh Make all these vals external
val muiCardHeader: ComponentClass<CardHeaderProps> = importedCardHeader.default

@JsModule("@material-ui/core/CardContent")
private external val importedCardContent: dynamic

external interface CardContentProps : Props {
  var classes: dynamic
  var className: dynamic
}

val muiCardContent: ComponentClass<CardContentProps> = importedCardContent.default


@JsModule("@material-ui/core/CardActions")
private external val importedCardActions: dynamic

external interface CardActionsProps : Props {
  var classes: dynamic
  var className: dynamic
  var children: dynamic
  var disableActionSpacing: Boolean
}

val muiCardActions: ComponentClass<PaperProps> = importedCardActions.default

@JsModule("@material-ui/core/SnackbarContent")
private external val importedSnackbarContent: dynamic

external interface SnackbarContentProps : Props {
  var action: dynamic
  var classes: dynamic
  var className: String
  var message: ReactNode?
}

val snackbarContent: ComponentClass<SnackbarContentProps> = importedSnackbarContent.default

@JsModule("@material-ui/core/Snackbar")
private external val importedSnackbar: dynamic


external interface SnackbarProps : Props {
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

val snackbar: ComponentClass<SnackbarProps> = importedSnackbar.default

@JsModule("@material-ui/core/Slide")
private external val importedSlide: dynamic

external interface SlideProps : Props {
  var children: dynamic
  var direction: String

  @JsName("in")
  var in_: Boolean
  var timeout: Number
}

val slide: ComponentClass<SlideProps> = importedSlide.default

@JsModule("@material-ui/core/Slider")
private external val importedSlider: dynamic

external interface SliderProps : Props {
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

val slider: ComponentClass<SliderProps> = importedSlider.default

@JsModule("@material-ui/core/Accordion")
private external val importedExpansionPanel: dynamic

external interface AccordionProps : Props {
  var CollapseProps: dynamic
  var TransitionProps: dynamic
  var defaultExpanded: Boolean
  var disabled: Boolean
  var expanded: Boolean
  var onChange: (event: dynamic, expanded: Boolean) -> Unit
}

val accordion: ComponentClass<AccordionProps> = importedExpansionPanel.default


@JsModule("@material-ui/core/AccordionSummary")
private external val importedAccordionSummary: dynamic

external interface AccordionSummaryProps : Props {
  var expandIcon: dynamic
  var IconButtonProps: dynamic
}

val accordionSummary: ComponentClass<AccordionSummaryProps> = importedAccordionSummary.default

@JsModule("@material-ui/core/AccordionDetails")
private external val importedAccordionDetails: dynamic

external interface AccordionDetailsProps : Props {
  var CollapseProps: dynamic
  var defaultExpanded: Boolean
  var disabled: Boolean
  var expanded: Boolean
  var className: String
  var onChange: (event: dynamic, expanded: Boolean) -> Unit
}

val accordionDetails: ComponentClass<AccordionDetailsProps> = importedAccordionDetails.default

@JsModule("@material-ui/core/ExpansionPanelActions")
private external val importedExpansionPanelActions: dynamic

external interface ExpansionPanelActionsProps : Props

val expansionPanelActions: ComponentClass<ExpansionPanelActionsProps> = importedExpansionPanelActions.default

@JsModule("@material-ui/core/Grid/Grid")
private external val gridImport: dynamic

// Props copied from: https://gitlab.com/AnimusDesign/KotlinReactMaterialUI
external interface GridProps : Props {
  var className: String?
  var classes: dynamic
  var alignContent: dynamic /* String /* "stretch" */ | String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "space-between" */ | String /* "space-around" */ */ get() = definedExternally; set(value) = definedExternally
  var alignItems: dynamic /* String /* "stretch" */ | String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "baseline" */ */ get() = definedExternally; set(value) = definedExternally
  var component: dynamic /* String | React.ComponentType<Omit<GridProps, dynamic /* String /* "container" */ | String /* "item" */ | String /* "hidden" */ | String /* "classes" */ | String /* "className" */ | String /* "component" */ | String /* "alignContent" */ | String /* "alignItems" */ | String /* "direction" */ | String /* "spacing" */ | String /* "justify" */ | String /* "wrap" */ | String /* "xs" */ | String /* "sm" */ | String /* "md" */ | String /* "lg" */ | String /* "xl" */ */>> */ get() = definedExternally; set(value) = definedExternally
  var container: Boolean? get() = definedExternally; set(value) = definedExternally
  var direction: dynamic /* String /* "row" */ | String /* "row-reverse" */ | String /* "column" */ | String /* "column-reverse" */ */ get() = definedExternally; set(value) = definedExternally
  var item: Boolean? get() = definedExternally; set(value) = definedExternally
  var justifyContent: dynamic /* String /* "center" */ | String /* "flex-start" */ | String /* "flex-end" */ | String /* "space-between" */ | String /* "space-around" */ */ get() = definedExternally; set(value) = definedExternally
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

val grid: ComponentClass<GridProps> = gridImport.default

@JsModule("@material-ui/core/Dialog")
private external val dialogImport: dynamic

external interface DialogProps : ModalProps {
  var className: String
  var onClose: () -> Unit
  var fullWidth: Boolean
  var maxWidth: dynamic
  var classes: dynamic
  var fullScreen: Boolean
}

val muiDialog: ComponentClass<DialogProps> = dialogImport.default


@JsModule("@material-ui/core/DialogTitle")
private external val dialogTitleImport: dynamic

val muiDialogTitle: ComponentClass<Props> = dialogTitleImport.default


@JsModule("@material-ui/core/DialogContent")
private external val dialogContentImport: dynamic

val muiDialogContent: ComponentClass<Props> = dialogContentImport.default

@JsModule("@material-ui/core/DialogContentText")
private external val dialogContentTextImport: dynamic

val muiDialogContentText: ComponentClass<Props> = dialogContentTextImport.default


@JsModule("@material-ui/core/DialogActions")
private external val dialogActionsImport: dynamic

val muiDialogActions: ComponentClass<Props> = dialogActionsImport.default

@JsModule("@material-ui/core/FormLabel")
private external val formLabelImport: dynamic

val muiFormLabel: ComponentClass<Props> = formLabelImport.default


@JsModule("@material-ui/core/Table/Table")
external val TableImport: dynamic

external interface TableProps : Props {
  var className: String?
  var classes: dynamic
  var size: String?
  var padding: String
  var component: Component<Props, State>? get() = definedExternally; set(value) = definedExternally
  var stickyHeader: Boolean?
}


var mTable: ComponentClass<TableProps> = TableImport.default

@JsModule("@material-ui/core/TableBody/TableBody")
external val TableBodyImport: dynamic

external interface TableBodyProps : Props {
  var component: Component<Props, State>? get() = definedExternally; set(value) = definedExternally
}

var mTableBody: ComponentClass<TableBodyProps> = TableBodyImport.default


@JsModule("@material-ui/core/TableHead/TableHead")
external val TableHeadImport: dynamic

external interface TableHeadProps : Props {
  var component: String? get() = definedExternally; set(value) = definedExternally
}

var mTableHead: ComponentClass<TableHeadProps> = TableHeadImport.default


@JsModule("@material-ui/core/TableRow/TableRow")
external val TableRowImport: dynamic


external interface TableRowProps : Props {
  var component: Component<Props, State>? get() = definedExternally; set(value) = definedExternally
  var hover: Boolean? get() = definedExternally; set(value) = definedExternally
  var selected: Boolean? get() = definedExternally; set(value) = definedExternally
}

var mTableRow: ComponentClass<TableRowProps> = TableRowImport.default

@JsModule("@material-ui/core/TableCell/TableCell")
external val TableCellImport: dynamic

external interface TableCellProps : Props {
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
  var onClick: () -> Unit
}

var mTableCell: ComponentClass<TableCellProps> = TableCellImport.default

@JsModule("@material-ui/core/TableFooter/TableFooter")
external val TableFooterImport: dynamic

external interface TableFooterProps : Props {
  var className: String
  var classes: dynamic
}

var mTableFooter: ComponentClass<TableFooterProps> = TableFooterImport.default

@JsModule("@material-ui/core/TablePagination/TablePagination")
external val TablePaginationImport: dynamic

external interface TablePaginationProps : Props {
  var rowsPerPage: Int
  var page: Int
  var onChangePage: (Event, Int) -> Unit
  var count: Int
  var rowsPerPageOptions: Array<Int>
  var onChangeRowsPerPage: (Event) -> Unit
}

var mTablePagination: ComponentClass<TablePaginationProps> = TablePaginationImport.default

@JsModule("@material-ui/core/TableSortLabel")
external val TableSortLabelImport: dynamic

external interface TableSortLabelProps : Props {
  var active: Boolean
  var classes: dynamic
  var direction: String
  var hideSortIcon: Boolean
  var onClick: (dynamic) -> Unit
}

var mTableSortLabel: ComponentClass<TableSortLabelProps> = TableSortLabelImport.default


@JsModule("@material-ui/core/Chip")
external val ChipImport: dynamic

external interface ChipProps : Props {
  var className: String?
  var classes: dynamic
  var avatar: ReactNode?
  var clickable: Boolean? get() = definedExternally; set(value) = definedExternally
  var color: String? get() = definedExternally; set(value) = definedExternally
  var deleteIcon: ReactNode?
  var icon: ReactElement<*>?
  var label: String? get() = definedExternally; set(value) = definedExternally
  var onClick: (dynamic) -> Unit
  var onDelete: ((Event) -> Unit)?
  var tabIndex: dynamic
  var size: String
  var style: dynamic
  var variant: String?
}

var mChip: ComponentClass<ChipProps> = ChipImport.default

@JsModule("@material-ui/core/Avatar")
external val AvatarImport: dynamic
var avatar: ComponentClass<CheckboxProps> = AvatarImport.default

@JsModule("@material-ui/core/Checkbox/Checkbox")
external val CheckboxImport: dynamic

external interface CheckboxProps : Props {
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

var mCheckbox: ComponentClass<CheckboxProps> = CheckboxImport.default


@JsModule("@material-ui/core/Popover")
external val PopoverImport: dynamic

external interface PopoverProps : Props {
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

var popover: ComponentClass<PopoverProps> = PopoverImport.default

@JsModule("@material-ui/core/Tooltip")
external val TooltipImport: dynamic

external interface TooltipProps : Props {
  var title: String
  var open: Boolean?
  var enterDelay: Int
  var leaveDelay: Int
  var placement: String
  var classes: dynamic
}

var muiTooltip: ComponentClass<TooltipProps> = TooltipImport.default


@JsModule("@material-ui/core/Grow")
external val GrowImport: dynamic

external interface GrowProps : Props {
  @JsName("in")
  var show: Boolean
  var timeout: dynamic
  var style: dynamic
  var onExited: (dynamic) -> Unit

}

var grow: ComponentClass<GrowProps> = GrowImport.default