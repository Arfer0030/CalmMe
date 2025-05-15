package com.example.calmme.pages.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.calmme.R
import com.example.calmme.commons.Routes
import com.example.calmme.data.CategoryData
import com.example.calmme.data.categoryList
import com.example.calmme.data.moods

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    0.0f to Color(0xffe4dfc6),
                    0.3f to Color(0xffd3c6de),
                    0.5f to Color(0xffCEBFE6),
                    0.7f to Color(0xffc8ceec),
                    1.0f to Color(0xffc2ddf2),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 800f),
                )
            )
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSection(navController) }
            item { ForYouSection() }
        }
    }
}

@Composable
fun HeaderSection(navController: NavController) {
        Column {
            Spacer(
                modifier = Modifier.padding(vertical = 14.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding( horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row (
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    Image(
                        painter = painterResource(id=R.drawable.profile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(72.dp)
                            .clickable { navController.navigate(Routes.Authentication.route)}
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text("Good Morning,", fontSize = 12.sp)
                        Text("Alvin!", fontSize = 12.sp,fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = {},
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notif),
                        contentDescription = "Notification",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            MoodSection()
        }
    }


@Composable
fun MoodSection() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("how are you today?", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(moods) { mood ->
                MoodItem(name = mood.first, icon = mood.second)
            }
        }
    }
}

@Composable
fun MoodItem(name: String, icon: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(80.dp)
            .padding(vertical = 8.dp)
            .clickable {  }
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = name,
            modifier = Modifier.size(80.dp)
        )
        Text(name, fontSize = 14.sp, color = Color(0xff933C9F))
    }
}

@Composable
fun ForYouSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "For You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "See more",
                fontSize = 14.sp,
                color = Color(0xFF933C9F),
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            0.0f to Color(0xFFdaf0d2),
                            0.4f to Color(0xFFc0e2f4),
                            0.5f to Color(0xFFc0e2f4),
                            0.7f to Color(0xFFc0e2f4),
                            1.0f to Color(0xFFcec2e8),
                            start = Offset(0f, 600f),
                            end = Offset(1000f, 100f),
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "GO PREMIUM 👑",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Upgrade to premium\nget more profit\nnow!",
                            fontSize = 20.sp,
                            color = Color(0xff863D3D),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow),
                            contentDescription = "Notification",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.foryou_1),
                            contentDescription = "Premium",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.foryou_2),
                            contentDescription = "Premium",
                            modifier = Modifier.size(67.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CategorySection()
    }
}



@Composable
fun CategorySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "See all",
                fontSize = 14.sp,
                color = Color(0xFF933C9F),
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid View
        LazyVerticalStaggeredGrid (
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.height(550.dp),
            contentPadding = PaddingValues(4.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(categoryList) { index, category ->
                // Cek apakah index genap atau ganjil untuk menentukan ukuran
                if (index % 2 == 0 ) {
                    CategoryItem(category = category, isLarge = true)
                } else {
                    CategoryItem(category = category, isLarge = false)
                }
            }
        }
    }
}


@Composable
fun CategoryItem(category: CategoryData, isLarge: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLarge) 180.dp else 150.dp) // Ukuran berbeda
            .clickable {  }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            category.color1,
                            category.color2
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Image(
                    painter = painterResource(id = category.icon),
                    contentDescription = category.name,
                    modifier = Modifier.size(75.dp).align(Alignment.End)
                )
            }
        }
    }
}
