package org.example.dementia_tester_app.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.ui.components.*
import org.example.dementia_tester_app.auth.AuthResult
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.UserProfile
import org.example.dementia_tester_app.data.UserProfileService
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.UserType
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.utils.*


/**
 * SignUp screen with user details and emergency contact information
 * @param onBack Callback to be invoked when the user wants to go back to the login screen
 * @param onSignUpSuccess Callback to be invoked when signup and automatic login are successful
 */
@Composable
fun SignUp(onBack: () -> Unit = {}, onSignUpSuccess: (String) -> Unit = {_ ->}) {
    val NAME = "name"
    val EMAIL = "email"
    val DATE_OF_BIRTH = "dateOfBirth"
    val PASSWORD = "password"
    val CONFIRM_PASSWORD = "confirmPassword"
    val PHONE_NUMBER = "phoneNumber"
    val ADDRESS = "address"
    val SUBURB = "suburb"
    val STATE = "state"
    val POSTCODE = "postcode"
    val COUNTRY = "country"
    val GENDER = "gender"
    val EMERGENCY_NAME = "emergencyName"
    val EMERGENCY_EMAIL = "emergencyEmail"
    val EMERGENCY_RELATION = "emergencyRelation"
    val EMERGENCY_PHONE_NUMBER = "emergencyPhoneNumber"
    // User details
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") } // Format: DD/MM/YYYY
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    // Address fields
    var address by remember { mutableStateOf("") }
    var suburb by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Emergency contact details
    var emergencyName by remember { mutableStateOf("") }
    var emergencyEmail by remember { mutableStateOf("") }
    var emergencyRelation by remember { mutableStateOf("") }
    var emergencyPhoneNumber by remember { mutableStateOf("") }

    // Using a single map to track all field error states
    var fieldErrors by remember { mutableStateOf(mapOf<String, Boolean>()) }
    
    // Special case for password matching error
    var passwordsMatchError by remember { mutableStateOf(false) }
    
    // Helper function to get error state for a field
    fun isFieldError(field: String): Boolean = fieldErrors[field] == true
    
    // Helper function to clear error for a field
    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) {
            fieldErrors = fieldErrors - field
        }
    }
    
    // Helper function to update field error state
    fun updateFieldError(field: String, hasError: Boolean) {
        fieldErrors = if (hasError) {
            fieldErrors + (field to true)
        } else {
            fieldErrors - field
        }
    }

    // General error message state
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Please enter all required fields") }
    
    val authService = remember { AuthService() }
    val userProfileService = remember { UserProfileService() }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    
    // Function to handle the signup process
    fun handleSignUp(email: String, password: String) {
        isLoading = true
        authService.signUp(email, password) { result ->
            when (result) {
                is AuthResult.Success -> {
                    // Create user profile
                    val userProfile = UserProfile(
                        name = name,
                        dateOfBirth = dateOfBirth,
                        email = email,
                        phoneNumber = phoneNumber,
                        userType = UserType.USER,
                        address = address,
                        suburb = suburb,
                        state = state,
                        postcode = postcode,
                        country = country,
                        gender = gender,
                        emergencyName = emergencyName,
                        emergencyEmail = emergencyEmail,
                        emergencyRelation = emergencyRelation,
                        emergencyPhoneNumber = emergencyPhoneNumber
                    )
                    
                    // Save user profile to database
                    userProfileService.updateUserProfile(userProfile) { dbResult ->
                        isLoading = false
                        when (dbResult) {
                            is DatabaseResult.Success -> {
                                // Profile saved successfully, navigate to dashboard
                                onSignUpSuccess(email)
                            }
                            is DatabaseResult.Error -> {
                                // Profile save failed, show error message
                                errorMessage = "Signup successful but profile save failed: ${dbResult.message}"
                                showErrorMessage = true
                            }
                        }
                    }
                }
                is AuthResult.Error -> {
                    // Registration failed, show error message
                    isLoading = false
                    errorMessage = result.message
                    showErrorMessage = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "Sign Up",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 70.dp, bottom = 24.dp)
        )

        // Your Details Section
        Text(
            text = "Your Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Name Field
        FormTextField(
            value = name,
            onValueChange = { 
                name = it
                clearFieldError(NAME)
                showErrorMessage = false
            },
            label = "Name",
            isError = isFieldError(NAME),
        )

        // Email Field
        FormTextField(
            value = email,
            onValueChange = { 
                email = it
                clearFieldError(EMAIL)
                showErrorMessage = false
            },
            label = "Email",
            isError = isFieldError(EMAIL),
            keyboardType = KeyboardType.Email
        )
        
        // Date of Birth Field
        DateField(
            date = dateOfBirth,
            onDateChange = { 
                dateOfBirth = it
                clearFieldError(DATE_OF_BIRTH)
                showErrorMessage = false
            },
            label = "Date of Birth",
            isError = isFieldError(DATE_OF_BIRTH),
            isEditable = true,
            allowDatesAfterToday = false
        )

        // Password Field
        FormTextField(
            value = password,
            onValueChange = { 
                password = it
                clearFieldError(PASSWORD)
                passwordsMatchError = false
                showErrorMessage = false
            },
            label = "Password",
            isError = isFieldError(PASSWORD) || passwordsMatchError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        // Confirm Password Field
        FormTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                clearFieldError(CONFIRM_PASSWORD)
                passwordsMatchError = false
                showErrorMessage = false
            },
            label = "Confirm Password",
            isError = isFieldError(CONFIRM_PASSWORD) || passwordsMatchError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        // Phone Number Field
        FormTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                clearFieldError(PHONE_NUMBER)
                showErrorMessage = false
            },
            label = "Phone Number",
            isError = isFieldError(PHONE_NUMBER),
            keyboardType = KeyboardType.Phone
        )

        // Address Fields
        Text(
            text = "Home Address",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        )
        FormTextField(
            value = address,
            onValueChange = { 
                address = it
                clearFieldError(ADDRESS)
                showErrorMessage = false
            },
            label = "Address",
            isError = isFieldError(ADDRESS),
        )
        FormTextField(
            value = suburb,
            onValueChange = { 
                suburb = it
                clearFieldError(SUBURB)
                showErrorMessage = false
            },
            label = "Suburb",
            isError = isFieldError(SUBURB),
        )
        FormTextField(
            value = state,
            onValueChange = { 
                state = it
                clearFieldError(STATE)
                showErrorMessage = false
            },
            label = "State",
            isError = isFieldError(STATE),
        )
        FormTextField(
            value = postcode,
            onValueChange = { 
                postcode = it
                clearFieldError(POSTCODE)
                showErrorMessage = false
            },
            label = "Postcode",
            isError = isFieldError(POSTCODE),
            keyboardType = KeyboardType.Number
        )
        FormTextField(
            value = country,
            onValueChange = { 
                country = it
                clearFieldError(COUNTRY)
                showErrorMessage = false
            },
            label = "Country",
            isError = isFieldError(COUNTRY),
        )
        // Gender Field - Radio Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Gender",
                color = if (isFieldError(GENDER)) FormColors.errorColor else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Radio button options
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
                                onClick = { 
                                    gender = option
                                    clearFieldError(GENDER)
                                    showErrorMessage = false
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == option,
                            onClick = null, // null because we're handling the click on the row
                            colors = RadioButtonDefaults.colors(
                                selectedColor = FormColors.green,
                                unselectedColor = if (isFieldError(GENDER)) FormColors.errorColor else FormColors.green
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

            // Show an error outline if there's an error
            if (isFieldError(GENDER)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(FormColors.errorColor)
                )
            }
        }

        // Emergency Contact Section
        Text(
            text = "Emergency Contact",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Emergency Contact Name Field
        FormTextField(
            value = emergencyName,
            onValueChange = { 
                emergencyName = it
                clearFieldError(EMERGENCY_NAME)
                showErrorMessage = false
            },
            label = "Name",
            isError = isFieldError(EMERGENCY_NAME),
        )

        // Emergency Contact Email Field
        FormTextField(
            value = emergencyEmail,
            onValueChange = { 
                emergencyEmail = it
                clearFieldError(EMERGENCY_EMAIL)
                showErrorMessage = false
            },
            label = "Email",
            isError = isFieldError(EMERGENCY_EMAIL),
            keyboardType = KeyboardType.Email
        )

        // Emergency Contact Relation Field - Dropdown
        val relationOptions = listOf("Spouse", "Parent", "Other family", "Friend", "Other")
        FormDropdown(
            label = "Relation",
            value = emergencyRelation,
            options = relationOptions,
            onValueChange = { 
                emergencyRelation = it
                clearFieldError(EMERGENCY_RELATION)
                showErrorMessage = false
            },
            isError = isFieldError(EMERGENCY_RELATION),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Emergency Contact Phone Number Field
        FormTextField(
            value = emergencyPhoneNumber,
            onValueChange = { 
                emergencyPhoneNumber = it
                clearFieldError(EMERGENCY_PHONE_NUMBER)
                showErrorMessage = false
            },
            label = "Phone Number",
            isError = isFieldError(EMERGENCY_PHONE_NUMBER),
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Submit Button
        Button(
            onClick = { 
                // Validate all fields using the shared validation utility
                fieldErrors = validateFields(
                    mapOf(
                        NAME to name,
                        EMAIL to email,
                        DATE_OF_BIRTH to dateOfBirth,
                        PASSWORD to password,
                        CONFIRM_PASSWORD to confirmPassword,
                        PHONE_NUMBER to phoneNumber,
                        ADDRESS to address,
                        SUBURB to suburb,
                        STATE to state,
                        POSTCODE to postcode,
                        COUNTRY to country,
                        GENDER to gender,
                        EMERGENCY_NAME to emergencyName,
                        EMERGENCY_EMAIL to emergencyEmail,
                        EMERGENCY_RELATION to emergencyRelation,
                        EMERGENCY_PHONE_NUMBER to emergencyPhoneNumber
                    )
                )

                // Check if any field is empty
                if (fieldErrors.isNotEmpty()) {
                    // Show a general error message
                    errorMessage = "Please enter all required fields"
                    showErrorMessage = true
                } 
                // Validate email format
                else if (!email.isValidEmail()) {
                    updateFieldError(EMAIL, true)
                    errorMessage = "Please enter a valid email address"
                    showErrorMessage = true
                }
                // Validate emergency email format if not empty
                else if (emergencyEmail.isNotEmpty() && !emergencyEmail.isValidEmail()) {
                    updateFieldError(EMERGENCY_EMAIL, true)
                    errorMessage = "Please enter a valid emergency contact email address"
                    showErrorMessage = true
                }
                // Validate phone number format
                else if (!phoneNumber.isValidPhoneNumber()) {
                    updateFieldError(PHONE_NUMBER, true)
                    errorMessage = "Please enter a valid phone number"
                    showErrorMessage = true
                }
                // Validate emergency phone number format
                else if (!emergencyPhoneNumber.isValidPhoneNumber()) {
                    updateFieldError(EMERGENCY_PHONE_NUMBER, true)
                    errorMessage = "Please enter a valid emergency contact phone number"
                    showErrorMessage = true
                }
                // Check if passwords match
                else if (password != confirmPassword) {
                    // Passwords don't match
                    passwordsMatchError = true
                    errorMessage = "Passwords do not match"
                    showErrorMessage = true
                } else {
                    handleSignUp(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormColors.green
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                LoadingSpinner()
            } else {
                Text("Signup")
            }
        }

        ErrorMessage(
            show = showErrorMessage,
            message = errorMessage
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Log in Button
        OutlinedButton(
            onClick = { onBack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FormColors.green
            )
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

