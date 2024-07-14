//MainActivity
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
//    private fun clearFoodItemsFile(context: Context, fileName: String) {
//        val file = File(context.filesDir, fileName)
//            file.bufferedWriter().use { writer ->
//                writer.write("")
//         }
//    }
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
    var sliderPosition by remember { mutableFloatStateOf(3F) }
    var switchClick by remember { mutableFloatStateOf(0F) }
    var color_choose by remember { mutableStateOf(Color.White) }
    var targetAngle by remember { mutableStateOf(0f) }
    var cod by remember { mutableIntStateOf(1) }


    fun randomAngle(): Float {
        return (0..360).random().toFloat()
    }

    @Composable
    fun color_bg(int: Int) {
        when (int) {
            1 -> color_choose = colorResource(R.color.blue)
            2 -> color_choose = colorResource(R.color.red)
            3 -> color_choose = colorResource(R.color.green)
            4 -> color_choose = colorResource(R.color.orange)
            5 -> color_choose = colorResource(R.color.purle)
            6 -> color_choose = colorResource(R.color.yellow)
            7 -> color_choose = colorResource(R.color.pink)
            8 -> color_choose = colorResource(R.color.gold)

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color_choose),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(50.dp)
                .fillMaxSize()
                .background(color_choose),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 15.dp)
                    .align(Alignment.CenterHorizontally),
                text = "EACH PERSON SHOULD \nPICK A COLOUR",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Box(modifier = Modifier.padding(0.dp)){
                if (switchClick == 0F){
                    when (sliderPosition) {
                        3F ->{
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_3),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }

                        4F -> {
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_4),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }

                        5F -> {
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_5),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }

                        6F -> {
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_6),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }

                        7F -> {
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_7),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }

                        8F -> {
                            Box{
                                Image(
                                    painter = painterResource(R.drawable.wheel_8),
                                    contentDescription = null
                                )
                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier.align(Alignment.Center)){
                                    Image(
                                        painter = painterResource(id = R.drawable.point),
                                        modifier = Modifier.align(Alignment.Center),
                                        contentDescription = null
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (switchClick>0f) {
//                    @Composable
//                    fun rotationAnimation() {
//                        val infiniteTransition = rememberInfiniteTransition()
//                        val angle by infiniteTransition.animateFloat(
//                            initialValue = 0f,
//                            targetValue = 360f,
//                            animationSpec = (InfiniteRepeatableSpec(
//                                    tween(800, easing = LinearEasing))))
                when (sliderPosition) {
                    3F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_3),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(130F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(230F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(40f),
                                    contentDescription = null,
                                )
                        }
                    }

                    4F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_4),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(130F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(230F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(280f),
                                    contentDescription = null,
                                )
                            if (cod == 4)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(20f),
                                    contentDescription = null,
                                )
                        }
                    }

                    5F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_5),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(130F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(210F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(280f),
                                    contentDescription = null,
                                )
                            if (cod == 4)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(350f),
                                    contentDescription = null,
                                )
                            if (cod==5)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(40f),
                                    contentDescription = null,
                                )
                        }
                    }

                    6F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_6),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(130F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(190F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(250f),
                                    contentDescription = null,
                                )
                            if (cod == 4)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(310f),
                                    contentDescription = null,
                                )
                            if (cod==5)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(10f),
                                    contentDescription = null,
                                )
                            if (cod==6)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(60f),
                                    contentDescription = null,
                                )
                        }
                    }


                    7F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_7),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(130F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(175F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(200f),
                                    contentDescription = null,
                                )
                            if (cod == 4)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(270f),
                                    contentDescription = null,
                                )
                            if (cod==5)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(330f),
                                    contentDescription = null,
                                )
                            if (cod==6)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(20f),
                                    contentDescription = null,
                                )
                            if (cod==7)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(60f),
                                    contentDescription = null,
                                )
                        }
                    }

                    8F -> Box{
                        Image(
                            painter = painterResource(R.drawable.wheel_8),
                            contentDescription = null
                        )
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            if (cod == 1)  // 1 -> Red colour  cod -> random number generated
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(115F),
                                    contentDescription = null,
                                )
                            if (cod == 2)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(165F),
                                    contentDescription = null,
                                )
                            if (cod == 3)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(200f),
                                    contentDescription = null,
                                )
                            if (cod == 4)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(250f),
                                    contentDescription = null,
                                )
                            if (cod==5)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(280f),
                                    contentDescription = null,
                                )
                            if (cod==6)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(345f),
                                    contentDescription = null,
                                )
                            if (cod==7)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(20f),
                                    contentDescription = null,
                                )
                            if (cod==8)
                                Image(
                                    painter = painterResource(id = R.drawable.point),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(60f),
                                    contentDescription = null,
                                )
                        }
                    }

                }
//                        LaunchedEffect(targetAngle) {
//                            if (angle == targetAngle) {
//                                switchClick = 0F // Reset switchClick to stop animation
//                                color_choose = Color.White // Reset color for next spin
//                                sliderPosition=0f
//                            }
//                        }

//                    rotationAnimation()
            }
            Spacer(modifier = Modifier.padding(13.dp))
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it.roundToInt().toFloat()
                    switchClick = 0F
                    color_choose = Color.White
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.LightGray,
                    activeTrackColor = colorResource(R.color.darkslider),
                    inactiveTrackColor = colorResource(R.color.blueslider)
                ),
                thumb = {
                    Image(painterResource(id = R.drawable.pin),"contentDescription")
                },
                steps = 4,
                valueRange = 3F..8F,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(13.dp))
            Text(text = sliderPosition.toInt().toString() + " Persons", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(modifier = Modifier.padding(13.dp))

            Button(
                onClick = {
                    targetAngle = randomAngle()
                    switchClick++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black) )
            {
                Text(text = "SPIN", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.padding(13.dp))
            if (switchClick > 0) {
                cod=PayBill(numberOfPeople = sliderPosition.toInt(), reset = false)
                color_bg(int = cod)
            }

        }

    }

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
@Composable
fun PayBill(numberOfPeople: Int, reset: Boolean = false) : Int {
    var chosenNumber by remember { mutableIntStateOf((1..numberOfPeople).random()) }
    if (reset) {
        chosenNumber = (1..numberOfPeople).random()
    }
    Column {
        colourChoose(chosenNumber)
    }
    return chosenNumber
}

@Composable
fun colourChoose(num: Int) {
    when (num) {
        1 -> Text(text = "Blue must pay the Bill", color = Color.White, fontWeight = FontWeight.Bold , fontSize = 20.sp)
        2 -> Text(text = "Red must pay the Bill", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        3 -> Text(text = "Green must pay the Bill", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        4 -> Text(text = "Orange must pay the Bill", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        5 -> Text(text = "Purple must pay the Bill", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        6 -> Text(text = "Yellow  must pay the Bill", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        7 -> Text(text = "Pink must pay the Bill", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        8 -> Text(text = "Gold must pay the Bill", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}


//        1 -> Blue
//        2 -> Red
//        3 -> Green
//        4 -> Orange
//        5 -> Purple
//        6 -> Yellow
//        7 -> Pink
//        8 -> Gold
