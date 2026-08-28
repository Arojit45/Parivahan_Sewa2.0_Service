package com.parivahan.backend.assistant.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fallback AI implementation — reads the structured vehicle context built by
 * VehicleContextBuilder and produces intelligent, data-accurate answers
 * without calling any external LLM.
 *
 * Activated by VehicleAssistantService when GeminiAiClient fails.
 */
@Component
public class ContextAwareAiClient implements AiClient {

    @Override
    public String generate(String systemPrompt, String context, String userQuestion) {
        String q = userQuestion.toLowerCase();

        // If history is included, only match keywords against the actual current question
        int cqIndex = q.lastIndexOf("current question:");
        if (cqIndex != -1) {
            q = q.substring(cqIndex);
        }

        // ── Routing by question intent ──────────────────────────────────────
        if (contains(q, "puc", "pollution", "emission")) {
            return answerPuc(context);
        }
        if (contains(q, "insurance")) {
            return answerInsurance(context);
        }
        if (contains(q, "tax", "road tax")) {
            return answerTax(context);
        }
        if (contains(q, "rc", "registration certificate", "registration")) {
            return answerRc(context);
        }
        if (contains(q, "challan", "fine", "penalty", "ticket")) {
            return answerChallans(context);
        }
        if (contains(q, "location", "where", "gps", "tracking", "address")) {
            return answerLocation(context);
        }
        if (contains(q, "health", "score", "condition")) {
            return answerHealth(context);
        }
        if (contains(q, "alert", "attention", "issue", "problem", "warning")) {
            return answerAlerts(context);
        }
        if (contains(q, "okay", "ok", "good", "status", "overview", "summary", "today", "do today")) {
            return answerOverview(context);
        }
        if (contains(q, "document", "compliance", "expire", "expiry", "expiring")) {
            return answerCompliance(context);
        }

        // Generic fallback
        return answerOverview(context);
    }

    // ── Answer generators ────────────────────────────────────────────────────

    private String answerPuc(String ctx) {
        String puc = extract(ctx, "PUC[^\\n]*STATUS[:\\s]*\\n([^\\n]+)");
        if (puc == null) puc = extractLine(ctx, "PUC:");
        if (puc == null) return "I don't have PUC information available for this vehicle right now.";

        if (puc.toUpperCase().contains("VALID") && !puc.toUpperCase().contains("EXPIRING")) {
            return "✅ Your PUC (Pollution Under Control) certificate is *valid*.\n\n" + formatItem(puc)
                    + "\n\nYou're good to go! PUC is required for all vehicles under the Motor Vehicles Act. Renewal is usually done at authorised PUC testing centres.";
        } else if (puc.toUpperCase().contains("EXPIRING")) {
            return "⚠️ Your PUC certificate is *expiring soon*.\n\n" + formatItem(puc)
                    + "\n\nPlease renew it at your nearest authorised PUC testing centre before it expires. Driving with an expired PUC can result in a challan of ₹10,000.";
        } else if (puc.toUpperCase().contains("EXPIRED")) {
            return "🔴 Your PUC certificate has *expired*.\n\n" + formatItem(puc)
                    + "\n\n⚠️ Driving with an expired PUC is illegal and can result in a challan of ₹10,000. Please renew it immediately at your nearest PUC testing centre.";
        }
        return "According to the records, your PUC status is: " + puc.trim() + "\n\nFor renewals, visit your nearest authorised PUC testing centre.";
    }

    private String answerInsurance(String ctx) {
        String ins = extractLine(ctx, "Insurance");
        String provider = extractLine(ctx, "Insurance Provider");
        String providerText = provider != null ? " (Provider: " + provider.trim() + ")" : "";

        if (ins == null) return "I don't have insurance information available for this vehicle right now.";

        if (ins.toUpperCase().contains("EXPIRING")) {
            return "⚠️ Your vehicle insurance is *expiring soon*" + providerText + ".\n\n" + formatItem(ins)
                    + "\n\nPlease renew your insurance before it expires. Driving without valid insurance is illegal under the Motor Vehicles Act and can lead to a fine and vehicle impoundment.";
        } else if (ins.toUpperCase().contains("EXPIRED")) {
            return "🔴 Your vehicle insurance has *expired*" + providerText + ".\n\n" + formatItem(ins)
                    + "\n\n⚠️ Driving without valid insurance is illegal. Please renew immediately with your insurer or through the Parivahan portal.";
        } else if (ins.toUpperCase().contains("VALID")) {
            return "✅ Your vehicle insurance is *valid*" + providerText + ".\n\n" + formatItem(ins)
                    + "\n\nYou are covered. Keep your policy documents handy in the vehicle at all times.";
        }
        return "Your insurance status: " + ins.trim() + providerText;
    }

