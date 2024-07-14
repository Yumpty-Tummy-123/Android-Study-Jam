//AddItem
package com.example.yumpty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.yumpty.ui.theme.YumptyTheme
import java.io.File
import java.io.FileOutputStream
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
