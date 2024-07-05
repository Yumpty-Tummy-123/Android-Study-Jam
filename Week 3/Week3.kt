package com.example.yumpty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.yumpty.ui.theme.Klee
import com.example.yumpty.ui.theme.YumptyTheme
import com.google.accompanist.pager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt
import androidx.compose.material3.ExperimentalMaterial3Api


data class FoodItem(val name: String, val price: Int)

class MainActivity : ComponentActivity() {
    private val fileName = "food.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YumptyTheme {
                val mContext = LocalContext.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //clearFoodItemsFile(this, fileName)
                    val context = LocalContext.current
                    val foodItemsState = remember { mutableStateOf<List<FoodItem>>(emptyList()) }

                    if (!fileExists(context, fileName)) {
                        createFile(context, fileName)
                    }
                    val foodItems = remember { mutableStateOf(readFoodItemsFromFile(this, fileName)) }
                    MainContent(foodItems.value, onAddItem = { newItem ->
                        val updatedItems = foodItems.value + newItem
                        foodItems.value = updatedItems
                        writeFoodItemsToFile(this, updatedItems, fileName)
                    })
                }
            }
        }
    }

    private fun fileExists(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }

    private fun createFile(context: Context, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.createNewFile()
    }
    private fun writeFoodItemsToFile(context: Context, foodItems: List<FoodItem>, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.bufferedWriter().use { writer ->
            foodItems.forEach { item ->
                writer.write("${item.name},${item.price}\n")
            }
        }
    }

    private fun readFoodItemsFromFile(context: Context, fileName: String): List<FoodItem> {
        val file = File(context.filesDir, fileName)
        val foodItems = mutableListOf<FoodItem>()
        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 2) {
                    val name = parts[0]
                    val price = parts[1].toIntOrNull() ?: 0
                    foodItems.add(FoodItem(name, price))
                }
            }
        }
        return foodItems
    }

}
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainContent(foodItems: List<FoodItem>, onAddItem: (FoodItem) -> Unit) {
    val titles = listOf("Food", "Fun")
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = Color(116, 201, 49),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    height = 4.dp,
                    color = Color.Black
                )
            }
        ) {
            titles.forEachIndexed { index, title ->
                androidx.compose.material3.Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }
        HorizontalPager(
            count = titles.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> FoodScreen(foodItems, onAddItem)
                1 -> FunScreen()
            }
        }
    }
}

@Composable
fun FoodScreen(foodItems: List<FoodItem>, onAddItem: (FoodItem) -> Unit) {
    val mContext = LocalContext.current
    var selectedItems by remember { mutableStateOf(setOf<FoodItem>()) }
    val totalPrice by remember { derivedStateOf { selectedItems.sumOf { it.price } } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Total Price: $totalPrice",
            modifier = Modifier.padding(top = 30.dp, bottom = 10.dp, start = 25.dp),
            fontSize = 20.sp,
            color = Color.Black
        )
        RecyclerView(
            names = foodItems,
            selectedItems = selectedItems,
            onItemCheckedChange = { item, isChecked ->
                selectedItems = if (isChecked) {
                    selectedItems + item
                } else {
                    selectedItems - item
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        androidx.compose.material.FloatingActionButton(
            onClick = {
                mContext.startActivity(Intent(mContext, AddItem::class.java))
            },
            backgroundColor = Color(116, 201, 49),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 20.dp
            )
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunScreen() {
    //FunScreen here
}

@Composable
fun RecyclerView(names: List<FoodItem>, selectedItems: Set<FoodItem>, onItemCheckedChange: (FoodItem, Boolean) -> Unit) {
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.Top) {
        items(items = names) { item ->
            ListItem(
                item = item,
                isChecked = selectedItems.contains(item),
                onCheckedChange = { isChecked ->
                    onItemCheckedChange(item, isChecked)
                }
            )
        }
    }
}


@Composable
fun ListItem(item: FoodItem, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = Color.White,
        border = BorderStroke(3.dp, Color(116, 201, 49)),
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                colors = CheckboxDefaults.colors(checkedColor = Color.Black),
                onCheckedChange = onCheckedChange
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 15.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(start = 5.dp)) {
                        Text(
                            text = item.name,
                            color = Color.Black,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = Klee, fontSize = 25.sp
                            )
                        )
                    }
                    Column {
                        Text(
                            modifier = Modifier.padding(end = 10.dp),
                            text = item.price.toString(),
                            color = Color.Black,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold, fontFamily = Klee, fontSize = 27.sp
                            )
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun SimpleComposablePreview() {
    MainContent(listOf(FoodItem("Sample Food", 123)), onAddItem = {})
}
