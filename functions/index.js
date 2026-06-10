const { onValueCreated } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Initialize Transporter
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: parseInt(process.env.SMTP_PORT),
  secure: parseInt(process.env.SMTP_PORT) === 465,
  auth: {
    user: process.env.SMTP_EMAIL,
    pass: process.env.SMTP_PASSWORD,
  },
});

function isValidEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(String(email).toLowerCase());
}

function getPatientEmailTemplate(appointment) {
  return `
    <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
      <h2 style="color: #4CAF50;">Appointment Confirmation</h2>
      <p>Hello <strong>${appointment.patientName || "Patient"}</strong>,</p>
      <p>Your appointment has been successfully booked. Here are the details:</p>
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Doctor:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.doctor || "an assigned doctor"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Date & Time:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.date} at ${appointment.time}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Type:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.type}</td>
        </tr>
      </table>
      <p>Please arrive 10 minutes early. If you need to cancel or reschedule, please contact us via the app.</p>
      <p>Best regards,<br>Dementia Tester Support Team</p>
    </div>
  `;
}

function getAdminEmailTemplate(appointment) {
  return `
    <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
      <h2 style="color: #2196F3;">New Appointment Booking Notification</h2>
      <p>A new appointment has been scheduled:</p>
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Patient Name:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.patientName || "Unknown"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Patient Email:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.patientEmail || "Not provided"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Doctor assigned:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.doctor || "an assigned doctor"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Date & Time:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.date} at ${appointment.time}</td>
        </tr>
      </table>
      <p>Action required: Please review and prepare for the session.</p>
    </div>
  `;
}

/**
 * Cloud Function triggered on Realtime Database appointment creation.
 * Path: /Appointments/{userId}/{appointmentId}
 */
exports.sendAppointmentConfirmationEmail = onValueCreated(
  "/Appointments/{userId}/{appointmentId}",
  async (event) => {
    const appointmentData = event.data.val();
    const appointmentId = event.params.appointmentId;

    console.log(`Processing new appointment: ${appointmentId} for user ${event.params.userId}`);

    if (appointmentData.emailSent === true) {
      return null;
    }

    const { patientEmail, patientName, doctorEmail } = appointmentData;
    try {
      const emailPromises = [];

      // 1. Send to Patient
      if (isValidEmail(patientEmail)) {
        emailPromises.push(transporter.sendMail({
          from: `"Dementia Tester Support" <${process.env.SMTP_EMAIL}>`,
          to: patientEmail,
          subject: "Your Appointment Confirmation - Dementia Tester App",
          html: getPatientEmailTemplate(appointmentData),
        }));
      }

      // 2. Send to Doctor/Admin
      const adminEmail = doctorEmail || process.env.ADMIN_EMAIL;
      if (isValidEmail(adminEmail)) {
        emailPromises.push(transporter.sendMail({
          from: `"System Notification" <${process.env.SMTP_EMAIL}>`,
          to: adminEmail,
          subject: `New Appointment Booking: ${patientName}`,
          html: getAdminEmailTemplate(appointmentData),
        }));
      }

      await Promise.all(emailPromises);
      console.log(`Emails sent successfully for ${appointmentId}`);

      // Mark as processed
      return event.data.ref.update({ emailSent: true });
    } catch (error) {
      console.error(`Error sending emails for ${appointmentId}:`, error);
      return null;
    }
  }
);

