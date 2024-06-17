class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            New1Theme {
                Main()
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Main() {
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
                0 -> FoodScreen()
                1 -> FunScreen()
            }
        }
    }
}

@Composable
fun FunScreen() {
    Text(text = "Food Screen")
}

@Composable
fun FoodScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        androidx.compose.material.FloatingActionButton(
            onClick = {
                //to be implemented
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



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    New1Theme {
        Main
    }
}