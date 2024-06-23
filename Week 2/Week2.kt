class AddItem : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YumptyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AddFoodScreen(onSave = { food ->

                    })
                }
            }
        }
    }
}
data class Food(
    val name: String,
    val canteen: String,
    val price: String,
    val type: FoodType,
    val cuisine: String
)

fun writeFoodItemToFile(context: Context, food: Food, fileName: String) {
    val file = File(context.filesDir, fileName)
    val foodItemString = "${food.name},${food.price.toString()}\n"
    FileOutputStream(file, true).bufferedWriter().use { writer ->
        writer.write(foodItemString)
    }
}

enum class FoodType {
    BEVERAGE,
    DESSERT,
    MAIN_COURSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onSave: (Food) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var canteen by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FoodType.MAIN_COURSE) }
    var cuisine by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Add Food Item")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            val outlineColor = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color(116, 201, 49)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                colors = outlineColor
            )
            OutlinedTextField(
                value = canteen,
                onValueChange = { canteen = it },
                label = { Text("Canteen") },
                colors = outlineColor
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = outlineColor
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = outlineColor
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    FoodType.values().forEach { selectedType ->
                        DropdownMenuItem(
                            text = { Text(selectedType.name) },
                            onClick = {
                                type = selectedType
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = cuisine,
                onValueChange = { cuisine = it },
                label = { Text("Cuisine")},
                colors = outlineColor
            )
            Button(
                onClick = {
                    if (name.isNotBlank() && canteen.isNotBlank() && price.isNotBlank() && cuisine.isNotBlank()) {
                        val foodItem = Food(
                            name = name,
                            canteen = canteen,
                            price = price,
                            type = type,
                            cuisine = cuisine
                        )
                        writeFoodItemToFile(context, foodItem, "food.txt")
                        onSave(foodItem)
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(116, 201, 49),
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        }
    }
}

@Preview( showBackground = true)
@Composable
fun preview(){
    AddFoodScreen(onSave = {food->})
}
