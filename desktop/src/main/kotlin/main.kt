import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.application
import com.arnyminerz.filamagenta.desktop.localization.Translations
import com.arnyminerz.filamagenta.desktop.ui.window.LoginWindow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
fun main() {
    Translations.load("en")
    Translations.setFallback(Locale.ENGLISH)

    application {
        // MainWindow(::exitApplication)
        LoginWindow(::exitApplication)
    }
}
