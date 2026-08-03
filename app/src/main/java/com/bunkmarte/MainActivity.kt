package com.bunkmarte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bunkmarte.ui.navigation.BunkMarteNavGraph
import com.bunkmarte.ui.theme.BunkMarteTheme

/**
 * Single activity for BunkMarte.
 * Sets up edge-to-edge display and Compose content with the nav graph.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BunkMarteTheme {
                BunkMarteNavGraph()
            }
        }
    }
}
