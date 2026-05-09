package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.UserProfile
import org.example.dementia_tester_app.data.UserProfileService
import org.example.dementia_tester_app.ui.components.*
import org.example.dementia_tester_app.utils.*

@Composable
fun Profile(onBack: () -> Unit = {}) {
    val userProfileService = remember { UserProfileService() }

    var isEditMode by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var emergencyEmailError by remember { mutableStateOf(false) }
    var emergencyPhoneNumberError by remember { mutableStateOf(false) }

    var userProfile by remember { mutableStateOf(UserProfile()) }
    var originalProfile by remember { mutableStateOf(UserProfile()) }

    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var suburb by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyEmail by remember { mutableStateOf("") }
    var emergencyRelation by remember { mutableStateOf("") }
    var emergencyPhoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(userProfile) {
        name = userProfile.name
        dateOfBirth = userProfile.dateOfBirth
        email = userProfile.email
        phoneNumber = userProfile.phoneNumber
        address = userProfile.address
        suburb = userProfile.suburb
        state = userProfile.state
        postcode = userProfile.postcode
        country = userProfile.country
        gender = userProfile.gender
        emergencyName = userProfile.emergencyName
        emergencyEmail = userProfile.emergencyEmail
        emergencyRelation = userProfile.emergencyRelation
        emergencyPhoneNumber = userProfile.emergencyPhoneNumber
    }

    val scrollState = rememberScrollState()

    val hasProfileChanges by derivedStateOf {
        name != originalProfile.name ||
                dateOfBirth != originalProfile.dateOfBirth ||
                email != originalProfile.email ||
                phoneNumber != originalProfile.phoneNumber ||
                address != originalProfile.address ||
                suburb != originalProfile.suburb ||
                state != originalProfile.state ||
                postcode != originalProfile.postcode ||
                country != originalProfile.country ||
                gender != originalProfile.gender ||
                emergencyName != originalProfile.emergencyName ||
                emergencyEmail != originalProfile.emergencyEmail ||
                emergencyRelation != originalProfile.emergencyRelation ||
                emergencyPhoneNumber != originalProfile.emergencyPhoneNumber
    }

    fun revertChanges() {
        name = originalProfile.name
        dateOfBirth = originalProfile.dateOfBirth
        email = originalProfile.email
        phoneNumber = originalProfile.phoneNumber
        address = originalProfile.address
        suburb = originalProfile.suburb
        state = originalProfile.state
        postcode = originalProfile.postcode
        country = originalProfile.country
        gender = originalProfile.gender
        emergencyName = originalProfile.emergencyName
        emergencyEmail = originalProfile.emergencyEmail
        emergencyRelation = originalProfile.emergencyRelation
        emergencyPhoneNumber = originalProfile.emergencyPhoneNumber
    }

    fun handleSave() {
        emailError = false
        phoneNumberError = false
        emergencyEmailError = false
        emergencyPhoneNumberError = false

        if (dateOfBirth.isNotEmpty() && !dateOfBirth.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{4}$"))) {
            errorMessage = "Date of birth must be in D/M/YYYY or DD/MM/YYYY format"
            return
        }

        val calculatedAge = calculateAgeFromDateOfBirth(dateOfBirth)
        if (dateOfBirth.isNotEmpty() && (calculatedAge == null || calculatedAge <= 0)) {
            errorMessage = "Age cannot be 0. Please select a valid date of birth."
            return
        }

        if (!email.isValidEmail()) {
            emailError = true
            errorMessage = "Please enter a valid email address"
            return
        }

        if (!phoneNumber.isValidPhoneNumber()) {
            phoneNumberError = true
            errorMessage = "Phone number should contain only digits"
            return
        }

        if (emergencyEmail.isNotEmpty() && !emergencyEmail.isValidEmail()) {
            emergencyEmailError = true
            errorMessage = "Please enter a valid emergency contact email address"
            return
        }

        if (!emergencyPhoneNumber.isValidPhoneNumber()) {
            emergencyPhoneNumberError = true
            errorMessage = "Emergency contact phone number should contain only digits"
            return
        }

        val updatedProfile = UserProfile(
            name = name,
            dateOfBirth = dateOfBirth,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            suburb = suburb,
            state = state,
            postcode = postcode,
            country = country,
            gender = gender,
            emergencyName = emergencyName,
            emergencyEmail = emergencyEmail,
            emergencyRelation = emergencyRelation,
            emergencyPhoneNumber = emergencyPhoneNumber,
            profileImageUrl = userProfile.profileImageUrl
        )

        isLoading = true

        userProfileService.updateUserProfile(updatedProfile) { result ->
            isLoading = false
            when (result) {
                is DatabaseResult.Success -> {
                    userProfile = updatedProfile
                    originalProfile = updatedProfile
                    isEditMode = false
                    showSuccessMessage = true
                    userProfileService.getCurrentUserProfile { result ->
                        if (result is DatabaseResult.Success) {
                            userProfile = result.data
                        }
                    }
                }

                is DatabaseResult.Error -> {
                    errorMessage = "Failed to save profile: ${result.message}"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        userProfileService.getCurrentUserProfile { result ->
            isLoading = false
            when (result) {
                is DatabaseResult.Success -> {
                    userProfile = result.data
                    originalProfile = result.data
                    errorMessage = null
                }

                is DatabaseResult.Error -> {
                    errorMessage = "Failed to load profile: ${result.message}"
                }
            }
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(5000)
            showSuccessMessage = false
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(5000)
            errorMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingSpinner()
        }

        if (showExitConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmationDialog = false },
                title = { Text("Discard Changes?") },
                text = { Text("You have unsaved changes. Are you sure you want to exit? Your changes will be lost.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirmationDialog = false
                            revertChanges()
                            isEditMode = false
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitConfirmationDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .padding(bottom = 80.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfile.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }
            }

            Text(
                text = "Your Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (isEditMode) {
                FormTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    isError = false
                )
            } else {
                ProfileField(label = "Name", value = name)
            }

            if (isEditMode) {
                DateField(
                    date = dateOfBirth,
                    onDateChange = { dateOfBirth = it },
                    label = "Date of Birth",
                    isError = false,
                    isEditable = true,
                    displayValue = dateOfBirth,
                    allowDatesAfterToday = false
                )
            } else {
                ProfileField(
                    label = "Date of Birth",
                    value = dateOfBirth
                )
            }

            if (isEditMode) {
                FormTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    label = "Email",
                    isError = emailError,
                    keyboardType = KeyboardType.Email
                )
            } else {
                ProfileField(label = "Email", value = email)
            }

            if (isEditMode) {
                FormTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneNumberError = false
                    },
                    label = "Phone Number",
                    isError = phoneNumberError,
                    keyboardType = KeyboardType.Phone
                )
            } else {
                ProfileField(label = "Phone Number", value = phoneNumber)
            }

            if (isEditMode) {
                Text(
                    text = "Home Address",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )

                FormTextField(value = address, onValueChange = { address = it }, label = "Address", isError = false)
                FormTextField(value = suburb, onValueChange = { suburb = it }, label = "Suburb", isError = false)
                FormTextField(value = state, onValueChange = { state = it }, label = "State", isError = false)
                FormTextField(value = postcode, onValueChange = { postcode = it }, label = "Postcode", isError = false, keyboardType = KeyboardType.Number)
                FormTextField(value = country, onValueChange = { country = it }, label = "Country", isError = false)
            } else {
                val displayAddress = listOf(address, suburb)
                    .filter { it.isNotEmpty() }
                    .joinToString(", ")
                ProfileField(label = "Home Address", value = displayAddress)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Gender",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isEditMode) {
                    val genderOptions = listOf("Male", "Female")

                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .fillMaxWidth()
                    ) {
                        genderOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = gender == option,
                                        onClick = { gender = option },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = gender == option,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = FormColors.green,
                                        unselectedColor = FormColors.green
                                    )
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = gender,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            Text(
                text = "Emergency Contact",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            )

            if (isEditMode) {
                FormTextField(value = emergencyName, onValueChange = { emergencyName = it }, label = "Name", isError = false)
            } else {
                ProfileField(label = "Name", value = emergencyName)
            }

            if (isEditMode) {
                FormTextField(
                    value = emergencyEmail,
                    onValueChange = {
                        emergencyEmail = it
                        emergencyEmailError = false
                    },
                    label = "Email",
                    isError = emergencyEmailError,
                    keyboardType = KeyboardType.Email
                )
            } else {
                ProfileField(label = "Email", value = emergencyEmail)
            }

            if (isEditMode) {
                val relationOptions = listOf("Spouse", "Parent", "Other family", "Friend", "Other")
                FormDropdown(
                    label = "Relation",
                    value = emergencyRelation,
                    options = relationOptions,
                    onValueChange = { emergencyRelation = it },
                    isError = false
                )
            } else {
                ProfileField(label = "Relation", value = emergencyRelation)
            }

            if (isEditMode) {
                FormTextField(
                    value = emergencyPhoneNumber,
                    onValueChange = {
                        emergencyPhoneNumber = it
                        emergencyPhoneNumberError = false
                    },
                    label = "Phone Number",
                    isError = emergencyPhoneNumberError,
                    keyboardType = KeyboardType.Phone
                )
            } else {
                ProfileField(label = "Phone Number", value = emergencyPhoneNumber)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SuccessMessage(
                    message = "Your details have been successfully saved",
                    isVisible = showSuccessMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isEditMode) {
                                if (hasProfileChanges) {
                                    showExitConfirmationDialog = true
                                } else {
                                    isEditMode = false
                                }
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FormColors.green
                        ),
                        enabled = !isLoading
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = {
                            if (isEditMode) {
                                if (!hasProfileChanges) isEditMode = false
                                else handleSave()
                            } else {
                                isEditMode = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FormColors.green
                        ),
                        enabled = !isLoading
                    ) {
                        Text(if (isEditMode) "Save" else "Edit")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = value,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}