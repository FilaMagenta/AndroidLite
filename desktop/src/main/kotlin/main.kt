import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.resources.LocalCredentialsProvider
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.desktop.localization.Translations
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.LANGUAGE
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.window.LoginWindow
import com.arnyminerz.filamagenta.desktop.ui.window.MainWindow
import io.sentry.Sentry
import java.util.Locale

private const val TAG = "Main"

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
)
fun main() {
    Translations.load("en", "ca", "es")
    LocalPropertiesStorage[LANGUAGE]?.let {
        Translations.setFallback(it)
    } ?: Translations.setFallback(Locale.ENGLISH)

    Logger.i(TAG, "Initializing Sentry...")
    Sentry.init { options ->
        options.dsn = LocalCredentialsProvider["SENTRY_DSN"]
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
        // When first trying Sentry it's good to see what the SDK is doing:
        options.isDebug = true
    }

    application(exitProcessOnExit = false) {
        val userToken by LocalPropertiesStorage.getLive(USER_TOKEN).collectAsState(null)

        if (userToken == null)
            LoginWindow(::exitApplication) { LocalPropertiesStorage.setMemory(USER_TOKEN, it) }
        else
            MainWindow(::exitApplication) {
                LocalPropertiesStorage.clear(USER_TOKEN)
                Logger.i(TAG, "Restating application...")
                exitApplication()
                doAsync { main() }
            }
    }
}
