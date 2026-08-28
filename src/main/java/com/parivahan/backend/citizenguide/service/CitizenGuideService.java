package com.parivahan.backend.citizenguide.service;

import com.parivahan.backend.citizenguide.dto.*;
import com.parivahan.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CitizenGuideService {

    public CitizenGuideResponse getGuide(String category, String search) {
        List<GuideVideoDto> filteredVideos = videos().stream()
                .filter(video -> matchesCategory(video.getCategory(), category))
                .filter(video -> matchesSearch(video.getTitle(), video.getDescription(), search))
                .toList();

        List<ServiceGuideDto> filteredServices = serviceGuides().stream()
                .filter(guide -> matchesSearch(guide.getTitle(), guide.getSummary(), search))
                .toList();

        return CitizenGuideResponse.builder()
                .videos(filteredVideos)
                .serviceGuides(filteredServices)
                .rules(rules())
                .dos(List.of(
                        "Keep documents valid",
                        "Follow traffic rules",
                        "Wear helmet or seat belt",
                        "Check expiry alerts",
                        "Maintain vehicle health"
                ))
                .donts(List.of(
                        "Do not drive without valid documents",
                        "Do not ignore challans",
                        "Do not share account credentials",
                        "Do not drive under the influence",
                        "Do not modify vehicle illegally"
                ))
                .emergencyGuides(emergencyGuides())
                .terms(terms())
                .build();
    }

    public GuideDetailResponse getGuideDetail(String guideId) {
        List<ServiceGuideDto> guides = serviceGuides();
        ServiceGuideDto guide = guides.stream()
                .filter(item -> item.getId().equalsIgnoreCase(guideId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found: " + guideId));

        int index = guides.indexOf(guide);
        ServiceGuideDto previous = index > 0 ? guides.get(index - 1) : null;
        ServiceGuideDto next = index < guides.size() - 1 ? guides.get(index + 1) : guides.get(0);
        GuideVideoDto video = findVideo(guide.getRelatedVideoId()).orElse(null);

        return GuideDetailResponse.builder()
                .guide(guide)
                .video(video)
                .previousGuide(previous)
                .nextGuide(next)
                .build();
    }

    private Optional<GuideVideoDto> findVideo(String videoId) {
        return videos().stream().filter(video -> video.getId().equals(videoId)).findFirst();
    }

    private boolean matchesCategory(String actualCategory, String requestedCategory) {
        if (!StringUtils.hasText(requestedCategory) || "all".equalsIgnoreCase(requestedCategory)) {
            return true;
        }
        return actualCategory.equalsIgnoreCase(requestedCategory);
    }

    private boolean matchesSearch(String title, String summary, String search) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String query = search.toLowerCase(Locale.ROOT);
        return title.toLowerCase(Locale.ROOT).contains(query)
                || summary.toLowerCase(Locale.ROOT).contains(query);
    }

    private List<GuideVideoDto> videos() {
        String morthVideos = "https://www.youtube.com/embed/videoseries?list=UUTOhBdWjB0FOH1MNa5mEBJg";
        return List.of(
                video("vehicle-twin", "How to use Vehicle Twin", "Understand your vehicle dashboard in 60 seconds", "dashboard", "0:48", "from-blue-950 to-blue-700", "/dashboard.png", "dashboard", morthVideos),
                video("challan-pay", "How to check & pay a challan", "Find, understand and pay your challan", "challan", "0:42", "from-sky-600 to-indigo-500", "/thumbnill2.png", "challan", morthVideos),
                video("live-tracking", "How to track your vehicle", "Track your vehicle in real time", "tracking", "0:51", "from-cyan-500 to-emerald-400", "/thumbnill3.png", "map", morthVideos),
                video("vehicle-health", "How to check vehicle health", "Understand your Vehicle Health Score", "health", "0:45", "from-emerald-600 to-teal-500", "/thumbnill4.png", "health", morthVideos),
                video("ask-ai", "How to Ask My Vehicle", "Get answers about your vehicle using AI", "assistant", "0:46", "from-indigo-500 to-violet-400", "/Ai asistance.png", "bot", morthVideos),
                video("vehicle-registration", "How to do vehicle registration", "Learn the complete new vehicle registration process", "registration", "1:08", "from-blue-700 to-cyan-500", "/thumbnill5.png", "file", morthVideos),
                video("licence-apply", "How to apply for a driving licence", "Understand learner licence, documents, test and payment", "licence", "1:12", "from-violet-600 to-blue-500", "/drivinglicensesHeroSection.png", "license", morthVideos)
        );
    }

    private GuideVideoDto video(String id, String title, String description, String category, String duration,
                                String thumbnailTone, String thumbnailImage, String icon, String embedUrl) {
        return GuideVideoDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .category(category)
                .duration(duration)
                .thumbnailTone(thumbnailTone)
                .thumbnailImage(thumbnailImage)
                .icon(icon)
                .embedUrl(embedUrl)
                .build();
    }

    private List<ServiceGuideDto> serviceGuides() {
        return List.of(
                service("vehicle-dashboard", "Vehicle Dashboard", "Understand your vehicle at a glance.", "dashboard", "blue", "vehicle-twin",
                        List.of("Open Dashboard from the side menu.", "Select your vehicle from My Vehicles.", "Review challans, PUC, insurance and health alerts.")),
                service("challan", "Challan", "Check, pay and challenge your challan.", "challan", "amber", "challan-pay",
                        List.of("Open Challan from Services.", "Review offence, amount, due date and location.", "Pay online or raise a dispute with documents.")),
                service("vehicle-health", "Vehicle Health", "Understand what affects your vehicle score.", "health", "violet", "vehicle-health",
                        List.of("Open the vehicle dashboard.", "Check PUC, insurance, challans and service indicators.", "Resolve expired items to improve the score.")),
                service("ask-my-vehicle", "Ask My Vehicle", "Ask questions about your vehicle using AI.", "bot", "indigo", "ask-ai",
                        List.of("Open Ask My Vehicle.", "Type a vehicle-related question.", "Use suggested actions for documents, challans and alerts.")),
                service("vehicle-registration", "Vehicle Registration", "Apply for vehicle registration with the correct documents.", "file", "cyan", "vehicle-registration",
                        List.of("Choose vehicle type and RTO.", "Upload invoice, insurance, address proof and identity proof.", "Book appointment, pay fee and track the application.")),
                service("driving-licence-apply", "Driving Licence Apply", "Apply for learner or permanent driving licence.", "license", "purple", "licence-apply",
                        List.of("Select state, RTO and vehicle class.", "Complete eligibility, learner licence and document steps.", "Book slot, pay fee and track application status."))
        );
    }

    private ServiceGuideDto service(String id, String title, String summary, String icon, String accent,
                                    String relatedVideoId, List<String> steps) {
        return ServiceGuideDto.builder()
                .id(id)
                .title(title)
                .summary(summary)
                .icon(icon)
                .accent(accent)
                .relatedVideoId(relatedVideoId)
                .steps(steps)
                .build();
    }

    private List<RuleGuideDto> rules() {
        return List.of(
                rule("Helmet", "Use an appropriate helmet when riding a two-wheeler.", "helmet"),
                rule("Seat Belt", "Use seat belts as required.", "seatbelt"),
                rule("PUC", "Keep the applicable Pollution Under Control certificate valid.", "leaf"),
                rule("Insurance", "Maintain the required vehicle insurance.", "shield"),
                rule("Driving Licence", "Drive only with the appropriate valid driving licence.", "license"),
                rule("Traffic Rules", "Follow applicable traffic signs and speed restrictions.", "signal")
        );
    }

    private RuleGuideDto rule(String title, String summary, String icon) {
        return RuleGuideDto.builder().title(title).summary(summary).icon(icon).build();
    }

    private List<EmergencyGuideDto> emergencyGuides() {
        return List.of(
                emergency("vehicle-stolen", "Vehicle Stolen", "Know more", "car"),
                emergency("lost-rc", "Lost RC", "Know more", "rc"),
                emergency("lost-driving-licence", "Lost Driving Licence", "Know more", "license"),
                emergency("wrong-challan", "Wrong Challan", "Know more", "alert"),
                emergency("puc-expired", "PUC Expired", "Know more", "leaf"),
                emergency("missed-appointment", "Missed Appointment", "Know more", "calendar"),
                emergency("application-rejected", "Application Rejected", "Know more", "reject"),
                emergency("other-problems", "Other Problems", "Know more", "dots")
        );
    }

    private EmergencyGuideDto emergency(String id, String title, String summary, String icon) {
        return EmergencyGuideDto.builder().id(id).title(title).summary(summary).icon(icon).build();
    }

    private List<TermGuideDto> terms() {
        return List.of(
                term("RC", "Registration Certificate"),
                term("PUC", "Pollution Under Control"),
                term("RTO", "Regional Transport Office"),
                term("DL", "Driving Licence"),
                term("LL", "Learner's Licence"),
                term("Challan", "Record/notice for violation")
        );
    }

    private TermGuideDto term(String term, String meaning) {
        return TermGuideDto.builder().term(term).meaning(meaning).build();
    }
}
