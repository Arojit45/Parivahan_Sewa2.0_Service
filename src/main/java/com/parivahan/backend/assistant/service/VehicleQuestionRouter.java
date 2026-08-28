package com.parivahan.backend.assistant.service;

import com.parivahan.backend.assistant.model.VehicleIntent;
import org.springframework.stereotype.Component;

/**
 * Simple keyword-based intent router.
 * Deterministic and transparent — no ML magic for the MVP.
 * The router just figures out which vehicle data is relevant to the user's question
 * so VehicleContextBuilder can fetch only that data from the dashboard.
 */
@Component
public class VehicleQuestionRouter {

    public VehicleIntent detectIntent(String question) {
        if (question == null || question.isBlank()) {
            return VehicleIntent.VEHICLE_OVERVIEW;
        }

        String q = question.toLowerCase().trim();
        VehicleIntent multilingualIntent = detectMultilingualIntent(q);
        if (multilingualIntent != null) {
            return multilingualIntent;
        }

        // "What should I do today?" — signature feature
        if (containsAny(q, "today", "aaj", "आज", "kya karna", "what to do", "priorities",
                "priority", "action", "first thing")) {
            return VehicleIntent.WHAT_TO_DO_TODAY;
        }

        // Health score
        if (containsAny(q, "health score", "score", "health", "healthy", "why is my score",
                "why is my health", "स्वास्थ्य", "स्कोर", "health kyu")) {
            return VehicleIntent.HEALTH_SCORE;
        }

        // PUC
        if (containsAny(q, "puc", "pollution", "emission", "certificate",
                "प्रदूषण", "puc kab")) {
            return VehicleIntent.PUC_STATUS;
        }

        // Insurance
        if (containsAny(q, "insurance", "insure", "bima", "बीमा", "policy", "cover",
                "insurance kab", "insurance expire")) {
            return VehicleIntent.INSURANCE_STATUS;
        }

        // Tax
        if (containsAny(q, "tax", "road tax", "टैक्स", "कर", "tax due", "tax expire")) {
            return VehicleIntent.TAX_STATUS;
        }

        // RC
        if (containsAny(q, " rc ", "registration certificate", "rc expire", "registration card",
                "आरसी", "rc kab")) {
            return VehicleIntent.RC_STATUS;
        }

        // Permit
        if (containsAny(q, "permit", "परमिट")) {
            return VehicleIntent.PERMIT_STATUS;
        }

        // Fitness
        if (containsAny(q, "fitness", "fit certificate", "फिटनेस")) {
            return VehicleIntent.FITNESS_STATUS;
        }

        // Challan
        if (containsAny(q, "challan", "fine", "penalty", "dues", "pending", "चालान",
                "jurmana", "जुर्माना", "owe", "amount", "pay")) {
            return VehicleIntent.CHALLAN_STATUS;
        }

        // GPS / location
        if (containsAny(q, "where", "location", "gps", "track", "map", "speed",
                "kahan", "कहां", "live", "locate", "address")) {
            return VehicleIntent.LIVE_LOCATION;
        }

        // Alerts
        if (containsAny(q, "alert", "warning", "attention", "problem", "issue",
                "चेतावनी", "danger")) {
            return VehicleIntent.ACTIVE_ALERTS;
        }

        // Documents expiring
        if (containsAny(q, "expir", "expire", "renew", "renewal", "due",
                "खत्म", "समाप्त", "update")) {
            return VehicleIntent.EXPIRING_DOCUMENTS;
        }

        // Vehicle overview / is it okay
        if (containsAny(q, "okay", "ok", "fine", "good", "status", "overview",
                "theek", "ठीक", "condition", "summary", "how is")) {
            return VehicleIntent.VEHICLE_OVERVIEW;
        }

        // General vehicle question (definition questions, what is X, etc.)
        if (containsAny(q, "what is", "what are", "explain", "define", "kya hai",
                "क्या है", "tell me about")) {
            return VehicleIntent.GENERAL_VEHICLE_QUESTION;
        }

        // Fallback to overview
        return VehicleIntent.VEHICLE_OVERVIEW;
    }

