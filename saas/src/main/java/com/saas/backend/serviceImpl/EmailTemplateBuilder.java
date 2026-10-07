package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.Client;
import com.saas.backend.models.Company;
import com.saas.backend.models.Guest;
import com.saas.backend.models.RoomRequirement;
import com.saas.backend.models.Safari;
import com.saas.backend.models.User;

/**
 * Builds responsive executive HTML and structured plain-text email templates
 * for accommodation reservation requests and follow-up reminders.
 *
 * Design Guidelines:
 * - Authentic East African Safari Palette: Deep Safari Forest Green (#264624) & Warm Earth Tones / Savannah Browns (#7A5229, #8C6239, #EDE7DC, #FAF8F4).
 * - Typography: Titles / Headings in Arial Bolded; Body Text in Calibri Light.
 * - Strictly NO emojis (all symbols/emojis removed).
 */
@Component
public class EmailTemplateBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm 'UTC'", Locale.ENGLISH);

    /**
     * Builds responsive HTML email for booking request.
     */
    public String buildBookingHtml(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;
        User consultant = resolveConsultant(booking, safari);
        Company company = resolveCompany(consultant, client);

        String bookingRef = escapeHtml(booking.getReferenceNumber());
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? escapeHtml(booking.getProperty().getName())
                : "Lodge Reservations Team";
        String checkInStr = booking.getCheckIn() != null ? booking.getCheckIn().format(DATE_FORMATTER) : "TBD";
        String checkOutStr = booking.getCheckOut() != null ? booking.getCheckOut().format(DATE_FORMATTER) : "TBD";
        String nightsStr = calculateNights(booking);

        String safariRef = safari != null && safari.getReferenceNumber() != null
                ? escapeHtml(safari.getReferenceNumber())
                : "N/A";
        String itineraryDayText = formatItineraryDay(req);
        String roomCount = (req != null && req.getNumberOfRooms() != null)
                ? String.valueOf(req.getNumberOfRooms())
                : "1";
        String guestCount = (safari != null && safari.getNumberOfPassengers() != null)
                ? String.valueOf(safari.getNumberOfPassengers())
                : (guests != null && !guests.isEmpty() ? String.valueOf(guests.size()) : "1");

        String roomRowsHtml = buildRoomRowsHtml(req);
        String roomPreferencesBlock = buildRoomPreferencesHtml(req);
        String guestRowsHtml = buildGuestRowsHtml(guests, client);
        String specialRequestsBlock = buildSpecialRequestsHtml(req, booking);
        String consultantBlock = buildConsultantSignatureHtml(consultant, company, systemEmail);
        String timestampStr = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Accommodation Reservation Request - %s</title>
<style type="text/css">
  body { margin: 0; padding: 0; min-width: 100%%; width: 100%% !important; height: 100%% !important; background-color: #F5F2EB; -webkit-font-smoothing: antialiased; }
  table { border-spacing: 0; border-collapse: collapse; }
  td { padding: 0; }
  img { border: 0; }
  @media screen and (max-width: 600px) {
    .container { width: 100%% !important; max-width: 100%% !important; }
    .col-stack { display: block !important; width: 100%% !important; box-sizing: border-box !important; }
    .mobile-padding { padding-left: 16px !important; padding-right: 16px !important; }
  }
</style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F5F2EB; font-family: 'Calibri Light', Calibri, sans-serif; font-weight: 300; color: #2D241E; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 16px rgba(45, 36, 30, 0.08); border: 1px solid #E2D9CC; text-align: left;">
      <!-- TOP WARM EARTH TONE STRIPE -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="5" style="background: linear-gradient(90deg, #7A5229 0%%, #B8860B 50%%, #7A5229 100%%); background-color: #7A5229;"></td>
        </tr>
      </table>

      <!-- EXECUTIVE SAFARI HEADER BANNER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #264624; padding: 28px 32px; color: #FFFFFF;">
        <tr>
          <td>
            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td>
                  <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-transform: uppercase; font-size: 11px; letter-spacing: 1.6px; color: #D5E3D2; margin-bottom: 6px;">
                    SAFARI OPERATIONS &bull; RESERVATION DESK
                  </div>
                  <h1 style="margin: 0; font-family: Arial, Helvetica, sans-serif; font-size: 20px; font-weight: bold; color: #FFFFFF; letter-spacing: -0.2px; line-height: 1.3;">
                    ACCOMMODATION RESERVATION REQUEST
                  </h1>
                </td>
              </tr>
              <tr>
                <td style="padding-top: 14px;">
                  <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td style="background-color: rgba(255, 255, 255, 0.14); border: 1px solid rgba(220, 205, 175, 0.5); border-radius: 18px; padding: 5px 14px;">
                        <span style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #EDE4D3; letter-spacing: 0.5px;">
                          REF: %s
                        </span>
                      </td>
                      <td style="padding-left: 12px;">
                        <span style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #D5E3D2;">
                          Dispatched: %s
                        </span>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>

      <!-- MAIN CONTENT -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 28px 32px;" class="mobile-padding">
        <tr>
          <td>
            <!-- SALUTATION -->
            <p style="margin: 0 0 14px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px; color: #2D241E; line-height: 1.6;">
              Dear Reservations Team at <strong style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;">%s</strong>,
            </p>
            <p style="margin: 0 0 22px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; color: #4A3A2F; line-height: 1.6;">
              Please accept our official accommodation reservation request for our upcoming safari party. Kindly review availability and confirm space for the dates and room configuration detailed below:
            </p>

            <!-- SECTION 1: STAY OVERVIEW CARDS -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden; background-color: #FAF8F4;">
              <tr>
                <td style="padding: 11px 16px; background-color: #EDE7DC; border-bottom: 1px solid #E2D9CC; font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #3A2E26; text-transform: uppercase; letter-spacing: 0.8px;">
                  STAY OVERVIEW &amp; ITINERARY PARAMETERS
                </td>
              </tr>
              <tr>
                <td style="padding: 16px;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px; padding-bottom: 12px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Check-In Date</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px; padding-bottom: 12px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Check-Out Date</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px; padding-bottom: 12px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Duration</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #264624;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px; padding-bottom: 12px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Total Requirements</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s Room(s) &bull; %s Guest(s)</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Safari Reference</div>
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 13px; font-weight: bold; color: #264624;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold; letter-spacing: 0.5px; margin-bottom: 2px;">Itinerary Schedule</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #3A2E26;">%s</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>

            <!-- SECTION 2: ROOM REQUIREMENTS -->
            <div style="margin-bottom: 24px;">
              <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #1F1916; text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 8px;">
                ROOM ALLOCATION &amp; REQUIREMENTS
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #EDE7DC; text-align: left; font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #3A2E26; text-transform: uppercase; letter-spacing: 0.5px;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC;">Room Category</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC; text-align: center; width: 90px;">Quantity</th>
                  </tr>
                </thead>
                <tbody style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #2D241E;">
                  %s
                </tbody>
              </table>
              %s
            </div>

            <!-- SECTION 3: GUEST MANIFEST -->
            <div style="margin-bottom: 24px;">
              <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #1F1916; text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 8px;">
                GUEST MANIFEST &amp; PASSENGER DETAILS
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #EDE7DC; text-align: left; font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #3A2E26; text-transform: uppercase; letter-spacing: 0.5px;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC; width: 36px; text-align: center;">#</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC;">Guest Full Name</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC; width: 90px;">Gender</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC; width: 120px;">Nationality</th>
                  </tr>
                </thead>
                <tbody style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #2D241E;">
                  %s
                </tbody>
              </table>
            </div>

            <!-- SECTION 4: SPECIAL REQUESTS & NOTES -->
            %s

            <!-- SECTION 5: ACTION REQUIRED & PROTOCOL -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #DFD5C3; border-left: 5px solid #264624; border-radius: 8px; background-color: #FAF7F2;">
              <tr>
                <td style="padding: 16px 20px;">
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #264624; text-transform: uppercase; letter-spacing: 0.6px; margin-bottom: 8px;">
                    ACTION REQUIRED: CONFIRMATION PROTOCOL
                  </div>
                  <ol style="margin: 0; padding-left: 18px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #3D3128; line-height: 1.6;">
                    <li style="margin-bottom: 6px;">
                      <strong style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;">Confirm Room Availability:</strong> Please reply directly to this email confirming space for the requested room configuration.
                    </li>
                    <li style="margin-bottom: 6px;">
                      <strong style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;">Lodge Confirmation Reference:</strong> Kindly provide your property confirmation number or reservation voucher code.
                    </li>
                    <li>
                      <strong style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;">Automated Tracking:</strong> Please maintain '<span style="font-family: Arial, monospace; background-color: #EDE7DC; color: #264624; padding: 2px 6px; border-radius: 4px; font-weight: bold; border: 1px solid #D8CBB6;">%s</span>' in the email subject line to guarantee automated status processing.
                    </li>
                  </ol>
                </td>
              </tr>
            </table>

            <!-- SECTION 6: CONSULTANT SIGNATURE -->
            %s
          </td>
        </tr>
      </table>

      <!-- FOOTER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #1F1A17; padding: 20px 32px; color: #A69C8E; font-size: 11px; text-align: center; border-top: 1px solid #2B231E;" class="mobile-padding">
        <tr>
          <td>
            <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #EDE7DC; margin-bottom: 4px; letter-spacing: 0.6px;">
              SAFARI OPERATIONS RESERVATIONS PLATFORM
            </div>
            <div style="font-family: 'Calibri Light', Calibri, sans-serif;">
              Automated Reservation Dispatch &bull; Reference: %s &bull; Generated: %s
            </div>
            <div style="margin-top: 6px; color: #877D70; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 10px;">
              This transmission is intended solely for the property reservations department. Confidentiality protected.
            </div>
          </td>
        </tr>
      </table>
    </div>
  </center>
</body>
</html>
"""
        .formatted(
                bookingRef,
                bookingRef,
                timestampStr,
                propName,
                checkInStr,
                checkOutStr,
                nightsStr,
                roomCount,
                guestCount,
                safariRef,
                itineraryDayText,
                roomRowsHtml,
                roomPreferencesBlock,
                guestRowsHtml,
                specialRequestsBlock,
                bookingRef,
                consultantBlock,
                bookingRef,
                timestampStr
        );
    }

    /**
     * Builds structured plain-text fallback for booking request.
     */
    public String buildBookingPlainText(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;
        User consultant = resolveConsultant(booking, safari);
        Company company = resolveCompany(consultant, client);

        String bookingRef = booking.getReferenceNumber();
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? booking.getProperty().getName()
                : "Lodge Reservations Team";
        String checkInStr = booking.getCheckIn() != null ? booking.getCheckIn().format(DATE_FORMATTER) : "TBD";
        String checkOutStr = booking.getCheckOut() != null ? booking.getCheckOut().format(DATE_FORMATTER) : "TBD";
        String nightsStr = calculateNights(booking);

        String safariRef = safari != null && safari.getReferenceNumber() != null ? safari.getReferenceNumber() : "N/A";
        String itineraryDayText = formatItineraryDay(req);
        String roomCount = (req != null && req.getNumberOfRooms() != null) ? String.valueOf(req.getNumberOfRooms()) : "1";
        String guestCount = (safari != null && safari.getNumberOfPassengers() != null)
                ? String.valueOf(safari.getNumberOfPassengers())
                : (guests != null && !guests.isEmpty() ? String.valueOf(guests.size()) : "1");

        String roomsText = buildRoomsPlainText(req);
        String guestsText = buildGuestsPlainText(guests, client);

        StringBuilder sb = new StringBuilder();
        sb.append("ACCOMMODATION RESERVATION REQUEST\n");
        sb.append("Reference: ").append(bookingRef).append("\n");
        sb.append("----------------------------------------------------------------------\n\n");
        sb.append("Dear Reservations Team at ").append(propName).append(",\n\n");
        sb.append("Please accept our official accommodation reservation request for our upcoming safari party.\n\n");
        sb.append("1. RESERVATION PARAMETERS:\n");
        sb.append("   - Booking Reference:   ").append(bookingRef).append("\n");
        sb.append("   - Safari Reference:    ").append(safariRef).append("\n");
        sb.append("   - Itinerary Schedule:  ").append(itineraryDayText).append("\n");
        sb.append("   - Check-in Date:       ").append(checkInStr).append("\n");
        sb.append("   - Check-out Date:      ").append(checkOutStr).append("\n");
        sb.append("   - Duration:            ").append(nightsStr).append("\n");
        sb.append("   - Total Rooms:         ").append(roomCount).append("\n");
        sb.append("   - Total Guests:        ").append(guestCount).append("\n\n");

        sb.append("2. ROOM REQUIREMENTS:\n");
        sb.append(roomsText).append("\n");
        if (req != null && req.getRoomPreferences() != null && !req.getRoomPreferences().isBlank()) {
            sb.append("   Preferences: ").append(req.getRoomPreferences().trim()).append("\n\n");
        } else {
            sb.append("\n");
        }

        sb.append("3. GUEST MANIFEST:\n");
        sb.append(guestsText).append("\n\n");

        boolean hasSpecial = req != null && req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank();
        boolean hasNotes = booking.getNotes() != null && !booking.getNotes().isBlank();
        if (hasSpecial || hasNotes) {
            sb.append("4. SPECIAL REQUESTS & NOTES:\n");
            if (hasSpecial) {
                sb.append("   - Special Requests: ").append(req.getSpecialRequests().trim()).append("\n");
            }
            if (hasNotes) {
                sb.append("   - Additional Notes: ").append(booking.getNotes().trim()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("5. ACTION REQUIRED & CONFIRMATION PROTOCOL:\n");
        sb.append("   - Please reply to this email directly confirming availability.\n");
        sb.append("   - Kindly include your lodge reservation number / voucher reference.\n");
        sb.append("   - Please maintain '").append(bookingRef).append("' in the subject line.\n\n");

        sb.append("Best regards,\n");
        if (consultant != null) {
            sb.append(consultant.getFirstName()).append(" ").append(consultant.getLastName()).append("\n");
            if (consultant.getRole() != null) {
                sb.append(consultant.getRole().getName().replace("_", " ")).append("\n");
            }
        } else {
            sb.append("Reservations Operations Desk\n");
        }
        if (company != null && company.getName() != null) {
            sb.append(company.getName()).append("\n");
        }
        sb.append("Email: ").append(systemEmail).append("\n");
        if (consultant != null && consultant.getPhone() != null && !consultant.getPhone().isBlank()) {
            sb.append("Phone: ").append(consultant.getPhone()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Builds responsive HTML email for booking reminder/follow-up.
     */
    public String buildReminderHtml(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;
        User consultant = resolveConsultant(booking, safari);
        Company company = resolveCompany(consultant, client);

        String bookingRef = escapeHtml(booking.getReferenceNumber());
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? escapeHtml(booking.getProperty().getName())
                : "Lodge Reservations Team";
        String checkInStr = booking.getCheckIn() != null ? booking.getCheckIn().format(DATE_FORMATTER) : "TBD";
        String checkOutStr = booking.getCheckOut() != null ? booking.getCheckOut().format(DATE_FORMATTER) : "TBD";
        String nightsStr = calculateNights(booking);
        String safariRef = safari != null && safari.getReferenceNumber() != null ? escapeHtml(safari.getReferenceNumber()) : "N/A";
        String roomRowsHtml = buildRoomRowsHtml(req);
        String consultantBlock = buildConsultantSignatureHtml(consultant, company, systemEmail);
        String timestampStr = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Follow-up - Accommodation Request %s</title>
<style type="text/css">
  body { margin: 0; padding: 0; min-width: 100%%; width: 100%% !important; background-color: #F5F2EB; -webkit-font-smoothing: antialiased; }
  table { border-spacing: 0; border-collapse: collapse; }
  td { padding: 0; }
  @media screen and (max-width: 600px) {
    .container { width: 100%% !important; max-width: 100%% !important; }
    .mobile-padding { padding-left: 16px !important; padding-right: 16px !important; }
  }
</style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F5F2EB; font-family: 'Calibri Light', Calibri, sans-serif; font-weight: 300; color: #2D241E; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 16px rgba(45, 36, 30, 0.08); border: 1px solid #E2D9CC; text-align: left;">
      <!-- TOP WARM EARTH TONE STRIPE -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="5" style="background: linear-gradient(90deg, #7A5229 0%%, #B8860B 100%%); background-color: #7A5229;"></td>
        </tr>
      </table>

      <!-- HEADER BANNER (SAFARI FOREST GREEN) -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #264624; padding: 28px 32px; color: #FFFFFF;">
        <tr>
          <td>
            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td>
                  <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-transform: uppercase; font-size: 11px; letter-spacing: 1.6px; color: #D5E3D2; margin-bottom: 6px;">
                    FOLLOW-UP NOTIFICATION &bull; PENDING CONFIRMATION
                  </div>
                  <h1 style="margin: 0; font-family: Arial, Helvetica, sans-serif; font-size: 20px; font-weight: bold; color: #FFFFFF; letter-spacing: -0.2px; line-height: 1.3;">
                    RESERVATION REQUEST FOLLOW-UP
                  </h1>
                </td>
              </tr>
              <tr>
                <td style="padding-top: 14px;">
                  <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td style="background-color: rgba(255, 255, 255, 0.14); border: 1px solid rgba(220, 205, 175, 0.5); border-radius: 18px; padding: 5px 14px;">
                        <span style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #EDE4D3; letter-spacing: 0.5px;">
                          REF: %s
                        </span>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>

      <!-- CONTENT BODY -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 28px 32px;" class="mobile-padding">
        <tr>
          <td>
            <p style="margin: 0 0 14px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px; color: #2D241E; line-height: 1.6;">
              Dear Reservations Team at <strong style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;">%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; color: #4A3A2F; line-height: 1.6;">
              We are following up on our accommodation reservation request submitted previously. We have not yet received confirmation of room availability for this booking.
            </p>

            <!-- REMINDER SUMMARY CARD -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden; background-color: #FAF8F4;">
              <tr>
                <td style="padding: 11px 16px; background-color: #EDE7DC; border-bottom: 1px solid #E2D9CC; font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #3A2E26; text-transform: uppercase; letter-spacing: 0.8px;">
                  PENDING RESERVATION SUMMARY
                </td>
              </tr>
              <tr>
                <td style="padding: 16px;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="50%%" valign="top" style="padding-bottom: 10px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Check-In Date</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-bottom: 10px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Check-Out Date</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Duration</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #264624;">%s</div>
                      </td>
                      <td width="50%%" valign="top">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Safari Reference</div>
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 13px; font-weight: bold; color: #264624;">%s</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>

            <!-- ROOM TABLE -->
            <div style="margin-bottom: 24px;">
              <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #1F1916; text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 8px;">
                REQUESTED ROOMS:
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #EDE7DC; text-align: left; font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #3A2E26; text-transform: uppercase;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC;">Room Category</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2D9CC; text-align: center; width: 90px;">Quantity</th>
                  </tr>
                </thead>
                <tbody style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #2D241E;">
                  %s
                </tbody>
              </table>
            </div>

            <!-- URGENT CTA (WARM EARTH BROWN ACCENT) -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E5D5C0; border-left: 5px solid #7A5229; border-radius: 8px; background-color: #FAF6F0;">
              <tr>
                <td style="padding: 16px 20px;">
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #7A5229; text-transform: uppercase; letter-spacing: 0.6px; margin-bottom: 6px;">
                    KINDLY CONFIRM AT YOUR EARLIEST CONVENIENCE
                  </div>
                  <p style="margin: 0; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #4A3525; line-height: 1.55;">
                    Please reply to this email confirming availability or advising if alternative dates/rooms are available. Kindly keep reference '<span style="font-family: Arial, monospace; background-color: #EDE7DC; color: #7A5229; padding: 2px 6px; border-radius: 4px; font-weight: bold; border: 1px solid #D8CBB6;">%s</span>' in the subject line.
                  </p>
                </td>
              </tr>
            </table>

            %s
          </td>
        </tr>
      </table>

      <!-- FOOTER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #1F1A17; padding: 20px 32px; color: #A69C8E; font-size: 11px; text-align: center;" class="mobile-padding">
        <tr>
          <td>
            <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #EDE7DC; margin-bottom: 4px;">SAFARI OPERATIONS RESERVATIONS PLATFORM</div>
            <div style="font-family: 'Calibri Light', Calibri, sans-serif;">Follow-up Reminder &bull; Reference: %s &bull; Dispatched: %s</div>
          </td>
        </tr>
      </table>
    </div>
  </center>
</body>
</html>
"""
        .formatted(
                bookingRef,
                bookingRef,
                propName,
                checkInStr,
                checkOutStr,
                nightsStr,
                safariRef,
                roomRowsHtml,
                bookingRef,
                consultantBlock,
                bookingRef,
                timestampStr
        );
    }

    /**
     * Builds structured plain-text fallback for reminder email.
     */
    public String buildReminderPlainText(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        Client client = safari != null ? safari.getClient() : null;
        User consultant = resolveConsultant(booking, safari);
        Company company = resolveCompany(consultant, client);

        String bookingRef = booking.getReferenceNumber();
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? booking.getProperty().getName()
                : "Lodge Reservations Team";
        String checkInStr = booking.getCheckIn() != null ? booking.getCheckIn().format(DATE_FORMATTER) : "TBD";
        String checkOutStr = booking.getCheckOut() != null ? booking.getCheckOut().format(DATE_FORMATTER) : "TBD";
        String nightsStr = calculateNights(booking);
        String safariRef = safari != null && safari.getReferenceNumber() != null ? safari.getReferenceNumber() : "N/A";
        String roomsText = buildRoomsPlainText(req);

        StringBuilder sb = new StringBuilder();
        sb.append("FOLLOW-UP: ACCOMMODATION RESERVATION REQUEST\n");
        sb.append("Reference: ").append(bookingRef).append("\n");
        sb.append("----------------------------------------------------------------------\n\n");
        sb.append("Dear Reservations Team at ").append(propName).append(",\n\n");
        sb.append("We are following up on our accommodation reservation request. We have not yet received confirmation of room availability.\n\n");
        sb.append("BOOKING DETAILS:\n");
        sb.append("   - Booking Reference:   ").append(bookingRef).append("\n");
        sb.append("   - Safari Reference:    ").append(safariRef).append("\n");
        sb.append("   - Check-in Date:       ").append(checkInStr).append("\n");
        sb.append("   - Check-out Date:      ").append(checkOutStr).append("\n");
        sb.append("   - Duration:            ").append(nightsStr).append("\n\n");
        sb.append("ROOM REQUIREMENTS:\n");
        sb.append(roomsText).append("\n\n");
        sb.append("ACTION REQUIRED:\n");
        sb.append("Please reply at your earliest convenience confirming room availability.\n");
        sb.append("Kindly maintain reference '").append(bookingRef).append("' in the subject line.\n\n");
        sb.append("Best regards,\n");
        if (consultant != null) {
            sb.append(consultant.getFirstName()).append(" ").append(consultant.getLastName()).append("\n");
        } else {
            sb.append("Reservations Operations Desk\n");
        }
        if (company != null && company.getName() != null) {
            sb.append(company.getName()).append("\n");
        }
        sb.append("Email: ").append(systemEmail).append("\n");

        return sb.toString();
    }

    /* -----------------------------------------------------------------------
     * Internal Helpers
     * --------------------------------------------------------------------- */

    private User resolveConsultant(AccommodationBooking booking, Safari safari) {
        if (booking.getReservationManager() != null) {
            return booking.getReservationManager();
        }
        if (safari != null && safari.getSalesPerson() != null) {
            return safari.getSalesPerson();
        }
        return null;
    }

    private Company resolveCompany(User consultant, Client client) {
        if (consultant != null && consultant.getCompany() != null) {
            return consultant.getCompany();
        }
        if (client != null && client.getCompany() != null) {
            return client.getCompany();
        }
        return null;
    }

    private String calculateNights(AccommodationBooking booking) {
        if (booking.getCheckIn() != null && booking.getCheckOut() != null) {
            long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
            return nights + " " + (nights == 1 ? "Night" : "Nights");
        }
        return "N/A";
    }

    private String formatItineraryDay(AccommodationRequirement req) {
        if (req != null && req.getItineraryDay() != null) {
            String dayNum = "Day " + req.getItineraryDay().getDayNumber();
            String dest = req.getItineraryDay().getDestination() != null
                    ? req.getItineraryDay().getDestination()
                    : (req.getDestination() != null ? req.getDestination() : "Route Scheduled");
            return escapeHtml(dayNum + " - " + dest);
        }
        if (req != null && req.getDestination() != null && !req.getDestination().isBlank()) {
            return escapeHtml(req.getDestination());
        }
        return "Scheduled Safari Route";
    }

    private String buildRoomRowsHtml(AccommodationRequirement req) {
        if (req == null || req.getRooms() == null || req.getRooms().isEmpty()) {
            return """
              <tr style="background-color: #FFFFFF;">
                <td colspan="2" style="padding: 12px 14px; font-family: 'Calibri Light', Calibri, sans-serif; color: #7D6B5C; font-style: italic;">
                  Standard room configuration requested
                </td>
              </tr>
            """;
        }

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (RoomRequirement r : req.getRooms()) {
            String typeName = r.getRoomType() != null && r.getRoomType().getName() != null
                    ? escapeHtml(r.getRoomType().getName())
                    : "Standard Room";
            int qty = r.getQuantity() != null ? r.getQuantity() : 1;
            String bg = (idx++ % 2 == 0) ? "#FFFFFF" : "#FAF8F4";
            sb.append(String.format(
                    "<tr style=\"background-color: %s;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; font-weight: bold; color: #1F1916;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; text-align: center; font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #264624;\">%d</td></tr>\n",
                    bg, typeName, qty
            ));
        }
        return sb.toString();
    }

    private String buildRoomPreferencesHtml(AccommodationRequirement req) {
        if (req == null || req.getRoomPreferences() == null || req.getRoomPreferences().isBlank()) {
            return "";
        }
        return String.format(
                "<div style=\"margin-top: 8px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #4A3A2F; background-color: #FAF8F4; border: 1px solid #E2D9CC; border-radius: 6px; padding: 8px 12px;\"><strong style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;\">Bedding / Room Preferences:</strong> %s</div>",
                escapeHtml(req.getRoomPreferences().trim())
        );
    }

    private String buildGuestRowsHtml(List<Guest> guests, Client client) {
        StringBuilder sb = new StringBuilder();
        if (guests != null && !guests.isEmpty()) {
            int idx = 1;
            for (Guest g : guests) {
                String name = escapeHtml(g.getFirstName() + " " + g.getLastName());
                String gender = g.getGender() != null ? escapeHtml(g.getGender().name()) : "-";
                String nationality = (g.getNationality() != null && !g.getNationality().isBlank())
                        ? escapeHtml(g.getNationality())
                        : "-";
                String leadBadge = (idx == 1)
                        ? "<span style=\"display: inline-block; background-color: #EDE7DC; color: #5C3D1E; border: 1px solid #D8CBB6; font-family: Arial, Helvetica, sans-serif; font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; margin-left: 6px;\">LEAD GUEST</span>"
                        : "";
                String bg = (idx % 2 == 1) ? "#FFFFFF" : "#FAF8F4";
                sb.append(String.format(
                        "<tr style=\"background-color: %s;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; text-align: center; font-family: Arial, sans-serif; color: #7D6B5C;\">%d</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; font-weight: bold; color: #1F1916;\">%s%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; color: #4A3A2F;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; color: #4A3A2F;\">%s</td></tr>\n",
                        bg, idx, name, leadBadge, gender, nationality
                ));
                idx++;
            }
        } else if (client != null) {
            String name = escapeHtml(client.getFirstName() + " " + client.getLastName());
            String gender = client.getGender() != null ? escapeHtml(client.getGender().name()) : "-";
            String nationality = (client.getNationality() != null && !client.getNationality().isBlank())
                    ? escapeHtml(client.getNationality())
                    : "-";
            sb.append(String.format(
                    "<tr style=\"background-color: #FFFFFF;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; text-align: center; font-family: Arial, sans-serif; color: #7D6B5C;\">1</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; font-weight: bold; color: #1F1916;\">%s <span style=\"display: inline-block; background-color: #EDE7DC; color: #5C3D1E; border: 1px solid #D8CBB6; font-family: Arial, Helvetica, sans-serif; font-size: 10px; font-weight: bold; padding: 2px 6px; border-radius: 4px; margin-left: 6px;\">LEAD GUEST</span></td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; color: #4A3A2F;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; color: #4A3A2F;\">%s</td></tr>\n",
                    name, gender, nationality
            ));
        } else {
            sb.append("""
              <tr style="background-color: #FFFFFF;">
                <td colspan="4" style="padding: 12px 14px; font-family: 'Calibri Light', Calibri, sans-serif; color: #7D6B5C; font-style: italic; text-align: center;">
                  Guest manifest details will be provided prior to arrival
                </td>
              </tr>
            """);
        }
        return sb.toString();
    }

    private String buildSpecialRequestsHtml(AccommodationRequirement req, AccommodationBooking booking) {
        boolean hasSpecial = req != null && req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank();
        boolean hasNotes = booking.getNotes() != null && !booking.getNotes().isBlank();
        if (!hasSpecial && !hasNotes) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 24px; border: 1px solid #E5D5C0; border-left: 5px solid #7A5229; border-radius: 8px; background-color: #FAF6F0;\">");
        sb.append("<tr><td style=\"padding: 16px 20px;\">");
        sb.append("<div style=\"font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #7A5229; text-transform: uppercase; letter-spacing: 0.6px; margin-bottom: 6px;\">SPECIAL REQUESTS &amp; DIETARY REQUIREMENTS</div>");

        if (hasSpecial) {
            sb.append("<div style=\"font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #4A3525; line-height: 1.55; margin-bottom: ").append(hasNotes ? "8px;" : "0;").append("\">")
              .append(escapeHtml(req.getSpecialRequests().trim()))
              .append("</div>");
        }

        if (hasNotes) {
            sb.append("<div style=\"font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #6B4E38; line-height: 1.5; font-style: italic;\"><strong style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #4A3525;\">Additional Internal Notes:</strong> ")
              .append(escapeHtml(booking.getNotes().trim()))
              .append("</div>");
        }

        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private String buildConsultantSignatureHtml(User consultant, Company company, String systemEmail) {
        String name = (consultant != null)
                ? escapeHtml(consultant.getFirstName() + " " + consultant.getLastName())
                : "Reservations Operations Desk";
        String title = (consultant != null && consultant.getRole() != null)
                ? escapeHtml(consultant.getRole().getName().replace("_", " "))
                : "Reservation Operations Consultant";
        String compName = (company != null && company.getName() != null)
                ? escapeHtml(company.getName())
                : "Safari Operations Platform";
        String phoneStr = (consultant != null && consultant.getPhone() != null && !consultant.getPhone().isBlank())
                ? " &bull; Phone: <strong style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #1F1916;\">" + escapeHtml(consultant.getPhone()) + "</strong>"
                : "";

        return String.format("""
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-top: 1px solid #E2D9CC; padding-top: 18px; margin-top: 6px;">
              <tr>
                <td valign="top">
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 14px; font-weight: bold; color: #1F1916;">%s</div>
                  <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #6B5B4E; margin-top: 2px;">%s &bull; %s</div>
                  <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #4A3A2F; margin-top: 6px;">
                    Inquiries / Direct Reply: <a href="mailto:%s" style="color: #264624; text-decoration: none; font-weight: bold;">%s</a>%s
                  </div>
                </td>
              </tr>
            </table>
            """,
            name, title, compName, escapeHtml(systemEmail), escapeHtml(systemEmail), phoneStr
        );
    }

    private String buildRoomsPlainText(AccommodationRequirement req) {
        if (req == null || req.getRooms() == null || req.getRooms().isEmpty()) {
            return "   - Standard room configuration requested";
        }
        StringBuilder sb = new StringBuilder();
        for (RoomRequirement r : req.getRooms()) {
            String typeName = r.getRoomType() != null && r.getRoomType().getName() != null
                    ? r.getRoomType().getName()
                    : "Standard Room";
            int qty = r.getQuantity() != null ? r.getQuantity() : 1;
            sb.append("   - ").append(typeName).append(": ").append(qty).append(" Room(s)\n");
        }
        return sb.toString().trim();
    }

    private String buildGuestsPlainText(List<Guest> guests, Client client) {
        if (guests != null && !guests.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int idx = 1;
            for (Guest g : guests) {
                sb.append("   ").append(idx).append(". ").append(g.getFirstName()).append(" ").append(g.getLastName());
                if (idx == 1) sb.append(" (Lead Guest)");
                if (g.getGender() != null) sb.append(" [").append(g.getGender().name()).append("]");
                if (g.getNationality() != null && !g.getNationality().isBlank()) {
                    sb.append(" - ").append(g.getNationality());
                }
                sb.append("\n");
                idx++;
            }
            return sb.toString().trim();
        } else if (client != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("   1. ").append(client.getFirstName()).append(" ").append(client.getLastName()).append(" (Lead Guest)");
            if (client.getGender() != null) sb.append(" [").append(client.getGender().name()).append("]");
            if (client.getNationality() != null && !client.getNationality().isBlank()) {
                sb.append(" - ").append(client.getNationality());
            }
            return sb.toString();
        }
        return "   - Guest details to follow prior to arrival";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
