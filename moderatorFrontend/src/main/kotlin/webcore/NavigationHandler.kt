package webcore

import com.studo.campusqr.common.utils.LocalizedString
import js.core.jso
import webcore.extensions.toRoute
import web.window.window
import mui.icons.material.Edit
import mui.material.ButtonColor
import mui.material.ButtonVariant
import web.html.HTMLAnchorElement
import web.dom.Node
import web.uievents.MouseEvent
import web.url.URL
import react.RefObject
import util.AppRoute
import util.MbUrl
import util.get
import util.relativeUrl
import web.history.POP_STATE
import web.history.PopStateEvent
import web.history.history
import web.location.location
import web.scroll.ScrollBehavior
import web.uievents.CLICK
import web.url.URLSearchParams
import web.window.BEFORE_UNLOAD
import web.window.BeforeUnloadEvent
import web.window.WindowTarget
import webcore.extensions.findParent
import kotlin.js.Date

// The `shouldNavigateAway()` lambda cannot be put in the `navigateAwayListeners` directly
// because the lambda's hashCode changes between add/remove.
// By implementing this interface the component's instance can be used for adding/removing
// from the `navigateAwayListeners` list since this hash stays the same.
interface NavigateAwayObservable {
  fun shouldNavigateAway(): Boolean
}

/**
 * Used for handling the navigation in projects.
 * This also includes functionality to configure an unsaved changes dialog.
 */
object NavigationHandler {
  lateinit var allUrls: List<MbUrl>
    private set

  // Global NavigationHandler dialog. Use only in this object.
  // This dialog can pop up above all other dialogs.
  lateinit var dialogRef: RefObject<MbDialog>
    private set

  // Necessary to handle the different causing events in `shouldNavigate()`
  enum class NavigationEvent {
    // When the active history entry changes (e.g. browser back/forward button triggers the popstate event)
    POPSTATE,

    // When a link is clicked (e.g. clicking link in the side drawer like "All organizations")
    LINK_CLICK,

    // When a new route is pushed via `appContext.routeContext.pushRoute()` (e.g. Route change after creating a new organization)
    PUSH_APP_ROUTE,
  }

  // We have an unsaved changes dialog with stay on page functionality, so we need to handle the browser history stack manually.
  // This is required because the `popstate` event always changes the URL which cannot be prevented.
  // Otherwise, the actual displayed view would be different from the URL when clicking "Stay on page" in the unsaved changes dialog.

  // Procedure: Add/Remove a listener (usually in componentDidMount & componentWillUnmount) for a view.
  // This makes it possible to determine whether to show the unsaved changes dialog when trying to navigate away from a view.
  val navigateAwayListeners: MutableList<NavigateAwayObservable> = mutableListOf()

  private var currentHistoryState: Double? = null // Timestamp of the last visited page (NOT highest timestamp, consider e.g. browser back)

  private val historyStack: MutableList<Double> = mutableListOf() // History states to determine multiple steps backward/forward in history

  /**
   * [allUrls]: Pass all urls, needed for `.toRoute()`.
   */
  fun initApp(allUrls: List<MbUrl>, dialogRef: RefObject<MbDialog>, handleHistoryChange: (newRoute: AppRoute?) -> Unit) {
    val duplicatePaths = allUrls.groupBy { it.path }.filter { it.value.count() > 1 }.keys
    if (duplicatePaths.isNotEmpty()) {
      throw IllegalStateException(
        "Duplicate path at ${duplicatePaths.first()} is not allowed by design. " +
            "We need a 1:1 mapping of AppRoutes and paths"
      )
    }
    // Init urls
    this.allUrls = allUrls

    // Init dialog
    this.dialogRef = dialogRef

    // Init history
    val timestamp = (history.state as? Double) ?: Date().getTime() // history.state can already exist when reloading a page
    if (history.state == null) {
      // Set history state for navigation handling
      history.replaceState(data = timestamp, unused = "")
    }
    currentHistoryState = timestamp // Initialize
    historyStack.add(timestamp)

    // Enable client side routing
    enableClientSideRouting(handleHistoryChange)
  }