    private VehicleIntent detectMultilingualIntent(String q) {
        if (containsAny(q, "आज", "आज क्या", "आज काय", "আজ", "இன்று", "నేడు", "ఇవాళ",
                "ಇಂದು", "ഇന്ന്", "આજે", "ਅੱਜ", "ଆଜି")) {
            return VehicleIntent.WHAT_TO_DO_TODAY;
        }
        if (containsAny(q, "स्वास्थ्य", "सेहत", "आरोग्य", "স্বাস্থ্য", "உடல்நலம்", "ஆரோக்கியம்",
                "ఆరోగ్యం", "ಆರೋಗ್ಯ", "ആരോഗ്യം", "આરોગ્ય", "ਸਿਹਤ", "ସ୍ୱାସ୍ଥ୍ୟ")) {
            return VehicleIntent.HEALTH_SCORE;
        }
        if (containsAny(q, "प्रदूषण", "प्रमाणपत्र", "প্রদূষণ", "மாசு", "కాలుష్యం",
                "ಮಾಲಿನ್ಯ", "മലിനീകരണം", "પ્રદૂષણ", "ਪ੍ਰਦੂਸ਼ਣ", "ପ୍ରଦୂଷଣ")) {
            return VehicleIntent.PUC_STATUS;
        }
        if (containsAny(q, "बीमा", "विमा", "বীমা", "காப்பீடு", "బీమా", "ವಿಮೆ",
                "ഇൻഷുറൻസ്", "વીમો", "ਬੀਮਾ", "ବୀମା")) {
            return VehicleIntent.INSURANCE_STATUS;
        }
        if (containsAny(q, "टैक्स", "कर", "রোড ট্যাক্স", "வரி", "పన్ను", "ತೆರಿಗೆ",
                "നികുതി", "ટેક્સ", "ਟੈਕਸ", "କର")) {
            return VehicleIntent.TAX_STATUS;
        }
        if (containsAny(q, "आरसी", "निबंधन", "नोंदणी", "নিবন্ধন", "பதிவு", "నమోదు",
                "ನೋಂದಣಿ", "രജിസ്ട്രേഷൻ", "નોંધણી", "ਰਜਿਸਟ੍ਰੇਸ਼ਨ", "ପଞ୍ଜିକରଣ")) {
            return VehicleIntent.RC_STATUS;
        }
        if (containsAny(q, "परमिट", "পারমিট", "அனுமதி", "పర్మిట్", "ಪರ್ಮಿಟ್",
                "പെർമിറ്റ്", "પરમિટ", "ਪਰਮਿਟ", "ପରମିଟ")) {
            return VehicleIntent.PERMIT_STATUS;
        }
        if (containsAny(q, "फिटनेस", "ফিটনেস", "தகுதி", "ఫిట్‌నెస్", "ಫಿಟ್ನೆಸ್",
                "ഫിറ്റ്നസ്", "ફિટનેસ", "ਫਿਟਨੈੱਸ", "ଫିଟନେସ")) {
            return VehicleIntent.FITNESS_STATUS;
        }
        if (containsAny(q, "चालान", "जुर्माना", "চালান", "அபராதம்", "చలాన్",
                "ದಂಡ", "ಚಲನ್", "ചലാൻ", "ચલણ", "ਚਲਾਨ", "ଚାଲାଣ")) {
            return VehicleIntent.CHALLAN_STATUS;
        }
        if (containsAny(q, "कहाँ", "स्थान", "कुठे", "অবস্থান", "இடம்", "எங்கே",
                "స్థానం", "ಎಲ್ಲಿ", "സ്ഥലം", "എവിടെ", "સ્થાન", "ક્યાં", "ਕਿੱਥੇ", "ସ୍ଥାନ")) {
            return VehicleIntent.LIVE_LOCATION;
        }
        if (containsAny(q, "चेतावनी", "समस्या", "সতর্কতা", "সমস্যা", "பிரச்சனை",
                "எச்சரிக்கை", "హెచ్చరిక", "ಎಚ್ಚರಿಕೆ", "മുന്നറിയിപ്പ്", "ચેતવણી",
                "ਸਮੱਸਿਆ", "ਚੇਤਾਵਨੀ", "ସତର୍କତା")) {
            return VehicleIntent.ACTIVE_ALERTS;
        }
        if (containsAny(q, "समाप्त", "नवीनीकरण", "मुदत", "মেয়াদ", "புதுப்பிக்க",
                "காலாவதி", "గడువు", "ನವೀಕರಣ", "കാലാവധി", "સમાપ્ત", "મુદત",
                "ਮਿਆਦ", "ନବୀକରଣ")) {
            return VehicleIntent.EXPIRING_DOCUMENTS;
        }
        if (containsAny(q, "ठीक", "स्थिति", "स्थिती", "ভালো", "நிலை", "స్థితి",
                "ಸ್ಥಿತಿ", "സ്ഥിതി", "સ્થિતિ", "ਠੀਕ", "ସ୍ଥିତି")) {
            return VehicleIntent.VEHICLE_OVERVIEW;
        }
        if (containsAny(q, "क्या है", "काय आहे", "কী", "என்ன", "ఏమిటి", "ಏನು",
                "എന്താണ്", "શું છે", "ਕੀ ਹੈ", "କଣ")) {
            return VehicleIntent.GENERAL_VEHICLE_QUESTION;
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
