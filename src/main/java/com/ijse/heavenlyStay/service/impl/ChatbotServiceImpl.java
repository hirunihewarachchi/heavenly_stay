package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.ChatRequestDTO;
import com.ijse.heavenlyStay.dto.ChatResponseDTO;
import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.RoomCategory;
import com.ijse.heavenlyStay.repository.BoardingRepository;
import com.ijse.heavenlyStay.repository.ReviewRepository;
import com.ijse.heavenlyStay.service.BoardingService;
import com.ijse.heavenlyStay.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final BoardingRepository boardingRepository;
    private final BoardingService boardingService;
    private final ReviewRepository reviewRepository;

    // Domain-specific keywords: a message must contain at least one of these to be answered.
    private static final Set<String> DOMAIN_KEYWORDS = Set.of(
            "boarding", "room", "stay", "rent", "heavenly", "heavenlystay",
            "key money", "keymoney", "location", "colombo", "kandy", "galle",
            "matara", "kurunegala", "gampaha", "jaffna", "panadura", "moratuwa",
            "kelaniya", "malabe", "nugegoda", "battaramulla", "maharagama",
            "horana", "negombo", "ratnapura", "badulla",
            "bed", "hostel", "place", "fee", "publish", "deposit", "booking",
            "available", "vacancy", "price", "cost", "cheap", "affordable",
            "single", "shared", "listing", "commission", "how many", "district",
            "province", "address", "hi", "hello", "hey",
            "lowest", "highest", "minimum", "maximum", "most expensive",
            "most reviewed", "most popular", "popular", "top reviewed", "best reviewed"
    );

    private static final String OFF_TOPIC_REPLY =
            "I am the HeavenlyStay chat agent. I can only answer the questions related to HeavenlyStay. Thank you.";

    @Override
    public ChatResponseDTO processQuery(ChatRequestDTO request) {
        log.info("Executing method processQuery() for message: {}", request.getUserMessage());
        try {
            String msg = (request.getUserMessage() != null) ? request.getUserMessage().trim().toLowerCase() : "";

            List<String> defaultPills = List.of(
                    "💡 Boardings under LKR 15,000",
                    "📍 Boardings in Colombo",
                    "🛏️ Available Shared Rooms",
                    "🔑 How does Key Money work?"
            );

            if (msg.isEmpty()) {
                return new ChatResponseDTO(
                        "Hello! 👋 Ask me any question about our boardings, prices, locations, or availability.",
                        "GREETING",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            // 1. Domain intent check — reject anything that has no HeavenlyStay-related keyword.
            boolean containsDomainTerm = DOMAIN_KEYWORDS.stream().anyMatch(msg::contains);
            if (!containsDomainTerm) {
                return new ChatResponseDTO(
                        OFF_TOPIC_REPLY,
                        "REJECTED_OFFTOPIC",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            List<Boarding> allApproved = boardingRepository.findByStatus(BoardingStatus.APPROVED);

            // 2. Specific Boarding Entity Question (e.g. "What is the rent for Sunset Villa?")
            Boarding matchedBoarding = findMatchingBoarding(msg, allApproved);
            if (matchedBoarding != null) {
                if (msg.contains("key money") || msg.contains("deposit")) {
                    BigDecimal km = matchedBoarding.getKeyMoney() != null ? matchedBoarding.getKeyMoney() : BigDecimal.ZERO;
                    BigDecimal comm = km.multiply(new BigDecimal("0.05"));
                    BigDecimal total = km.add(comm);
                    return new ChatResponseDTO(
                            "The key money for **" + matchedBoarding.getName() + "** is **LKR " + km.stripTrailingZeros().toPlainString() + "** (plus 5% commission LKR " + comm.stripTrailingZeros().toPlainString() + " = Total **LKR " + total.stripTrailingZeros().toPlainString() + "**).",
                            "SPECIFIC_INFO",
                            defaultPills,
                            List.of(boardingService.getBoardingById(matchedBoarding.getBoardingId()))
                    );
                }
                if (msg.contains("rent") || msg.contains("price") || msg.contains("cost") || msg.contains("how much")) {
                    return new ChatResponseDTO(
                            "The monthly rent for **" + matchedBoarding.getName() + "** is **LKR " + matchedBoarding.getMonthlyRent().stripTrailingZeros().toPlainString() + "/month**.",
                            "SPECIFIC_INFO",
                            defaultPills,
                            List.of(boardingService.getBoardingById(matchedBoarding.getBoardingId()))
                    );
                }
                if (msg.contains("location") || msg.contains("where") || msg.contains("address")) {
                    return new ChatResponseDTO(
                            "**" + matchedBoarding.getName() + "** is located at **" + matchedBoarding.getAddress() + ", " + matchedBoarding.getDistrict() + "**.",
                            "SPECIFIC_INFO",
                            defaultPills,
                            List.of(boardingService.getBoardingById(matchedBoarding.getBoardingId()))
                    );
                }
                if (msg.contains("available") || msg.contains("vacancy") || msg.contains("bed") || msg.contains("room") || msg.contains("capacity")) {
                    int avail = (matchedBoarding.getAvailableBeds() != null && matchedBoarding.getAvailableBeds() > 0) ? matchedBoarding.getAvailableBeds() : (matchedBoarding.getAvailableRooms() != null ? matchedBoarding.getAvailableRooms() : 0);
                    String text = avail > 0 ? "currently has **" + avail + " available bed(s)/room(s)**." : "is currently **FULLY BOOKED**.";
                    return new ChatResponseDTO(
                            "**" + matchedBoarding.getName() + "** " + text,
                            "SPECIFIC_INFO",
                            defaultPills,
                            List.of(boardingService.getBoardingById(matchedBoarding.getBoardingId()))
                    );
                }
                // General query about specific boarding
                return new ChatResponseDTO(
                        "**" + matchedBoarding.getName() + "** (" + matchedBoarding.getDistrict() + ") — Rent: LKR " + matchedBoarding.getMonthlyRent().stripTrailingZeros().toPlainString() + "/mo, Key Money: LKR " + (matchedBoarding.getKeyMoney() != null ? matchedBoarding.getKeyMoney().stripTrailingZeros().toPlainString() : "0") + ".",
                        "SPECIFIC_INFO",
                        defaultPills,
                        List.of(boardingService.getBoardingById(matchedBoarding.getBoardingId()))
                );
            }

            // 3. Question: "Cheapest" / "Lowest rent"
            if (msg.contains("cheapest") || msg.contains("lowest rent") || msg.contains("lowest price") || msg.contains("most affordable")) {
                String loc = extractLocationName(msg);
                Optional<Boarding> cheapestOpt = allApproved.stream()
                        .filter(b -> loc == null || (b.getDistrict() != null && b.getDistrict().toLowerCase().contains(loc)) || (b.getAddress() != null && b.getAddress().toLowerCase().contains(loc)))
                        .min(Comparator.comparing(b -> b.getMonthlyRent() != null ? b.getMonthlyRent() : BigDecimal.valueOf(Long.MAX_VALUE)));

                if (cheapestOpt.isPresent()) {
                    Boarding ch = cheapestOpt.get();
                    String locStr = loc != null ? " in **" + capitalize(loc) + "**" : "";
                    return new ChatResponseDTO(
                            "The cheapest boarding place" + locStr + " is **" + ch.getName() + "** in " + ch.getDistrict() + " at **LKR " + ch.getMonthlyRent().stripTrailingZeros().toPlainString() + "/month**.",
                            "CHEAPEST_INFO",
                            defaultPills,
                            List.of(boardingService.getBoardingById(ch.getBoardingId()))
                    );
                }
            }

            // 3b. Lowest monthly rent (same as cheapest but triggered by "lowest monthly rent" phrasing too)
            if (msg.contains("lowest monthly rent") || msg.contains("minimum rent") || msg.contains("cheapest rent")) {
                String loc = extractLocationName(msg);
                List<Boarding> candidates = allApproved.stream()
                        .filter(b -> loc == null || (b.getDistrict() != null && b.getDistrict().toLowerCase().contains(loc)))
                        .filter(b -> b.getMonthlyRent() != null)
                        .sorted(Comparator.comparing(Boarding::getMonthlyRent))
                        .limit(5)
                        .collect(Collectors.toList());
                if (!candidates.isEmpty()) {
                    Boarding lowest = candidates.get(0);
                    String locStr = loc != null ? " in **" + capitalize(loc) + "**" : "";
                    List<BoardingDTO> dtos = candidates.stream()
                            .map(b -> boardingService.getBoardingById(b.getBoardingId())).toList();
                    return new ChatResponseDTO(
                            "The lowest monthly rent" + locStr + " starts from **LKR " + lowest.getMonthlyRent().stripTrailingZeros().toPlainString() + "/month** at **" + lowest.getName() + "**, " + lowest.getDistrict() + ". Here are the top affordable options:",
                            "RENT_INFO",
                            defaultPills,
                            dtos
                    );
                }
            }

            // 3c. Highest monthly rent
            if (msg.contains("highest monthly rent") || msg.contains("maximum rent") || msg.contains("most expensive") || msg.contains("highest rent") || msg.contains("highest price")) {
                String loc = extractLocationName(msg);
                List<Boarding> candidates = allApproved.stream()
                        .filter(b -> loc == null || (b.getDistrict() != null && b.getDistrict().toLowerCase().contains(loc)))
                        .filter(b -> b.getMonthlyRent() != null)
                        .sorted(Comparator.comparing(Boarding::getMonthlyRent).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                if (!candidates.isEmpty()) {
                    Boarding highest = candidates.get(0);
                    String locStr = loc != null ? " in **" + capitalize(loc) + "**" : "";
                    List<BoardingDTO> dtos = candidates.stream()
                            .map(b -> boardingService.getBoardingById(b.getBoardingId())).toList();
                    return new ChatResponseDTO(
                            "The highest monthly rent" + locStr + " goes up to **LKR " + highest.getMonthlyRent().stripTrailingZeros().toPlainString() + "/month** at **" + highest.getName() + "**, " + highest.getDistrict() + ". Here are the premium options:",
                            "RENT_INFO",
                            defaultPills,
                            dtos
                    );
                }
            }

            // 3d. Most reviewed boardings
            if (msg.contains("most reviewed") || msg.contains("most popular") || msg.contains("top reviewed") || msg.contains("best reviewed") || msg.contains("popular boarding")) {
                String loc = extractLocationName(msg);
                // Get boarding IDs ordered by review count from DB
                List<Object[]> reviewCounts = reviewRepository.findBoardingIdsByReviewCountDesc();
                // Build a map of boardingId -> approved boarding
                Map<Long, Boarding> approvedMap = allApproved.stream()
                        .collect(Collectors.toMap(Boarding::getBoardingId, b -> b));
                // Pick the top 5 approved boardings that match the location filter
                List<Boarding> topReviewed = reviewCounts.stream()
                        .map(row -> approvedMap.get((Long) row[0]))
                        .filter(Objects::nonNull)
                        .filter(b -> loc == null || (b.getDistrict() != null && b.getDistrict().toLowerCase().contains(loc)))
                        .limit(5)
                        .collect(Collectors.toList());
                if (!topReviewed.isEmpty()) {
                    String locStr = loc != null ? " in **" + capitalize(loc) + "**" : "";
                    List<BoardingDTO> dtos = topReviewed.stream()
                            .map(b -> boardingService.getBoardingById(b.getBoardingId())).toList();
                    return new ChatResponseDTO(
                            "Here are the most reviewed boarding places" + locStr + " on HeavenlyStay:",
                            "REVIEW_INFO",
                            defaultPills,
                            dtos
                    );
                } else {
                    return new ChatResponseDTO(
                            "There are no reviewed boarding places" + (loc != null ? " in **" + capitalize(loc) + "**" : "") + " yet.",
                            "REVIEW_INFO",
                            defaultPills,
                            Collections.emptyList()
                    );
                }
            }

            // 4. Listing Fee / Publishing Fee Question
            if ((msg.contains("listing fee") || msg.contains("publish") || msg.contains("ad fee") || msg.contains("cost to post")) && !msg.contains("key money")) {
                return new ChatResponseDTO(
                        "Publishing a boarding ad on HeavenlyStay costs a one-time listing fee of **LKR 1,000** for a 3-month active listing period.",
                        "POLICY_INFO",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            // 5. Key Money Policy Question
            if ((msg.contains("key money") || msg.contains("commission") || msg.contains("deposit")) && !msg.contains("how many")) {
                return new ChatResponseDTO(
                        "Key money booking payment is calculated as **Key Money Deposit + 5% Platform Commission**.",
                        "POLICY_INFO",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            // 6. Count Question ("How many boardings...")
            if (msg.contains("how many") && (msg.contains("boarding") || msg.contains("room") || msg.contains("place") || msg.contains("bed"))) {
                String loc = extractLocationName(msg);
                long count = allApproved.stream()
                        .filter(b -> loc == null || (b.getDistrict() != null && b.getDistrict().toLowerCase().contains(loc)))
                        .count();
                String locText = loc != null ? " in **" + capitalize(loc) + "**" : "";
                return new ChatResponseDTO(
                        "We currently have **" + count + " approved boarding place(s)**" + locText + " in our database.",
                        "COUNT_INFO",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            // 7. Greeting
            if (msg.equals("hi") || msg.equals("hello") || msg.equals("hey")) {
                return new ChatResponseDTO(
                        "Hi there! What question do you have about our boardings, prices, or locations?",
                        "GREETING",
                        defaultPills,
                        Collections.emptyList()
                );
            }

            // 8. General Search — directly answer based on what the user asked.
            // Detect filters from message: location, budget, room type, availability.
            BigDecimal maxRent = extractMaxRent(msg);

            final RoomCategory reqCategory;
            if (msg.contains("single")) {
                reqCategory = RoomCategory.SINGLE;
            } else if (msg.contains("shared") || msg.contains("sharing") || msg.contains("bed")) {
                reqCategory = RoomCategory.SHARED;
            } else {
                reqCategory = null;
            }

            final boolean wantsAvailable = msg.contains("available") || msg.contains("vacancy") || msg.contains("vacant");
            final BigDecimal maxBudget = maxRent;
            final String targetLoc = extractLocationName(msg);

            List<Boarding> filtered = allApproved.stream().filter(b -> {
                // Filter by budget
                if (maxBudget != null && b.getMonthlyRent() != null) {
                    if (b.getMonthlyRent().compareTo(maxBudget) > 0) return false;
                }
                // Filter by room category
                if (reqCategory != null && b.getRoomCategory() != reqCategory) {
                    return false;
                }
                // Filter by location
                if (targetLoc != null) {
                    boolean matchDistrict = b.getDistrict() != null && b.getDistrict().toLowerCase().contains(targetLoc);
                    boolean matchProvince = b.getProvince() != null && b.getProvince().toLowerCase().contains(targetLoc);
                    boolean matchAddress = b.getAddress() != null && b.getAddress().toLowerCase().contains(targetLoc);
                    boolean matchName = b.getName() != null && b.getName().toLowerCase().contains(targetLoc);
                    if (!matchDistrict && !matchProvince && !matchAddress && !matchName) return false;
                }
                // Filter by availability if user asked for it
                if (wantsAvailable) {
                    int beds = b.getAvailableBeds() != null ? b.getAvailableBeds() : 0;
                    int rooms = b.getAvailableRooms() != null ? b.getAvailableRooms() : 0;
                    if (beds <= 0 && rooms <= 0) return false;
                }
                return true;
            })
            // Sort by monthly rent ascending so cheapest come first
            .sorted(Comparator.comparing(b -> b.getMonthlyRent() != null ? b.getMonthlyRent() : BigDecimal.valueOf(Long.MAX_VALUE)))
            .collect(Collectors.toList());

            int totalFound = filtered.size();
            // Show top 5
            List<BoardingDTO> dtoList = filtered.stream()
                    .limit(5)
                    .map(b -> boardingService.getBoardingById(b.getBoardingId()))
                    .toList();

            if (dtoList.isEmpty()) {
                StringBuilder noMatch = new StringBuilder("I couldn't find any boarding places");
                if (targetLoc != null) noMatch.append(" in **").append(capitalize(targetLoc)).append("**");
                if (maxRent != null) noMatch.append(" under **LKR ").append(maxRent.toPlainString()).append("**");
                if (reqCategory != null) noMatch.append(" with a **").append(reqCategory.name().toLowerCase()).append(" room**");
                if (wantsAvailable) noMatch.append(" with available beds/rooms");
                noMatch.append(". You may try a different location or budget.");
                return new ChatResponseDTO(noMatch.toString(), "BOARDING_SEARCH", defaultPills, Collections.emptyList());
            }

            // Build a natural human-language reply
            StringBuilder reply = new StringBuilder();
            if (wantsAvailable) {
                reply.append("Good news! I found ");
            } else {
                reply.append("Here are ");
            }
            if (totalFound > 5) {
                reply.append("the top **5 out of ").append(totalFound).append("** boarding place(s)");
            } else {
                reply.append("**").append(totalFound).append("** boarding place(s)");
            }
            if (targetLoc != null) reply.append(" in **").append(capitalize(targetLoc)).append("**");
            if (reqCategory != null) reply.append(" with **").append(reqCategory.name().toLowerCase()).append(" rooms**");
            if (maxRent != null) reply.append(" under **LKR ").append(maxRent.toPlainString()).append("**");
            if (wantsAvailable) reply.append(" that have available space");
            reply.append(", sorted by lowest rent:");

            return new ChatResponseDTO(reply.toString(), "BOARDING_SEARCH", defaultPills, dtoList);
        } catch (Exception e) {
            log.error("Error in processQuery() " + e.getMessage());
            throw e;
        }
    }

    // Generic words that appear in boarding names but are too common to use for matching.
    // A query like "show me boarding in Colombo" should NOT match the first boarding
    // whose name contains the word "boarding", "room", "house", etc.
    private static final Set<String> GENERIC_NAME_WORDS = Set.of(
            "boarding", "room", "house", "stay", "home", "villa", "hostel",
            "lodge", "place", "inn", "residence", "residency", "flat", "apartment",
            "the", "and", "for", "new", "sri", "with", "from", "near", "road",
            "lane", "street", "floor", "block", "city", "town", "area"
    );

    private Boarding findMatchingBoarding(String msg, List<Boarding> boardings) {
        for (Boarding b : boardings) {
            if (b.getName() != null && !b.getName().trim().isEmpty()) {
                String nameLower = b.getName().toLowerCase();

                // 1. Full name match (strongest signal)
                if (msg.contains(nameLower)) return b;

                // 2. Require at least 2 significant (non-generic) words from the boarding
                //    name to appear in the message before claiming a specific match.
                String[] words = nameLower.split("\\s+");
                int significantMatchCount = 0;
                for (String w : words) {
                    if (w.length() > 3 && !GENERIC_NAME_WORDS.contains(w) && msg.contains(w)) {
                        significantMatchCount++;
                    }
                }
                if (significantMatchCount >= 2) return b;

                // 3. If the boarding name has only ONE significant non-generic word and it
                //    appears in the message, also match (e.g. a boarding simply named "Sunset").
                long totalSignificantWords = Arrays.stream(words)
                        .filter(w -> w.length() > 3 && !GENERIC_NAME_WORDS.contains(w))
                        .count();
                if (totalSignificantWords == 1 && significantMatchCount == 1) return b;
            }
        }
        return null;
    }

    private BigDecimal extractMaxRent(String text) {
        Pattern pK = Pattern.compile("(\\d+)\\s*k");
        Matcher mK = pK.matcher(text);
        if (mK.find()) {
            return new BigDecimal(mK.group(1)).multiply(new BigDecimal(1000));
        }

        Pattern pNum = Pattern.compile("(under|below|less than|max|budget)?\\s*(\\d{4,6})");
        Matcher mNum = pNum.matcher(text);
        if (mNum.find()) {
            return new BigDecimal(mNum.group(2));
        }
        return null;
    }

    private String extractLocationName(String text) {
        List<String> knownLocations = List.of(
                "colombo", "kandy", "galle", "matara", "kurunegala", "gampaha", "jaffna",
                "panadura", "moratuwa", "kelaniya", "malabe", "nugegoda", "battaramulla",
                "maharagama", "horana", "negombo", "ratnapura", "badulla"
        );
        for (String loc : knownLocations) {
            if (text.contains(loc)) return loc;
        }
        return null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