  private fun enableClientSideRouting(handleHistoryChange: (newRoute: AppRoute?) -> Unit) {
    window.addEventListener(BeforeUnloadEvent.BEFORE_UNLOAD, { event ->
      if (navigateAwayListeners.any { !it.shouldNavigateAway() }) {
        // Confirmation dialog is only shown when preventDefault is called
        // https://developer.mozilla.org/en-US/docs/Web/API/Window/beforeunload_event
        event.preventDefault() // Prevent unloading
        event.asDynamic().returnValue = "" // Chrome requires returnValue to be set, otherwise no dialog is shown
      }
    })

    window.addEventListener(PopStateEvent.POP_STATE, {
      if (currentHistoryState == (history.state as Double?)) {
        // This popstate event is triggered by history.go() in the "stay on page" button callback.
        // We do not want to do anything in this case. Everything history related is already handled in the shouldNavigate() callbacks.
        return@addEventListener
      }

      if (shouldNavigate(
          newRoute = location.toRoute() ?: throw Exception("Could not parse route from URL: ${location.href}"),
          navigationEvent = NavigationEvent.POPSTATE,
          handleHistoryChange = handleHistoryChange,
        )
      ) {
        if (history.state == null) {
          // Log for debugging
          console.error(
            "history.state is null in popstate. " +
                "This should not happen! historyStack: ${historyStack.joinToString()}, currentHistoryState: $currentHistoryState"
          )
        } else {
          // Update currentHistoryState to the history state of the page we are navigating to.
          currentHistoryState = history.state as Double
        }
        // Updating the currentAppRoute shows the new page, so only do it when we are sure we want to leave the page.
        handleHistoryChange(location.toRoute())
      }
    })

    window.addEventListener(MouseEvent.CLICK, { event ->
      val target = event.target
      if (target != null && !event.altKey && !event.ctrlKey && !event.metaKey && !event.shiftKey) {
        // Only handle click for anchor elements
        val linkNode = (target as Node).findParent { it.nodeName.lowercase() == "a" } ?: return@addEventListener
        val anchor = linkNode as HTMLAnchorElement
        val parsedUrl = try {
          URL(anchor.href)
        } catch (_: Throwable) {
          null
        }
        val relativeUrl = parsedUrl?.relativeUrl
        val activeView = parsedUrl?.toRoute()
        if (relativeUrl != null && anchor.target != WindowTarget._blank && activeView != null) {
          if (shouldNavigate(
              newRoute = parsedUrl.toRoute()!!,
              navigationEvent = NavigationEvent.LINK_CLICK,
              handleHistoryChange = handleHistoryChange,
            )
          ) {
            pushHistory(relativeUrl = relativeUrl, title = anchor.title, handleHistoryChange = handleHistoryChange)
            resetScrollPosition()
          }
          event.preventDefault()
        }
      }
    })
  }

  fun resetScrollPosition() {
    // Reset scroll position.
    // Otherwise, scroll position from previous route is kept sometimes.
    // We're not 100% sure when it is / isn't reset.
    window.scrollTo(
      options = jso {
        top = 0.0
        left = 0.0
        behavior = ScrollBehavior.instant
      }
    )
  }

  fun pushHistory(relativeUrl: String, title: String, handleHistoryChange: (newRoute: AppRoute?) -> Unit) {
    // Only handle navigation when the URL changes
    if (relativeUrl != location.relativeUrl) {
      while (historyStack.isNotEmpty() && currentHistoryState != historyStack.last()) {
        // Remove history entries that are after the current page, since they are no longer part of the history.
        // This is the case when navigating from a history entry that is not the last one (= `history.forward()` works) to a new page
        historyStack.removeLast()
      }
      currentHistoryState = Date().getTime()
      historyStack.add(currentHistoryState!!)
      history.pushState(data = currentHistoryState, unused = title, url = relativeUrl)
      handleHistoryChange(location.toRoute())
    }
  }

