import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.application
import com.arnyminerz.filamagenta.desktop.localization.Translations
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.window.LoginWindow
import com.arnyminerz.filamagenta.desktop.ui.window.MainWindow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun main() {
    Translations.load("en")
    Translations.setFallback(Locale.ENGLISH)

    application {
        val userToken by LocalPropertiesStorage.getLive(USER_TOKEN).collectAsState(null)

        if (userToken == null)
            LoginWindow(::exitApplication) { LocalPropertiesStorage.setMemory(USER_TOKEN, it) }
        else
            MainWindow(::exitApplication) { LocalPropertiesStorage.clear(USER_TOKEN) }
    }
}
