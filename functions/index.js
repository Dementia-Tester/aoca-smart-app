const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Initialize Transporter
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: parseInt(process.env.SMTP_PORT),
  secure: parseInt(process.env.SMTP_PORT) === 465, // true for 465, false for other ports
  auth: {
    user: process.env.SMTP_EMAIL,
    pass: process.env.SMTP_PASSWORD,
  },
});

/**
 * Validates an email address.
 */
function isValidEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(String(email).toLowerCase());
}

/**
 * Generates the email template for the patient.
 */
function getPatientEmailTemplate(appointment) {
  return `
    <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
      <h2 style="color: #4CAF50;">Appointment Confirmation</h2>
      <p>Hello <strong>${appointment.patientName || "Patient"}</strong>,</p>
      <p>Your appointment has been successfully booked. Here are the details:</p>
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Booking ID:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.id}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Doctor:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.doctor || "an assigned doctor"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px soli
          d #ddd;"><strong>Date & Time:</strong></td>
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

/**
 * Generates the email template for the doctor/admin.
 */
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
         <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Reason/Comments:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${appointment.reason || "No comments provided"}</td>
        </tr>
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;"><strong>Booked At:</strong></td>
          <td style="padding: 8px; border-bottom: 1px solid #ddd;">${new Date().toLocaleString()}</td>
        </tr>
      </table>
      <p>Action required: Please review and prepare for the session.</p>
    </div>
  `;
}

/**
 * Cloud Function triggered on Firestore appointment creation.
 */
exports.sendAppointmentConfirmationEmail = functions.firestore
  .document("appointments/{appointmentId}")
  .onCreate(async (snap, context) => {
    const appointmentId = context.params.appointmentId;
    const appointmentData = snap.data();
    appointmentData.id = appointmentId;

    console.log(`Processing new appointment: ${appointmentId}`);

    // Check for duplicate processing (Optional: using a 'processed' flag in Firestore)
    if (appointmentData.emailSent === true) {
      console.log(`Email already sent for appointment ${appointmentId}. Skipping.`);
      return null;
    }

    const { patientEmail, patientName, doctorEmail } = appointmentData;

    try {
      const emailPromises = [];

      // 1. Send confirmation to Patient
      if (isValidEmail(patientEmail)) {
        const patientMailOptions = {
          from: `"Dementia Tester Support" <${process.env.SMTP_EMAIL}>`,
          to: patientEmail,
          subject: "Your Appointment Confirmation - Dementia Tester App",
          html: getPatientEmailTemplate(appointmentData),
        };
        emailPromises.push(transporter.sendMail(patientMailOptions));
      } else {
        console.warn(`Invalid patient email: ${patientEmail}`);
      }

      // 2. Send notification to Doctor/Admin
      const adminEmail = doctorEmail || process.env.ADMIN_EMAIL;
      if (isValidEmail(adminEmail)) {
        const adminMailOptions = {
          from: `"System Notification" <${process.env.SMTP_EMAIL}>`,
          to: adminEmail,
          subject: `New Appointment Booking: ${patientName}`,
          html: getAdminEmailTemplate(appointmentData),
        };
        emailPromises.push(transporter.sendMail(adminMailOptions));
      }

      // Wait for all emails to be sent
      await Promise.all(emailPromises);
      console.log(`Emails sent successfully for appointment ${appointmentId}`);

      // Mark as processed in Firestore (best effort)
      await snap.ref.update({ emailSent: true });

    } catch (error) {
      console.error(`Error sending emails for ${appointmentId}:`, error);
      // We don't throw the error so that the Firestore creation itself isn't impacted
      // (although as a trigger, it wouldn't impact the client-side write anyway)
    }

    return null;
  });