    private String answerTax(String ctx) {
        String tax = extractLine(ctx, "Tax");
        if (tax == null) return "I don't have road tax information available for this vehicle right now.";

        if (tax.toUpperCase().contains("VALID")) {
            return "✅ Your Road Tax is *valid*.\n\n" + formatItem(tax) + "\n\nNo action required at this time.";
        } else if (tax.toUpperCase().contains("EXPIRING")) {
            return "⚠️ Your Road Tax is *expiring soon*.\n\n" + formatItem(tax)
                    + "\n\nPlease renew your road tax through the Parivahan portal or your nearest RTO before the due date.";
        } else if (tax.toUpperCase().contains("EXPIRED")) {
            return "🔴 Your Road Tax has *expired*.\n\n" + formatItem(tax)
                    + "\n\nPlease renew your road tax immediately at your nearest RTO or through the Parivahan portal to avoid penalties.";
        }
        return "Your road tax status: " + tax.trim();
    }

    private String answerRc(String ctx) {
        String rc = extractLine(ctx, "RC");
        String reg = extractLine(ctx, "Registration Number");
        String regText = reg != null ? " for vehicle " + reg.trim() : "";

        if (rc == null) return "I don't have RC information available for this vehicle right now.";
        if (rc.toUpperCase().contains("VALID")) {
            return "✅ Your Registration Certificate (RC)" + regText + " is *valid*.\n\n" + formatItem(rc)
                    + "\n\nYour vehicle is legally registered. Keep the RC document in the vehicle at all times.";
        }
        return "Your RC status: " + rc.trim() + regText;
    }

