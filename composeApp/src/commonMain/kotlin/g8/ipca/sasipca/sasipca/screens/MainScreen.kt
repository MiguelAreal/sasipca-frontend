package g8.ipca.sasipca.sasipca.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import g8.ipca.sasipca.sasipca.storage.*

/** Enum para gerir separadores da BottomNavigationBar */
sealed class BottomNavScreen(val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("Home", Icons.Default.Home)
    object Stock : BottomNavScreen("Stock", Icons.Default.Description)
    object Calendario : BottomNavScreen("Calendário", Icons.Default.CalendarMonth)
    object Configuracoes : BottomNavScreen("Configurações", Icons.Default.Settings)
    object Perfil : BottomNavScreen("Perfil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.Stock,
    BottomNavScreen.Calendario,
    BottomNavScreen.Configuracoes,
    BottomNavScreen.Perfil
)

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Home) }
    var previousScreen by remember { mutableStateOf(currentScreen) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentScreen) { selected ->
                previousScreen = currentScreen
                currentScreen = selected
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val initialIndex = bottomNavItems.indexOf(previousScreen)
                    val targetIndex = bottomNavItems.indexOf(currentScreen)

                    if (targetIndex > initialIndex) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) with
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) with
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                    }
                }
            ) { screen ->
                when (screen) {
                    is BottomNavScreen.Home -> HomeScreen()
                    is BottomNavScreen.Stock -> StockScreen()
                    is BottomNavScreen.Calendario -> ReceptionScreen()
                    else -> PlaceholderScreen(screen.label)
                }
            }
        }
    }
}

/** Placeholder para separadores ainda não implementados */
@Composable
fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "$label ainda não implementado", color = Color.Gray)
    }
}

/** BottomNavigationBar adaptada para navegar entre os separadores */
@Composable
fun BottomNavigationBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val navItems = listOf(
            BottomNavScreen.Home,
            BottomNavScreen.Stock,
            BottomNavScreen.Calendario,
            BottomNavScreen.Configuracoes,
            BottomNavScreen.Perfil
        )

        navItems.forEach { screen ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3D4A7A),
                    unselectedIconColor = Color(0xFF999999),
                    indicatorColor = Color(0xFFE8EAF6)
                )
            )
        }
    }
}
