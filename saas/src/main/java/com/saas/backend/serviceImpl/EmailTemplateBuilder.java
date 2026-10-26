package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saas.backend.dto.UnpaidAccommodationAlertDto;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.Client;
import com.saas.backend.models.Company;
import com.saas.backend.models.Guest;
import com.saas.backend.models.GuestRequirement;
import com.saas.backend.models.RequirementType;
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

    public String buildBookingHtml(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        return buildBookingHtml(booking, guests, Collections.emptyMap(), systemEmail);
    }

    /**
     * Builds responsive HTML email for booking request with full guest requirements.
     */
    public String buildBookingHtml(AccommodationBooking booking, List<Guest> guests, Map<UUID, List<GuestRequirement>> guestRequirements, String systemEmail) {
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
        String guestRowsHtml = buildGuestRowsHtml(guests, client, guestRequirements);
        String specialRequestsBlock = buildSpecialRequestsHtml(req, booking, guestRequirements, guests);
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
  body { margin: 0; padding: 0; min-width: 100%%; width: 100%% !important; height: 100%% !important; background-color: #F8FAFC; -webkit-font-smoothing: antialiased; }
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
<body style="margin: 0; padding: 24px 0; background-color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #0F172A; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); border: 1px solid #E2E8F0; text-align: left;">
      <!-- TOP NAVY ACCENT STRIPE -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="4" style="background-color: #0F172A;"></td>
        </tr>
      </table>

      <!-- EXECUTIVE CLEAN HEADER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 28px; border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
        <tr>
          <td>
            <div style="font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1.2px; color: #64748B; margin-bottom: 4px;">
              RESERVATION OPERATIONS DESK
            </div>
            <h1 style="margin: 0 0 12px; font-size: 20px; font-weight: 700; color: #0F172A; letter-spacing: -0.2px; line-height: 1.3;">
              ACCOMMODATION RESERVATION REQUEST
            </h1>
            <table cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td style="background-color: #F1F5F9; border: 1px solid #CBD5E1; border-radius: 4px; padding: 4px 10px;">
                  <span style="font-size: 12px; font-weight: 700; color: #1E293B; letter-spacing: 0.3px;">
                    REF: %s
                  </span>
                </td>
                <td style="padding-left: 12px;">
                  <span style="font-size: 12px; color: #64748B;">
                    Dispatched: %s
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>

      <!-- MAIN CONTENT -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 28px;" class="mobile-padding">
        <tr>
          <td>
            <!-- SALUTATION -->
            <p style="margin: 0 0 12px; font-size: 14px; color: #0F172A; line-height: 1.6;">
              Dear Reservations Team at <strong style="color: #0F172A;">%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-size: 13px; color: #475569; line-height: 1.6;">
              Please accept our official accommodation reservation request for our upcoming safari party. Kindly review availability and confirm space for the dates and room configuration detailed below:
            </p>

            <!-- SECTION 1: STAY OVERVIEW CARDS -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden; background-color: #F8FAFC;">
              <tr>
                <td style="padding: 10px 14px; background-color: #F1F5F9; border-bottom: 1px solid #E2E8F0; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px;">
                  STAY OVERVIEW &amp; ITINERARY PARAMETERS
                </td>
              </tr>
              <tr>
                <td style="padding: 14px 16px;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; 
                        margin-bottom: 2px;">Check-In Date</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Check-Out Date</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Duration</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Total Requirements</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s Room(s) &bull; %s Guest(s)</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Safari Reference</div>
                        <div style="font-size: 13px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Itinerary Schedule</div>
                        <div style="font-size: 13px; color: #334155;">%s</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>

            <!-- SECTION 2: ROOM REQUIREMENTS -->
            <div style="margin-bottom: 24px;">
              <div style="font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;">
                ROOM ALLOCATION &amp; REQUIREMENTS
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #F1F5F9; text-align: left; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0;">Room Category</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: center; width: 90px;">Quantity</th>
                  </tr>
                </thead>
                <tbody style="font-size: 13px; color: #0F172A;">
                  %s
                </tbody>
              </table>
              %s
            </div>

            <!-- SECTION 3: GUEST MANIFEST & SPECIAL REQUESTS -->
            <div style="margin-bottom: 24px;">
              <div style="font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;">
                GUEST MANIFEST &amp; SPECIAL REQUIREMENTS
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #F1F5F9; text-align: left; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; width: 32px; text-align: center;">#</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0;">Guest Details &amp; Special Needs</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; width: 80px;">Gender</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; width: 110px;">Nationality</th>
                  </tr>
                </thead>
                <tbody style="font-size: 13px; color: #0F172A;">
                  %s
                </tbody>
              </table>
            </div>

            <!-- SECTION 4: SPECIAL REQUESTS & NOTES -->
            %s

            <!-- SECTION 5: ACTION REQUIRED & PROTOCOL -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E2E8F0; border-radius: 6px; background-color: #F8FAFC;">
              <tr>
                <td style="padding: 14px 18px;">
                  <div style="font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px;">
                    ACTION REQUIRED: CONFIRMATION PROTOCOL
                  </div>
                  <ol style="margin: 0; padding-left: 18px; font-size: 12px; color: #475569; line-height: 1.6;">
                    <li style="margin-bottom: 4px;">Confirm room availability and applicable contracted rate.</li>
                    <li style="margin-bottom: 4px;">Provide your property reservation confirmation number.</li>
                    <li style="margin-bottom: 4px;">Advise meal plan details, cancellation terms, and payment release deadlines.</li>
                    <li>Please maintain '<span style="font-family: monospace; background-color: #F1F5F9; color: #0F172A; padding: 2px 6px; border-radius: 4px; font-weight: 700; border: 1px solid #CBD5E1;">%s</span>' in the email subject line for automated tracking.</li>
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
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #F8FAFC; border-top: 1px solid #E2E8F0; padding: 16px 28px; color: #64748B; font-size: 11px; text-align: center;" class="mobile-padding">
        <tr>
          <td>
            <div style="font-weight: 600; color: #334155; margin-bottom: 2px;">SAFARI OPERATIONS RESERVATIONS DESK</div>
            <div>Automated Reservation Request &bull; Booking Ref: %s &bull; Generated: %s</div>
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

    public String buildBookingPlainText(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        return buildBookingPlainText(booking, guests, Collections.emptyMap(), systemEmail);
    }

    /**
     * Builds structured plain-text fallback for booking request with full guest requirements.
     */
    public String buildBookingPlainText(AccommodationBooking booking, List<Guest> guests, Map<UUID, List<GuestRequirement>> guestRequirements, String systemEmail) {
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
        String guestsText = buildGuestsPlainText(guests, client, guestRequirements);

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

        sb.append("3. GUEST MANIFEST & SPECIAL NEEDS:\n");
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

    public String buildReminderHtml(AccommodationBooking booking, List<Guest> guests, String systemEmail) {
        return buildReminderHtml(booking, guests, Collections.emptyMap(), systemEmail);
    }

    /**
     * Builds responsive HTML email for booking reminder/follow-up in clean executive minimal style.
     */
    public String buildReminderHtml(AccommodationBooking booking, List<Guest> guests, Map<UUID, List<GuestRequirement>> guestRequirements, String systemEmail) {
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
  body { margin: 0; padding: 0; min-width: 100%%; width: 100%% !important; height: 100%% !important; background-color: #F8FAFC; -webkit-font-smoothing: antialiased; }
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
<body style="margin: 0; padding: 24px 0; background-color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #0F172A; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); border: 1px solid #E2E8F0; text-align: left;">
      <!-- TOP NAVY ACCENT STRIPE -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="4" style="background-color: #0F172A;"></td>
        </tr>
      </table>

      <!-- EXECUTIVE CLEAN HEADER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 28px; border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
        <tr>
          <td>
            <div style="font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1.2px; color: #64748B; margin-bottom: 4px;">
              FOLLOW-UP NOTIFICATION &bull; PENDING CONFIRMATION
            </div>
            <h1 style="margin: 0 0 12px; font-size: 20px; font-weight: 700; color: #0F172A; letter-spacing: -0.2px; line-height: 1.3;">
              RESERVATION REQUEST FOLLOW-UP
            </h1>
            <table cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td style="background-color: #F1F5F9; border: 1px solid #CBD5E1; border-radius: 4px; padding: 4px 10px;">
                  <span style="font-size: 12px; font-weight: 700; color: #1E293B; letter-spacing: 0.3px;">
                    REF: %s
                  </span>
                </td>
                <td style="padding-left: 12px;">
                  <span style="font-size: 12px; color: #64748B;">
                    Dispatched: %s
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>

      <!-- MAIN CONTENT -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 28px;" class="mobile-padding">
        <tr>
          <td>
            <p style="margin: 0 0 12px; font-size: 14px; color: #0F172A; line-height: 1.6;">
              Dear Reservations Team at <strong style="color: #0F172A;">%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-size: 13px; color: #475569; line-height: 1.6;">
              We are following up on our accommodation reservation request submitted previously. We have not yet received confirmation of room availability for this booking. Kindly review and confirm space for the parameters below:
            </p>

            <!-- REMINDER SUMMARY CARD -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden; background-color: #F8FAFC;">
              <tr>
                <td style="padding: 10px 14px; background-color: #F1F5F9; border-bottom: 1px solid #E2E8F0; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px;">
                  PENDING RESERVATION PARAMETERS
                </td>
              </tr>
              <tr>
                <td style="padding: 14px 16px;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Check-In Date</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px; padding-bottom: 12px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Check-Out Date</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                    </tr>
                    <tr>
                      <td width="50%%" valign="top" style="padding-right: 10px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Duration</div>
                        <div style="font-size: 14px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-left: 10px;">
                        <div style="font-size: 10px; text-transform: uppercase; color: #64748B; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 2px;">Safari Reference</div>
                        <div style="font-size: 13px; font-weight: 600; color: #0F172A;">%s</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>

            <!-- ROOM TABLE -->
            <div style="margin-bottom: 24px;">
              <div style="font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;">
                REQUESTED ROOMS
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border: 1px solid #E2E8F0; border-radius: 6px; overflow: hidden;">
                <thead>
                  <tr style="background-color: #F1F5F9; text-align: left; font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px;">
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0;">Room Category</th>
                    <th style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: center; width: 90px;">Quantity</th>
                  </tr>
                </thead>
                <tbody style="font-size: 13px; color: #0F172A;">
                  %s
                </tbody>
              </table>
            </div>

            <!-- ACTION REQUIRED BOX -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 24px; border: 1px solid #CBD5E1; border-left: 4px solid #0F172A; border-radius: 6px; background-color: #F8FAFC;">
              <tr>
                <td style="padding: 14px 18px;">
                  <div style="font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px;">
                    ACTION REQUIRED: CONFIRM AVAILABILITY
                  </div>
                  <p style="margin: 0; font-size: 12px; color: #475569; line-height: 1.6;">
                    Please reply to this email directly confirming availability or advising if alternative dates/rooms are available. Kindly maintain '<span style="font-family: monospace; background-color: #F1F5F9; color: #0F172A; padding: 2px 6px; border-radius: 4px; font-weight: 700; border: 1px solid #CBD5E1;">%s</span>' in the subject line.
                  </p>
                </td>
              </tr>
            </table>

            %s
          </td>
        </tr>
      </table>

      <!-- FOOTER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #F8FAFC; border-top: 1px solid #E2E8F0; padding: 16px 28px; color: #64748B; font-size: 11px; text-align: center;" class="mobile-padding">
        <tr>
          <td>
            <div style="font-weight: 600; color: #334155; margin-bottom: 2px;">SAFARI OPERATIONS RESERVATIONS DESK</div>
            <div>Follow-up Reminder &bull; Reference: %s &bull; Dispatched: %s</div>
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
        return buildReminderPlainText(booking, guests, Collections.emptyMap(), systemEmail);
    }

    /**
     * Builds structured plain-text fallback for reminder email with guest requirements.
     */
    public String buildReminderPlainText(AccommodationBooking booking, List<Guest> guests, Map<UUID, List<GuestRequirement>> guestRequirements, String systemEmail) {
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
        String guestsText = buildGuestsPlainText(guests, client, guestRequirements);

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
        sb.append("GUEST MANIFEST & SPECIAL NEEDS:\n");
        sb.append(guestsText).append("\n\n");
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
                <td colspan="2" style="padding: 12px 14px; font-size: 13px; color: #64748B; font-style: italic;">
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
            String bg = (idx++ % 2 == 0) ? "#FFFFFF" : "#F8FAFC";
            sb.append(String.format(
                    "<tr style=\"background-color: %s;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-weight: 600; color: #0F172A;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: center; font-weight: 700; color: #0F172A;\">%d</td></tr>\n",
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
                "<div style=\"margin-top: 8px; font-size: 12px; color: #334155; background-color: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; padding: 8px 12px;\"><strong style=\"font-weight: 600; color: #0F172A;\">Bedding / Room Preferences:</strong> %s</div>",
                escapeHtml(req.getRoomPreferences().trim())
        );
    }

    private String buildGuestRowsHtml(List<Guest> guests, Client client) {
        return buildGuestRowsHtml(guests, client, Collections.emptyMap());
    }

    private String buildGuestRowsHtml(List<Guest> guests, Client client, Map<UUID, List<GuestRequirement>> guestRequirements) {
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
                        ? "<span style=\"display: inline-block; background-color: #F1F5F9; color: #334155; border: 1px solid #CBD5E1; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; margin-left: 6px;\">LEAD GUEST</span>"
                        : "";

                StringBuilder reqBadges = new StringBuilder();
                if (guestRequirements != null && g.getId() != null && guestRequirements.containsKey(g.getId())) {
                    List<GuestRequirement> gReqs = guestRequirements.get(g.getId());
                    if (gReqs != null && !gReqs.isEmpty()) {
                        reqBadges.append("<div style=\"margin-top: 4px;\">");
                        for (GuestRequirement gr : gReqs) {
                            String type = gr.getRequirementType() != null ? gr.getRequirementType().name() : "SPECIAL";
                            String val = gr.getRequirementValue() != null ? escapeHtml(gr.getRequirementValue()) : "";
                            String notes = (gr.getNotes() != null && !gr.getNotes().isBlank()) ? " (" + escapeHtml(gr.getNotes().trim()) + ")" : "";

                            String bg = "#FEF2F2";
                            String text = "#991B1B";
                            String border = "#FECACA";
                            if (gr.getRequirementType() == RequirementType.ROOM || gr.getRequirementType() == RequirementType.OTHER) {
                                bg = "#F1F5F9";
                                text = "#334155";
                                border = "#CBD5E1";
                            }
                            reqBadges.append(String.format(
                                "<span style=\"display: inline-block; background-color: %s; color: %s; border: 1px solid %s; font-size: 10px; font-weight: 600; padding: 2px 6px; border-radius: 3px; margin-right: 4px; margin-top: 2px;\">%s: %s%s</span>",
                                bg, text, border, type, val, notes
                            ));
                        }
                        reqBadges.append("</div>");
                    }
                }

                String bg = (idx % 2 == 1) ? "#FFFFFF" : "#F8FAFC";
                sb.append(String.format(
                        "<tr style=\"background-color: %s;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: center; color: #64748B;\">%d</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-weight: 600; color: #0F172A;\">%s%s%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; color: #475569;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; color: #475569;\">%s</td></tr>\n",
                        bg, idx, name, leadBadge, reqBadges.toString(), gender, nationality
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
                    "<tr style=\"background-color: #FFFFFF;\"><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: center; color: #64748B;\">1</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-weight: 600; color: #0F172A;\">%s <span style=\"display: inline-block; background-color: #F1F5F9; color: #334155; border: 1px solid #CBD5E1; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; margin-left: 6px;\">LEAD GUEST</span></td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; color: #475569;\">%s</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #E2E8F0; color: #475569;\">%s</td></tr>\n",
                    name, gender, nationality
            ));
        } else {
            sb.append("""
              <tr style="background-color: #FFFFFF;">
                <td colspan="4" style="padding: 12px 14px; font-size: 13px; color: #64748B; font-style: italic; text-align: center;">
                  Guest manifest details will be provided prior to arrival
                </td>
              </tr>
            """);
        }
        return sb.toString();
    }

    private String buildSpecialRequestsHtml(AccommodationRequirement req, AccommodationBooking booking) {
        return buildSpecialRequestsHtml(req, booking, Collections.emptyMap(), Collections.emptyList());
    }

    private String buildSpecialRequestsHtml(AccommodationRequirement req, AccommodationBooking booking, Map<UUID, List<GuestRequirement>> guestRequirements, List<Guest> guests) {
        boolean hasReqSpecial = req != null && req.getSpecialRequests() != null && !req.getSpecialRequests().isBlank();
        boolean hasNotes = booking != null && booking.getNotes() != null && !booking.getNotes().isBlank();

        boolean hasGuestReqs = false;
        if (guestRequirements != null && !guestRequirements.isEmpty()) {
            for (List<GuestRequirement> list : guestRequirements.values()) {
                if (list != null && !list.isEmpty()) {
                    hasGuestReqs = true;
                    break;
                }
            }
        }

        if (!hasReqSpecial && !hasNotes && !hasGuestReqs) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 24px; border: 1px solid #E2E8F0; border-left: 4px solid #0F172A; border-radius: 6px; background-color: #F8FAFC;\">");
        sb.append("<tr><td style=\"padding: 14px 18px;\">");
        sb.append("<div style=\"font-size: 11px; font-weight: 700; color: #0F172A; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px;\">SPECIAL REQUESTS &amp; DIETARY / MEDICAL REQUIREMENTS</div>");

        if (hasReqSpecial) {
            sb.append("<div style=\"font-size: 13px; color: #334155; line-height: 1.55; margin-bottom: ").append((hasNotes || hasGuestReqs) ? "8px;" : "0;").append("\">")
              .append("<strong style=\"color: #0F172A;\">Party Special Requests:</strong> ")
              .append(escapeHtml(req.getSpecialRequests().trim()))
              .append("</div>");
        }

        if (hasGuestReqs && guests != null) {
            sb.append("<div style=\"margin-bottom: ").append(hasNotes ? "8px;" : "0;").append(";\">");
            for (Guest g : guests) {
                if (g.getId() != null && guestRequirements.containsKey(g.getId())) {
                    List<GuestRequirement> grList = guestRequirements.get(g.getId());
                    if (grList != null && !grList.isEmpty()) {
                        sb.append("<div style=\"font-size: 12px; color: #0F172A; margin-bottom: 4px;\">");
                        sb.append("<strong style=\"color: #0F172A;\">&bull; ").append(escapeHtml(g.getFirstName())).append(" ").append(escapeHtml(g.getLastName())).append(":</strong> ");
                        List<String> items = new ArrayList<>();
                        for (GuestRequirement gr : grList) {
                            String item = (gr.getRequirementType() != null ? gr.getRequirementType().name() + " - " : "") +
                                          (gr.getRequirementValue() != null ? gr.getRequirementValue() : "") +
                                          (gr.getNotes() != null && !gr.getNotes().isBlank() ? " (" + gr.getNotes().trim() + ")" : "");
                            items.add(escapeHtml(item));
                        }
                        sb.append(String.join("; ", items));
                        sb.append("</div>");
                    }
                }
            }
            sb.append("</div>");
        }

        if (hasNotes) {
            sb.append("<div style=\"font-size: 12px; color: #64748B; line-height: 1.5; font-style: italic;\"><strong style=\"color: #334155; font-style: normal;\">Booking Notes:</strong> ")
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
                ? " &bull; Phone: <strong style=\"color: #0F172A;\">" + escapeHtml(consultant.getPhone()) + "</strong>"
                : "";

        return String.format("""
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-top: 1px solid #E2E8F0; padding-top: 16px; margin-top: 8px;">
              <tr>
                <td valign="top">
                  <div style="font-size: 13px; font-weight: 700; color: #0F172A;">%s</div>
                  <div style="font-size: 12px; color: #64748B; margin-top: 2px;">%s &bull; %s</div>
                  <div style="font-size: 12px; color: #475569; margin-top: 4px;">
                    Inquiries / Direct Reply: <a href="mailto:%s" style="color: #0284C7; text-decoration: none; font-weight: 600;">%s</a>%s
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
        return buildGuestsPlainText(guests, client, Collections.emptyMap());
    }

    private String buildGuestsPlainText(List<Guest> guests, Client client, Map<UUID, List<GuestRequirement>> guestRequirements) {
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
                if (guestRequirements != null && g.getId() != null && guestRequirements.containsKey(g.getId())) {
                    List<GuestRequirement> grList = guestRequirements.get(g.getId());
                    if (grList != null && !grList.isEmpty()) {
                        sb.append("\n      * Special Needs: ");
                        List<String> rStrs = new ArrayList<>();
                        for (GuestRequirement gr : grList) {
                            String item = (gr.getRequirementType() != null ? gr.getRequirementType().name() + ": " : "") +
                                          (gr.getRequirementValue() != null ? gr.getRequirementValue() : "") +
                                          (gr.getNotes() != null && !gr.getNotes().isBlank() ? " (" + gr.getNotes().trim() + ")" : "");
                            rStrs.add(item);
                        }
                        sb.append(String.join(", ", rStrs));
                    }
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

    /**
     * Builds responsive HTML email for Sales Person payment deadline reminders.
     */
    public String buildSalesPaymentAlertHtml(User salesPerson, List<UnpaidAccommodationAlertDto> alerts, String systemEmail) {
        String consultantName = salesPerson != null && salesPerson.getFirstName() != null
                ? escapeHtml(salesPerson.getFirstName())
                : "Sales Consultant";
        int totalAlerts = alerts != null ? alerts.size() : 0;
        String timestampStr = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);

        StringBuilder cardsHtml = new StringBuilder();
        if (alerts != null) {
            for (UnpaidAccommodationAlertDto alert : alerts) {
                String badgeBg = "#FEF3C7";
                String badgeText = "#92400E";
                String badgeBorder = "#FCD34D";
                String urgencyLabel = "EXPIRING SOON";

                if ("OVERDUE".equalsIgnoreCase(alert.getUrgency())) {
                    badgeBg = "#FFE4E6";
                    badgeText = "#9F1239";
                    badgeBorder = "#FECDD3";
                    urgencyLabel = alert.getDaysRemaining() != null && alert.getDaysRemaining() < 0
                            ? "OVERDUE (" + Math.abs(alert.getDaysRemaining()) + " DAYS LATE)"
                            : "OVERDUE";
                } else if ("DUE_IN_1_DAY".equalsIgnoreCase(alert.getUrgency())) {
                    badgeBg = "#FFEDD5";
                    badgeText = "#9A3412";
                    badgeBorder = "#FED7AA";
                    urgencyLabel = "EXPIRING TOMORROW";
                } else if ("DUE_IN_3_DAYS".equalsIgnoreCase(alert.getUrgency())) {
                    badgeBg = "#FEF3C7";
                    badgeText = "#92400E";
                    badgeBorder = "#FDE68A";
                    urgencyLabel = "DUE IN 3 DAYS";
                } else if ("UPCOMING_SAFARI_UNPAID".equalsIgnoreCase(alert.getUrgency())) {
                    badgeBg = "#FEE2E2";
                    badgeText = "#B91C1C";
                    badgeBorder = "#FCA5A5";
                    urgencyLabel = "SAFARI IMMINENT (UNPAID)";
                }

                String amountStr = alert.getAmount() != null
                        ? (escapeHtml(alert.getCurrency() != null ? alert.getCurrency() : "USD") + " " + String.format(Locale.US, "%,.2f", alert.getAmount()))
                        : "Invoice Pending";
                String dueDateStr = alert.getDueDate() != null ? alert.getDueDate().format(DATE_FORMATTER) : "Check-in: " + (alert.getCheckInDate() != null ? alert.getCheckInDate().format(DATE_FORMATTER) : "TBD");

                cardsHtml.append("""
                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 18px; border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden; background-color: #FAF8F4;">
                      <tr>
                        <td style="padding: 10px 16px; background-color: #EDE7DC; border-bottom: 1px solid #E2D9CC;">
                          <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                              <td>
                                <strong style="font-family: Arial, Helvetica, sans-serif; font-size: 13px; color: #1F1916;">
                                  Client: %s
                                </strong>
                                <span style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 12px; color: #7A5229; margin-left: 8px;">
                                  (Safari: %s)
                                </span>
                              </td>
                              <td align="right">
                                <span style="display: inline-block; padding: 3px 10px; border-radius: 12px; font-family: Arial, Helvetica, sans-serif; font-size: 10px; font-weight: bold; background-color: %s; color: %s; border: 1px solid %s; letter-spacing: 0.5px;">
                                  %s
                                </span>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 14px 16px;">
                          <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                              <td width="50%%" valign="top" style="padding-bottom: 8px;">
                                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Property / Lodge</div>
                                <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; font-weight: bold; color: #264624;">%s</div>
                              </td>
                              <td width="50%%" valign="top" style="padding-bottom: 8px;">
                                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Booking Ref</div>
                                <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #1F1916;">%s</div>
                              </td>
                            </tr>
                            <tr>
                              <td width="50%%" valign="top">
                                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Amount Due</div>
                                <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px; font-weight: bold; color: #7A5229;">%s</div>
                              </td>
                              <td width="50%%" valign="top">
                                <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Due Date</div>
                                <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; font-weight: bold; color: #1F1916;">%s</div>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                """.formatted(
                        escapeHtml(alert.getClientName() != null ? alert.getClientName() : "Safari Traveler"),
                        escapeHtml(alert.getSafariReference() != null ? alert.getSafariReference() : "N/A"),
                        badgeBg, badgeText, badgeBorder, urgencyLabel,
                        escapeHtml(alert.getPropertyName() != null ? alert.getPropertyName() : "Lodge"),
                        escapeHtml(alert.getBookingReference() != null ? alert.getBookingReference() : "REF"),
                        amountStr,
                        dueDateStr
                ));
            }
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Safari Payment Deadline Alert</title>
<style type="text/css">
  body { margin: 0; padding: 0; width: 100%% !important; background-color: #F5F2EB; -webkit-font-smoothing: antialiased; }
  table { border-spacing: 0; border-collapse: collapse; }
  td { padding: 0; }
  @media screen and (max-width: 600px) {
    .container { width: 100%% !important; }
    .mobile-padding { padding-left: 16px !important; padding-right: 16px !important; }
  }
</style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F5F2EB; font-family: 'Calibri Light', Calibri, sans-serif; color: #2D241E; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 16px rgba(45, 36, 30, 0.08); border: 1px solid #E2D9CC; text-align: left;">
      <!-- TOP WARM EARTH TONE STRIPE -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="5" style="background: linear-gradient(90deg, #7A5229 0%%, #B8860B 50%%, #7A5229 100%%); background-color: #7A5229;"></td>
        </tr>
      </table>

      <!-- HEADER BANNER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #264624; padding: 26px 32px; color: #FFFFFF;">
        <tr>
          <td>
            <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-transform: uppercase; font-size: 11px; letter-spacing: 1.5px; color: #D5E3D2; margin-bottom: 6px;">
              SAFARI SALES OS &bull; PAYMENT NOTIFICATION
            </div>
            <h1 style="margin: 0; font-family: Arial, Helvetica, sans-serif; font-size: 20px; font-weight: bold; color: #FFFFFF;">
              PAYMENT DEADLINE ALERT
            </h1>
            <div style="margin-top: 8px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #D5E3D2;">
              %d accommodation(s) require payment attention &bull; Generated: %s
            </div>
          </td>
        </tr>
      </table>

      <!-- MAIN CONTENT -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 32px;" class="mobile-padding">
        <tr>
          <td>
            <p style="margin: 0 0 14px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px; color: #2D241E;">
              Hi <strong>%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; color: #4A3A2F;">
              The following lodge accommodation reservation(s) for your safari clients have upcoming or overdue payment deadlines. Missing payment deadlines risks accommodation cancellations by partner properties.
            </p>

            <!-- ALERT CARDS -->
            %s

            <!-- ADVICE BLOCK -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top: 10px; margin-bottom: 24px; background-color: #F8F9FA; border-left: 4px solid #264624; border-radius: 4px; padding: 14px 16px;">
              <tr>
                <td>
                  <strong style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #264624; text-transform: uppercase;">Next Operational Steps:</strong>
                  <ul style="margin: 6px 0 0; padding-left: 18px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #4A3A2F; line-height: 1.6;">
                    <li>Follow up with your <strong>Reservation Desk</strong> to verify lodge invoice arrival and bank coordinates.</li>
                    <li>If already paid, ensure the payment receipt reference is logged into the system.</li>
                  </ul>
                </td>
              </tr>
            </table>

            <!-- FOOTER -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-top: 1px solid #E2D9CC; padding-top: 16px;">
              <tr>
                <td>
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #7A5229;">
                    SAFARI SALES OS &bull; AUTOMATED RESERVATIONS ENGINE
                  </div>
                  <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 11px; color: #7D6B5C; margin-top: 4px;">
                    This automated alert was dispatched by the system to safeguard your client bookings.
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </div>
  </center>
</body>
</html>
        """.formatted(
                totalAlerts, timestampStr, consultantName, cardsHtml.toString()
        );
    }

    /**
     * Builds plain text email for Sales Person payment deadline reminders.
     */
    public String buildSalesPaymentAlertPlainText(User salesPerson, List<UnpaidAccommodationAlertDto> alerts, String systemEmail) {
        String consultantName = salesPerson != null && salesPerson.getFirstName() != null
                ? salesPerson.getFirstName()
                : "Sales Consultant";
        StringBuilder sb = new StringBuilder();
        sb.append("SAFARI SALES OS - PAYMENT DEADLINE ALERT\n");
        sb.append("=========================================\n\n");
        sb.append("Hi ").append(consultantName).append(",\n\n");
        sb.append("You have ").append(alerts != null ? alerts.size() : 0)
          .append(" accommodation reservation(s) requiring payment attention:\n\n");

        if (alerts != null) {
            int idx = 1;
            for (UnpaidAccommodationAlertDto a : alerts) {
                sb.append(idx).append(". ").append(a.getClientName()).append(" - ").append(a.getPropertyName()).append("\n");
                sb.append("   Safari: ").append(a.getSafariReference()).append("\n");
                sb.append("   Booking Ref: ").append(a.getBookingReference()).append("\n");
                sb.append("   Amount: ").append(a.getCurrency() != null ? a.getCurrency() : "USD")
                  .append(" ").append(a.getAmount() != null ? a.getAmount().toString() : "Pending Invoice").append("\n");
                sb.append("   Due Date: ").append(a.getDueDate() != null ? a.getDueDate().toString() : "Check-in: " + a.getCheckInDate()).append("\n");
                sb.append("   Urgency: ").append(a.getUrgency()).append("\n\n");
                idx++;
            }
        }

        sb.append("Please contact your Reservation Desk to ensure payment is processed and accommodation secured.\n\n");
        sb.append("---\nSafari Sales OS Reservations Desk\n").append(systemEmail).append("\n");
        return sb.toString();
    }

    /**
     * Builds responsive HTML email for Reservation Manager daily grouped payment summary.
     */
    public String buildReservationManagerPaymentSummaryHtml(User rm, Map<User, List<UnpaidAccommodationAlertDto>> groupedAlerts, String systemEmail) {
        String rmName = rm != null && rm.getFirstName() != null ? escapeHtml(rm.getFirstName()) : "Reservation Manager";
        int totalExpiring = 0;
        int salesCount = groupedAlerts != null ? groupedAlerts.size() : 0;
        if (groupedAlerts != null) {
            for (List<UnpaidAccommodationAlertDto> list : groupedAlerts.values()) {
                if (list != null) totalExpiring += list.size();
            }
        }
        String timestampStr = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);

        StringBuilder sectionsHtml = new StringBuilder();
        if (groupedAlerts != null) {
            for (Map.Entry<User, List<UnpaidAccommodationAlertDto>> entry : groupedAlerts.entrySet()) {
                User consultant = entry.getKey();
                List<UnpaidAccommodationAlertDto> list = entry.getValue();
                String consultantName = consultant != null
                        ? escapeHtml((consultant.getFirstName() != null ? consultant.getFirstName() : "") + " " + (consultant.getLastName() != null ? consultant.getLastName() : "")).trim()
                        : "Unassigned Sales";
                int count = list != null ? list.size() : 0;

                sectionsHtml.append("""
                    <div style="margin-bottom: 20px; border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden;">
                      <div style="background-color: #EDE7DC; padding: 10px 16px; font-family: Arial, Helvetica, sans-serif; font-size: 13px; font-weight: bold; color: #1F1916;">
                        %s (%d accommodation%s)
                      </div>
                      <div style="padding: 12px 16px; background-color: #FAF8F4;">
                """.formatted(consultantName, count, count == 1 ? "" : "s"));

                if (list != null) {
                    for (UnpaidAccommodationAlertDto alert : list) {
                        String amountStr = alert.getAmount() != null
                                ? (alert.getCurrency() != null ? alert.getCurrency() : "USD") + " " + String.format(Locale.US, "%,.2f", alert.getAmount())
                                : "Pending";
                        String dueStr = alert.getDueDate() != null ? alert.getDueDate().format(DATE_FORMATTER) : "Check-in: " + (alert.getCheckInDate() != null ? alert.getCheckInDate().toString() : "TBD");

                        sectionsHtml.append("""
                            <div style="padding: 8px 0; border-bottom: 1px dashed #E2D9CC; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px;">
                              <strong style="color: #1F1916;">&bull; %s</strong> &mdash; %s
                              <span style="color: #7A5229; font-weight: bold; margin-left: 6px;">[%s]</span>
                              <div style="font-size: 12px; color: #7D6B5C; margin-left: 12px; margin-top: 2px;">
                                Due: %s &bull; Ref: %s &bull; <span style="font-weight: bold; color: #BE123C;">%s</span>
                              </div>
                            </div>
                        """.formatted(
                                escapeHtml(alert.getClientName() != null ? alert.getClientName() : "Client"),
                                escapeHtml(alert.getPropertyName() != null ? alert.getPropertyName() : "Lodge"),
                                amountStr,
                                dueStr,
                                escapeHtml(alert.getBookingReference() != null ? alert.getBookingReference() : "REF"),
                                escapeHtml(alert.getUrgency() != null ? alert.getUrgency().replace("_", " ") : "ALERT")
                        ));
                    }
                }

                sectionsHtml.append("</div></div>");
            }
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Daily Reservation Manager Summary</title>
<style type="text/css">
  body { margin: 0; padding: 0; width: 100%% !important; background-color: #F5F2EB; -webkit-font-smoothing: antialiased; }
  table { border-spacing: 0; border-collapse: collapse; }
  td { padding: 0; }
</style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F5F2EB; font-family: 'Calibri Light', Calibri, sans-serif; color: #2D241E; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 16px rgba(45, 36, 30, 0.08); border: 1px solid #E2D9CC; text-align: left;">
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr><td height="5" style="background: #7A5229;"></td></tr>
      </table>

      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #264624; padding: 26px 32px; color: #FFFFFF;">
        <tr>
          <td>
            <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-transform: uppercase; font-size: 11px; letter-spacing: 1.5px; color: #D5E3D2; margin-bottom: 6px;">
              RESERVATION MANAGER SUMMARY
            </div>
            <h1 style="margin: 0; font-family: Arial, Helvetica, sans-serif; font-size: 20px; font-weight: bold; color: #FFFFFF;">
              DAILY DEADLINE DIGEST
            </h1>
            <div style="margin-top: 8px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #D5E3D2;">
              %d accommodations expiring across %d sales consultants &bull; %s
            </div>
          </td>
        </tr>
      </table>

      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 32px;">
        <tr>
          <td>
            <p style="margin: 0 0 16px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px;">
              Good morning <strong>%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; color: #4A3A2F;">
              Here is the consolidated daily summary of unpaid accommodation bookings organized by sales consultant:
            </p>

            %s

            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-top: 1px solid #E2D9CC; padding-top: 16px; margin-top: 20px;">
              <tr>
                <td>
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #7A5229;">
                    SAFARI SALES OS &bull; CENTRAL RESERVATIONS DESK
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </div>
  </center>
</body>
</html>
        """.formatted(totalExpiring, salesCount, timestampStr, rmName, sectionsHtml.toString());
    }

    /**
     * Builds plain text email for Reservation Manager daily grouped payment summary.
     */
    public String buildReservationManagerPaymentSummaryPlainText(User rm, Map<User, List<UnpaidAccommodationAlertDto>> groupedAlerts, String systemEmail) {
        String rmName = rm != null && rm.getFirstName() != null ? rm.getFirstName() : "Reservation Manager";
        StringBuilder sb = new StringBuilder();
        sb.append("SAFARI SALES OS - DAILY RESERVATION MANAGER SUMMARY\n");
        sb.append("===================================================\n\n");
        sb.append("Good morning ").append(rmName).append(",\n\n");

        if (groupedAlerts != null) {
            for (Map.Entry<User, List<UnpaidAccommodationAlertDto>> entry : groupedAlerts.entrySet()) {
                User consultant = entry.getKey();
                String consultantName = consultant != null ? consultant.getFirstName() + " " + consultant.getLastName() : "Unassigned";
                List<UnpaidAccommodationAlertDto> list = entry.getValue();
                sb.append("CONSULTANT: ").append(consultantName).append(" (").append(list != null ? list.size() : 0).append(")\n");
                if (list != null) {
                    for (UnpaidAccommodationAlertDto a : list) {
                        sb.append(" - ").append(a.getClientName()).append(" @ ").append(a.getPropertyName())
                          .append(" | Due: ").append(a.getDueDate())
                          .append(" | ").append(a.getCurrency()).append(" ").append(a.getAmount())
                          .append(" [").append(a.getUrgency()).append("]\n");
                    }
                }
                sb.append("\n");
            }
        }
        sb.append("---\nCentral Reservations Desk\n").append(systemEmail).append("\n");
        return sb.toString();
    }

    /**
     * Builds responsive HTML email notification when a lodge replies to a reservation request.
     * Sent to both the Sales Person managing the safari and the Reservation Manager.
     */
    public String buildLodgeReplyAlertHtml(
            User recipient,
            AccommodationBooking booking,
            com.saas.backend.dto.IncomingMailMessage reply,
            com.saas.backend.dto.EmailAnalysisResult analysis,
            String systemEmail) {

        String recipientName = recipient != null && recipient.getFirstName() != null
                ? escapeHtml(recipient.getFirstName())
                : "Colleague";

        String propName = booking.getProperty() != null && booking.getProperty().getName() != null
                ? escapeHtml(booking.getProperty().getName())
                : "Partner Property";

        String bookingRef = escapeHtml(booking.getReferenceNumber());

        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        String safariRef = safari != null && safari.getReferenceNumber() != null ? escapeHtml(safari.getReferenceNumber()) : "N/A";

        Client client = safari != null ? safari.getClient() : null;
        String clientName = client != null
                ? escapeHtml(client.getFirstName() + " " + (client.getLastName() != null ? client.getLastName() : ""))
                : "Safari Client";

        String checkInStr = booking.getCheckIn() != null ? booking.getCheckIn().format(DATE_FORMATTER) : "TBD";
        String checkOutStr = booking.getCheckOut() != null ? booking.getCheckOut().format(DATE_FORMATTER) : "TBD";
        String nightsStr = calculateNights(booking);

        com.saas.backend.models.EmailResponseType type = analysis != null && analysis.getResponseType() != null
                ? analysis.getResponseType()
                : com.saas.backend.models.EmailResponseType.UNKNOWN;

        String badgeBg;
        String badgeText;
        String badgeBorder;
        String headerTitle;
        String statusLabel;

        switch (type) {
            case CONFIRMED:
                badgeBg = "#ECFDF5";
                badgeText = "#065F46";
                badgeBorder = "#A7F3D0";
                headerTitle = "LODGE CONFIRMED RESERVATION";
                statusLabel = "CONFIRMED";
                break;
            case DECLINED:
                badgeBg = "#FFF1F2";
                badgeText = "#9F1239";
                badgeBorder = "#FECDD3";
                headerTitle = "LODGE DECLINED AVAILABILITY";
                statusLabel = "DECLINED";
                break;
            case NEED_MORE_INFORMATION:
                badgeBg = "#FFFBEB";
                badgeText = "#92400E";
                badgeBorder = "#FDE68A";
                headerTitle = "LODGE REQUESTED CLARIFICATION";
                statusLabel = "ACTION REQUIRED";
                break;
            default:
                badgeBg = "#F1F5F9";
                badgeText = "#334155";
                badgeBorder = "#CBD5E1";
                headerTitle = "INCOMING LODGE EMAIL REPLY";
                statusLabel = "RECEIVED";
                break;
        }

        String confNumberStr = analysis != null && analysis.getConfirmationNumber() != null && !analysis.getConfirmationNumber().isBlank()
                ? escapeHtml(analysis.getConfirmationNumber())
                : (booking.getConfirmationNumber() != null ? escapeHtml(booking.getConfirmationNumber()) : "—");

        String replyBodyHtml = reply != null && reply.getBody() != null
                ? escapeHtml(reply.getBody()).replace("\n", "<br/>")
                : "<i>(No text content in reply)</i>";

        String fromEmailStr = reply != null && reply.getFrom() != null ? escapeHtml(reply.getFrom()) : "lodge";
        String subjectStr = reply != null && reply.getSubject() != null ? escapeHtml(reply.getSubject()) : bookingRef;
        String timestampStr = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);

        String attachmentNotice = "";
        if (reply != null && reply.getAttachments() != null && !reply.getAttachments().isEmpty()) {
            StringBuilder attSb = new StringBuilder();
            attSb.append("<div style=\"margin-top: 14px; padding: 12px 14px; background-color: #F0FDF4; border: 1px solid #BBF7D0; border-radius: 6px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #166534;\">");
            attSb.append("<strong style=\"font-family: Arial, Helvetica, sans-serif;\">Attached Files Saved to System:</strong><ul style=\"margin: 4px 0 0; padding-left: 18px;\">");
            for (com.saas.backend.dto.EmailAttachmentDto att : reply.getAttachments()) {
                attSb.append("<li>").append(escapeHtml(att.getFileName())).append(" (").append(att.getSize() / 1024).append(" KB)</li>");
            }
            attSb.append("</ul></div>");
            attachmentNotice = attSb.toString();
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>%s - %s</title>
<style type="text/css">
  body { margin: 0; padding: 0; width: 100%% !important; background-color: #F5F2EB; -webkit-font-smoothing: antialiased; }
  table { border-spacing: 0; border-collapse: collapse; }
  td { padding: 0; }
  @media screen and (max-width: 600px) {
    .container { width: 100%% !important; }
    .mobile-padding { padding-left: 16px !important; padding-right: 16px !important; }
  }
</style>
</head>
<body style="margin: 0; padding: 24px 0; background-color: #F5F2EB; font-family: 'Calibri Light', Calibri, sans-serif; color: #2D241E; line-height: 1.55;">
  <center style="width: 100%%; table-layout: fixed;">
    <div class="container" style="max-width: 640px; margin: 0 auto; background-color: #FFFFFF; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 16px rgba(45, 36, 30, 0.08); border: 1px solid #E2D9CC; text-align: left;">
      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td height="5" style="background: linear-gradient(90deg, #264624 0%%, #7A5229 100%%); background-color: #264624;"></td>
        </tr>
      </table>

      <!-- HEADER BANNER -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #264624; padding: 26px 32px; color: #FFFFFF;">
        <tr>
          <td>
            <div style="font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-transform: uppercase; font-size: 11px; letter-spacing: 1.5px; color: #D5E3D2; margin-bottom: 6px;">
              LODGE COMMUNICATION DISPATCH &bull; %s
            </div>
            <h1 style="margin: 0; font-family: Arial, Helvetica, sans-serif; font-size: 20px; font-weight: bold; color: #FFFFFF;">
              %s
            </h1>
            <div style="margin-top: 8px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #D5E3D2;">
              Property: <strong>%s</strong> &bull; Booking: <strong>%s</strong>
            </div>
          </td>
        </tr>
      </table>

      <!-- CONTENT BODY -->
      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 24px 32px;" class="mobile-padding">
        <tr>
          <td>
            <p style="margin: 0 0 14px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 15px; color: #2D241E;">
              Hello <strong>%s</strong>,
            </p>
            <p style="margin: 0 0 20px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 14px; color: #4A3A2F;">
              A response has been received from <strong>%s</strong> regarding accommodation booking <strong>%s</strong>. Below are the booking coordinates and the lodge reply:
            </p>

            <!-- BOOKING SUMMARY CARD -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 20px; border: 1px solid #E2D9CC; border-radius: 8px; overflow: hidden; background-color: #FAF8F4;">
              <tr>
                <td style="padding: 10px 16px; background-color: #EDE7DC; border-bottom: 1px solid #E2D9CC;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td>
                        <strong style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #1F1916; text-transform: uppercase;">
                          Safari: %s &bull; Client: %s
                        </strong>
                      </td>
                      <td align="right">
                        <span style="display: inline-block; padding: 3px 10px; border-radius: 12px; font-family: Arial, Helvetica, sans-serif; font-size: 10px; font-weight: bold; background-color: %s; color: %s; border: 1px solid %s; letter-spacing: 0.5px;">
                          %s
                        </span>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td style="padding: 14px 16px;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td width="50%%" valign="top" style="padding-bottom: 8px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Check-in / Check-out</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; font-weight: bold; color: #1F1916;">%s to %s (%s)</div>
                      </td>
                      <td width="50%%" valign="top" style="padding-bottom: 8px;">
                        <div style="font-family: Arial, Helvetica, sans-serif; font-size: 10px; text-transform: uppercase; color: #7D6B5C; font-weight: bold;">Confirmation Code</div>
                        <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; font-weight: bold; color: #264624;">%s</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>

            <!-- LODGE REPLY MESSAGE BOX -->
            <div style="margin-bottom: 20px;">
              <div style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; font-weight: bold; color: #1F1916; text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 6px;">
                Lodge Reply Details (From: %s):
              </div>
              <div style="padding: 14px 16px; background-color: #FFFFFF; border: 1px solid #E2D9CC; border-left: 4px solid #7A5229; border-radius: 6px; font-family: 'Calibri Light', Calibri, sans-serif; font-size: 13px; color: #2D241E; line-height: 1.6; max-height: 280px; overflow-y: auto;">
                <div style="font-weight: bold; margin-bottom: 6px; color: #5C3D1E;">Subject: %s</div>
                %s
              </div>
            </div>

            %s

            <!-- FOOTER SIGN OFF -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-top: 1px solid #E2D9CC; padding-top: 16px; margin-top: 20px;">
              <tr>
                <td>
                  <div style="font-family: Arial, Helvetica, sans-serif; font-size: 12px; font-weight: bold; color: #264624;">
                    Safari Operations &bull; Central Reservations Platform
                  </div>
                  <div style="font-family: 'Calibri Light', Calibri, sans-serif; font-size: 11px; color: #7D6B5C; margin-top: 2px;">
                    This alert was delivered automatically to the Safari Sales Consultant &amp; Reservation Desk.
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>

      <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #1F1A17; padding: 16px 32px; color: #A69C8E; font-size: 11px; text-align: center;">
        <tr>
          <td>
            <div>Dispatched: %s &bull; System Mailbox: %s</div>
          </td>
        </tr>
      </table>
    </div>
  </center>
</body>
</html>
        """.formatted(
                headerTitle, bookingRef,
                statusLabel,
                headerTitle,
                propName, bookingRef,
                recipientName, propName, bookingRef,
                safariRef, clientName,
                badgeBg, badgeText, badgeBorder, statusLabel,
                checkInStr, checkOutStr, nightsStr,
                confNumberStr,
                fromEmailStr, subjectStr, replyBodyHtml,
                attachmentNotice,
                timestampStr, escapeHtml(systemEmail)
        );
    }

    /**
     * Builds plain text email fallback when a lodge replies to a booking request.
     */
    public String buildLodgeReplyAlertPlainText(
            User recipient,
            AccommodationBooking booking,
            com.saas.backend.dto.IncomingMailMessage reply,
            com.saas.backend.dto.EmailAnalysisResult analysis,
            String systemEmail) {

        String recipientName = recipient != null && recipient.getFirstName() != null ? recipient.getFirstName() : "Colleague";
        String propName = booking.getProperty() != null && booking.getProperty().getName() != null ? booking.getProperty().getName() : "Property";
        String bookingRef = booking.getReferenceNumber();
        AccommodationRequirement req = booking.getAccommodationRequirement();
        Safari safari = req != null ? req.getSafari() : null;
        String safariRef = safari != null && safari.getReferenceNumber() != null ? safari.getReferenceNumber() : "N/A";
        Client client = safari != null ? safari.getClient() : null;
        String clientName = client != null ? (client.getFirstName() + " " + (client.getLastName() != null ? client.getLastName() : "")) : "Client";

        String status = analysis != null && analysis.getResponseType() != null ? analysis.getResponseType().name() : "RECEIVED";
        String confNum = analysis != null && analysis.getConfirmationNumber() != null ? analysis.getConfirmationNumber() : (booking.getConfirmationNumber() != null ? booking.getConfirmationNumber() : "N/A");

        StringBuilder sb = new StringBuilder();
        sb.append("LODGE RESERVATION REPLY ALERT - ").append(bookingRef).append("\n");
        sb.append("====================================================\n\n");
        sb.append("Hello ").append(recipientName).append(",\n\n");
        sb.append("A reply was received from ").append(propName).append(" regarding booking ").append(bookingRef).append(".\n\n");
        sb.append("BOOKING COORDINATES:\n");
        sb.append(" - Safari Reference:    ").append(safariRef).append("\n");
        sb.append(" - Client:              ").append(clientName).append("\n");
        sb.append(" - Detected Status:     ").append(status).append("\n");
        sb.append(" - Confirmation Number: ").append(confNum).append("\n");
        sb.append(" - Check-in Date:       ").append(booking.getCheckIn()).append("\n");
        sb.append(" - Check-out Date:      ").append(booking.getCheckOut()).append("\n\n");

        if (reply != null) {
            sb.append("LODGE MESSAGE (From: ").append(reply.getFrom()).append("):\n");
            sb.append("Subject: ").append(reply.getSubject()).append("\n\n");
            sb.append(reply.getBody() != null ? reply.getBody().trim() : "(No text)").append("\n\n");

            if (reply.getAttachments() != null && !reply.getAttachments().isEmpty()) {
                sb.append("ATTACHMENTS PERSISTED IN SYSTEM:\n");
                for (com.saas.backend.dto.EmailAttachmentDto att : reply.getAttachments()) {
                    sb.append(" - ").append(att.getFileName()).append(" (").append(att.getSize()).append(" bytes)\n");
                }
                sb.append("\n");
            }
        }

        sb.append("---\nCentral Reservations Platform\n").append(systemEmail).append("\n");
        return sb.toString();
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
