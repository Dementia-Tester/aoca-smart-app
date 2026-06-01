# Testing and Quality Assurance Report

## Project

Dementia Tester App

## Purpose

The purpose of this testing work was to review the current state of testing within the Dementia Tester App and identify areas that would benefit from additional testing coverage. The work focused on authentication, Firebase services, user settings, form validation, and key user workflows. This documentation was prepared to support future testing and maintenance of the application.

## Areas Reviewed

The following areas were reviewed:

* Firebase authentication
* Login and sign up workflows
* Password reset and email verification
* User settings persistence
* Profile and account functionality
* Appointment and reminder features
* General app navigation
* Error handling and form validation

## Code Reviewed

### AuthService.android.kt

This file handles Firebase authentication features such as sign in, sign up, password reset, email verification, password change, account deletion, sign out, and current user checks. The authentication workflow was reviewed to understand how user credentials are validated and how errors are handled.

### UserSettingsService.android.kt

This file handles loading and saving user settings using Firebase Realtime Database. It also checks whether a user is signed in before accessing Firebase. The review focused on how settings are stored and retrieved to ensure persistence across app sessions.

## Manual Testing Summary

| Feature        | Test Case                   | Expected Result                  | Result   |
| -------------- | --------------------------- | -------------------------------- | -------- |
| Login          | Login with valid details    | User signs in successfully       | Pass     |
| Login          | Login with invalid details  | Error message is shown           | Pass     |
| Sign Up        | Register with valid details | Account is created               | Pass     |
| Sign Up        | Use invalid email format    | Error message is shown           | Pass     |
| Sign Up        | Use weak password           | Weak password error is shown     | Pass     |
| Password Reset | Request password reset      | Reset email process starts       | Reviewed |
| Settings       | Load saved settings         | Settings are loaded correctly    | Pass     |
| Settings       | Save changed settings       | Settings are saved in Firebase   | Pass     |
| Profile        | Open profile screen         | Profile page loads correctly     | Reviewed |
| Appointment    | Open appointment screen     | Appointment page loads correctly | Reviewed |
| Navigation     | Move between screens        | App does not crash               | Pass     |
| Emulator       | Run app on Android emulator | App launches successfully        | Pass     |

## Automated Testing Plan

The following automated tests are recommended for future development:

* Unit tests for form validation logic
* Unit tests for authentication result handling
* Firebase mock tests for settings persistence
* Component tests for login, sign up, profile, settings, and appointment screens
* Notification feature testing
* CI/CD integration so tests can run automatically before deployment

## Known Limitations

Full automated testing was not completed during this stage of the project. Firebase-related testing requires mocking Firebase Authentication and Firebase Realtime Database services, which would require additional setup. The current contribution focuses on reviewing key functionality, documenting testing activities, and identifying areas that would benefit from future automated testing.

## Summary

This testing work provides a foundation for quality assurance within the Dementia Tester App. The review focused on authentication, settings persistence, and key user workflows. The documentation identifies important areas for future automated testing and supports the long-term maintenance and reliability of the application.