    private String answerChallans(String ctx) {
        String challanSection = extractSection(ctx, "CHALLAN");
        if (challanSection == null || challanSection.contains("No pending challans")) {
            return "✅ Great news! You have *no pending challans* for this vehicle.\n\nKeep following traffic rules to stay challan-free. 🚦";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔴 You have *pending challans* that need to be paid:\n\n");

        String count = extractValueFromSection(challanSection, "Total Pending Challans");
        String amount = extractValueFromSection(challanSection, "Total Pending Amount");
        if (count != null) sb.append("📋 Challans: ").append(count).append("\n");
        if (amount != null) sb.append("💰 Total Due: ").append(amount).append("\n");

        sb.append("\nPlease pay your challans through the Parivahan portal or the mParivahan app to avoid further penalties or vehicle impoundment.");
        return sb.toString();
    }

    private String answerLocation(String ctx) {
        String locSection = extractSection(ctx, "GPS");
        if (locSection == null || locSection.contains("OFFLINE")) {
            return "📍 Location data is currently *unavailable* for this vehicle.\n\nThe GPS tracker may be offline or no recent location data was recorded.";
        }

        StringBuilder sb = new StringBuilder("📍 Here is the current location of your vehicle:\n\n");
        String address = extractValueFromSection(locSection, "Current Address");
        String speed = extractValueFromSection(locSection, "Speed");
        String updated = extractValueFromSection(locSection, "Last Updated");

        if (address != null) sb.append("📌 Address: ").append(address).append("\n");
        if (speed != null) {
            double s = parseDouble(speed.replace("km/h", "").trim());
            sb.append("🚗 Speed: ").append(speed).append(s == 0 ? " (parked)" : " (moving)").append("\n");
        }
        if (updated != null) sb.append("🕐 Last Updated: ").append(updated).append("\n");

        return sb.toString();
    }

    private String answerHealth(String ctx) {
        String score = extractValueFromSection(ctx, "Score");
        String label = extractValueFromSection(ctx, "Label");

        StringBuilder sb = new StringBuilder();
        if (score != null) {
            sb.append("🏥 Your vehicle health score is *").append(score).append("*");
            if (label != null) sb.append(" — ").append(label);
            sb.append("\n\n");
        }

        // Summarise issues
        String challanLine = extractSection(ctx, "CHALLAN");
        boolean hasChallans = challanLine != null && !challanLine.contains("No pending challans");

        String expiring = findExpiringItems(ctx);
        if (!expiring.isEmpty()) {
            sb.append("⚠️ Items needing attention:\n").append(expiring).append("\n");
        }
        if (hasChallans) {
            sb.append("🔴 You have pending challans that need payment.\n");
        }
        if (expiring.isEmpty() && !hasChallans) {
            sb.append("✅ All your documents are up-to-date and no challans are pending. Your vehicle is in great shape!");
        }
        return sb.toString();
    }

    private String answerAlerts(String ctx) {
        String alertSection = extractSection(ctx, "ACTIVE ALERTS");
        if (alertSection == null || alertSection.contains("No active alerts")) {
            return "✅ There are *no active alerts* for your vehicle right now.\n\nEverything looks good! Keep your documents renewed on time to stay alert-free.";
        }
        return "⚠️ Here are your active alerts:\n\n" + alertSection.trim()
                + "\n\nPlease address these issues at the earliest to keep your vehicle compliant.";
    }

    private String answerCompliance(String ctx) {
        StringBuilder sb = new StringBuilder("📋 Here is your document compliance status:\n\n");
        String[] docs = {"RC", "PUC", "Insurance", "Road Tax", "Permit", "Fitness"};
        for (String doc : docs) {
            String val = extractLine(ctx, doc + ":");
            if (val != null && !val.isEmpty()) {
                String emoji = val.toUpperCase().contains("EXPIRED") ? "🔴"
                        : val.toUpperCase().contains("EXPIRING") ? "⚠️"
                        : val.toUpperCase().contains("VALID") ? "✅" : "ℹ️";
                sb.append(emoji).append(" ").append(doc).append(": ").append(val.trim()).append("\n");
            }
        }
        return sb.toString();
    }

    private String answerOverview(String ctx) {
        StringBuilder sb = new StringBuilder();
        String score = extractValueFromSection(ctx, "Score");
        String label = extractValueFromSection(ctx, "Label");

        if (score != null) {
            sb.append("🏥 Vehicle Health Score: *").append(score).append("*");
            if (label != null) sb.append(" (").append(label).append(")");
            sb.append("\n\n");
        }

        String expiring = findExpiringItems(ctx);
        boolean hasChallans = ctx.contains("Total Pending Challans");

        if (!expiring.isEmpty() || hasChallans) {
            sb.append("📌 Things needing your attention:\n");
            if (!expiring.isEmpty()) sb.append(expiring);
            if (hasChallans) {
                String amt = extractValueFromSection(ctx, "Total Pending Amount");
                sb.append("  🔴 Pending challans").append(amt != null ? " (₹" + amt + " due)" : "").append(" — please pay via Parivahan portal\n");
            }
        } else {
            sb.append("✅ Your vehicle is in great shape! All documents are valid and no challans are pending.");
        }

        String address = extractValueFromSection(ctx, "Current Address");
        if (address != null) {
            sb.append("\n📍 Current Location: ").append(address);
        }

        return sb.toString().trim();
    }

    // ── Utility helpers ──────────────────────────────────────────────────────

    private boolean contains(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }

    private String extractLine(String ctx, String key) {
        Pattern p = Pattern.compile(key + "[^:]*:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(ctx);
        return m.find() ? m.group(1) : null;
    }

    private String extract(String ctx, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(ctx);
        return m.find() ? m.group(1) : null;
    }

    private String extractSection(String ctx, String header) {
        int idx = ctx.toUpperCase().indexOf(header.toUpperCase());
        if (idx < 0) return null;
        int end = ctx.indexOf("\n\n", idx);
        return end > 0 ? ctx.substring(idx, end) : ctx.substring(idx);
    }

    private String extractValueFromSection(String text, String key) {
        Pattern p = Pattern.compile(key + "[^:]*:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private String findExpiringItems(String ctx) {
        StringBuilder sb = new StringBuilder();
        String[] docs = {"Insurance", "PUC", "Road Tax", "RC", "Permit", "Fitness"};
        for (String doc : docs) {
            String line = extractLine(ctx, doc + "[^:]*");
            if (line == null) continue;
            if (line.toUpperCase().contains("EXPIRING")) {
                sb.append("  ⚠️ ").append(doc).append(": Expiring soon — ").append(formatItem(line)).append("\n");
            } else if (line.toUpperCase().contains("EXPIRED")) {
                sb.append("  🔴 ").append(doc).append(": EXPIRED — ").append(formatItem(line)).append("\n");
            }
        }
        return sb.toString();
    }

    private String formatItem(String raw) {
        // Extract days-left info from context string
        Pattern p = Pattern.compile("(\\d+) days (remaining|ago)");
        Matcher m = p.matcher(raw);
        if (m.find()) {
            return m.group(2).equals("remaining")
                    ? m.group(1) + " days remaining"
                    : "expired " + m.group(1) + " days ago";
        }
        Pattern dateP = Pattern.compile("Valid till: ([\\d-]+)");
        Matcher dateM = dateP.matcher(raw);
        if (dateM.find()) return "Valid till " + dateM.group(1);
        return raw.trim();
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }
}