  fun pushHistoryHash(hash: String, title: String) {
    // No handleHistoryChange required because currentAppRoute does not change
    pushHistory(relativeUrl = "#$hash", title = title, handleHistoryChange = {})
  }

  fun replaceHistory(relativeUrl: String, title: String) {
    history.replaceState(data = currentHistoryState, unused = title, url = relativeUrl)
  }

  fun setSearchParams(searchParams: URLSearchParams) {
    if (searchParams.toString().isEmpty()) {
      // No search params set
      replaceHistory(relativeUrl = location.pathname + location.hash, title = "")
    } else {
      replaceHistory(relativeUrl = "?$searchParams${location.hash}", title = "")
    }
  }

  fun shouldNavigate(
    newRoute: AppRoute, navigationEvent: NavigationEvent,
    handleHistoryChange: (newRoute: AppRoute?) -> Unit,
  ): Boolean {
    return if (navigateAwayListeners.any { !it.shouldNavigateAway() }) {
      dialogRef.current!!.showDialog(
        DialogConfig(
          title = DialogConfig.Title(
            text = unsaved_changes_title.get(),
            icon = Edit,
          ),
          text = unsaved_changes_text.get(),
          buttons = listOf(
            DialogButton(stay_on_page.get(), variant = ButtonVariant.text, onClick = {
              when (navigationEvent) {
                NavigationEvent.PUSH_APP_ROUTE, NavigationEvent.LINK_CLICK -> {
                  // No need to do anything here. shouldNavigate() returns false so the view does not change.
                }

                NavigationEvent.POPSTATE -> {
                  // currentHistoryState already points to the page we are staying on, so no need to update it here.
                  val currentHistoryState = history.state as Double
                  history.go(historyStack.indexOf(this.currentHistoryState!!) - historyStack.indexOf(currentHistoryState))
                }
              }
            }),
            DialogButton(leave_page.get(), color = ButtonColor.primary, variant = ButtonVariant.contained, onClick = {
              // Proceed with page change
              when (navigationEvent) {
                NavigationEvent.PUSH_APP_ROUTE, NavigationEvent.LINK_CLICK -> {
                  pushHistory(relativeUrl = newRoute.relativeUrl, title = newRoute.url.title.get(), handleHistoryChange)
                }

                NavigationEvent.POPSTATE -> {
                  // Popstate already changed to the correct URL & updated currentHistoryState when processing the history navigation.
                  // Only need to update currentHistoryState and currentAppRoute (done in handleHistoryChange()).

                  // Update currentHistoryState to the history state of the page we are navigating to.
                  currentHistoryState = history.state as Double
                  handleHistoryChange(newRoute) // Address bar already has new URL, now really render the new page.
                }
              }
              // Corresponding navigateAwayListener is removed "automatically"
              // because unregisterNavigateAwayListener() is called in componentWillUnmount() of the view that is now being left.
            }),
          ),
        )
      )
      false // Prevent navigation here at first. Navigation is handled when the user interacts with the dialog.
    } else {
      true
    }
  }

  val unsaved_changes_title = LocalizedString(
    en = "Unsaved changes",
    de = "Ungespeicherte Änderungen",
  )

  val unsaved_changes_text = LocalizedString(
    en = "There are unsaved changes. Leave this page without saving?",
    de = "Es gibt ungespeicherte Änderungen. Diese Seite verlassen, ohne zu speichern?",
  )

  val stay_on_page = LocalizedString(
    en = "Stay on page",
    de = "Auf Seite bleiben",
  )

  val leave_page = LocalizedString(
    "Leave page",
    "Seite verlassen"
  )

}