package id.my.matahati.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.my.matahati.pos.ui.screen.home.HomeScreen
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileMatahati_POSTheme {
                HomeScreen()
            }
        }
    }
}
