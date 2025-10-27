package g8.ipca.sasipca.sasipca

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CalendarMonth




@Composable
fun DashboardScreen() {
    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(paddingValues)
        ) {
            HeaderSection()
            StatsSection()
            Spacer(modifier = Modifier.height(24.dp))
            MenuSection()
        }
    }
}

@Composable
fun HeaderSection() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF3D4A7A),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(vertical = 24.dp, horizontal = 24.dp)
    ) {
        val isCompact = maxWidth < 600.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Boa tarde, Utilizador",
                    color = Color.White,
                    fontSize = if (isCompact) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quinta-Feira, 2 Out. 2025",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = if (isCompact) 12.sp else 13.sp
                )
            }

            IconButton(
                onClick = { /* Handle notification */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun StatsSection() {
    BoxWithConstraints {
        val horizontalPadding = if (maxWidth < 600.dp) 20.dp else 40.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Entregas", "08", Modifier.weight(1f))
            StatCard("Arrumação Volume", "15", Modifier.weight(1f))
            StatCard("Pendentes", "03", Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color(0xFF666666),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color(0xFF3D4A7A),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MenuSection() {
    BoxWithConstraints {
        val horizontalPadding = if (maxWidth < 600.dp) 20.dp else 40.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuItem(Icons.Default.Inventory, "Inventário")
            MenuItem(Icons.Default.CalendarToday, "Calendário")
            MenuItem(Icons.Default.Campaign, "Campanhas")
            MenuItem(Icons.Default.People, "Estudantes")
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { /* TODO */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF3D4A7A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar() {
    BoxWithConstraints {
        val iconSize = if (maxWidth < 360.dp) 20.dp else 24.dp

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val navItems = listOf(
                Icons.Default.Home to "Home",
                Icons.Default.Description to "Documentos",
                Icons.Default.CalendarMonth to "Calendário",
                Icons.Default.Settings to "Configurações",
                Icons.Default.Person to "Perfil"
            )

            navItems.forEachIndexed { index, (icon, desc) ->
                NavigationBarItem(
                    selected = index == 0,
                    onClick = { /* TODO */ },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF3D4A7A),
                        unselectedIconColor = Color(0xFF999999),
                        indicatorColor = Color(0xFFE8EAF6)
                    )
                )
            }
        }
    }
}
