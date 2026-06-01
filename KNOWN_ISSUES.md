# Known Issues and Future Testing Work

## Known Issues

1. Full automated testing coverage has not yet been implemented across the application.
2. Firebase Authentication and Firebase Database functionality would benefit from additional mock testing.
3. Compose UI component testing has not yet been added for all major screens.
4. Notification functionality has mainly been reviewed using the emulator and should also be tested on a physical Android device.
5. Automated test execution through a CI/CD pipeline has not yet been configured.
6. Some features rely on live Firebase services, meaning testing results may be affected by network availability or Firebase configuration issues.

## Future Work

The following improvements are recommended for future development:

* Add unit tests for login and sign up validation.
* Add tests for password reset and email verification workflows.
* Add tests for user settings loading and saving.
* Add tests for appointment creation and appointment history functionality.
* Add tests for notification preferences and reminder features.
* Add Compose UI tests for important screens such as Login, Sign Up, Profile, Settings, and Appointments.
* Integrate automated testing into GitHub Actions or another CI/CD workflow.
* Measure and monitor test coverage to identify untested areas of the application.

## Summary

The issues listed above do not prevent the Dementia Tester App from operating, but they highlight areas where testing and quality assurance can be improved. Increasing automated test coverage would improve reliability, reduce the likelihood of regression bugs, and make future maintenance of the application easier for future developers and project teams.