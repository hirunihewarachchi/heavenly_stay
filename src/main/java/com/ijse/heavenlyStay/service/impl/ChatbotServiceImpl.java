package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.ChatRequestDTO;
import com.ijse.heavenlyStay.dto.ChatResponseDTO;
import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.RoomCategory;
import com.ijse.heavenlyStay.repository.BoardingRepository;
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

    private static final Set<String> OUT_OF_SCOPE_KEYWORDS = Set.of(
            "python", "java code", "weather", "recipe", "who is", "president", "capital of",
            "movie", "football", "cricket", "calculator", "joke", "story", "song", "lyrics",
            "politics", "stock market", "crypto", "bitcoin", "translate", "write an essay"
    );

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

            // 1. Out-of-Scope check
            boolean isOutOfScope = OUT_OF_SCOPE_KEYWORDS.stream().anyMatch(msg::contains);
            boolean containsDomainTerm = msg.contains("boarding") || msg.contains("room") || msg.contains("stay") ||
                    msg.contains("rent") || msg.contains("heavenly") || msg.contains("key money") ||
                    msg.contains("location") || msg.contains("colombo") || msg.contains("kandy") ||
                    msg.contains("galle") || msg.contains("bed") || msg.contains("hostel") || msg.contains("place") ||
                    msg.contains("fee") || msg.contains("publish") || msg.contains("deposit");

            if (isOutOfScope && !containsDomainTerm) {
                return new ChatResponseDTO(
                        "I can only answer questions related to HeavenlyStay boardings, pricing, location, availability, and booking rules from our database.",
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

            // 8. General Search (Location / Budget / Room Type filtering)
            BigDecimal maxRent = extractMaxRent(msg);

            final RoomCategory reqCategory;
            if (msg.contains("single")) {
                reqCategory = RoomCategory.SINGLE;
            } else if (msg.contains("shared") || msg.contains("sharing") || msg.contains("bed")) {
                reqCategory = RoomCategory.SHARED;
            } else {
                reqCategory = null;
            }

            final BigDecimal maxBudget = maxRent;
            final String targetLoc = extractLocationName(msg);

            List<Boarding> filtered = allApproved.stream().filter(b -> {
                if (maxBudget != null && b.getMonthlyRent() != null) {
                    if (b.getMonthlyRent().compareTo(maxBudget) > 0) return false;
                }
                if (reqCategory != null && b.getRoomCategory() != reqCategory) {
                    return false;
                }
                if (targetLoc != null) {
                    boolean matchDistrict = b.getDistrict() != null && b.getDistrict().toLowerCase().contains(targetLoc);
                    boolean matchProvince = b.getProvince() != null && b.getProvince().toLowerCase().contains(targetLoc);
                    boolean matchAddress = b.getAddress() != null && b.getAddress().toLowerCase().contains(targetLoc);
                    boolean matchName = b.getName() != null && b.getName().toLowerCase().contains(targetLoc);
                    if (!matchDistrict && !matchProvince && !matchAddress && !matchName) return false;
                }
                return true;
            }).collect(Collectors.toList());

            List<BoardingDTO> dtoList = filtered.stream()
                    .map(b -> boardingService.getBoardingById(b.getBoardingId()))
                    .toList();

            if (dtoList.isEmpty()) {
                StringBuilder noMatch = new StringBuilder("No approved boardings found");
                if (targetLoc != null) noMatch.append(" in **").append(capitalize(targetLoc)).append("**");
                if (maxRent != null) noMatch.append(" under **LKR ").append(maxRent.toPlainString()).append("**");
                noMatch.append(" in our database.");
                return new ChatResponseDTO(noMatch.toString(), "BOARDING_SEARCH", defaultPills, Collections.emptyList());
            }

            StringBuilder reply = new StringBuilder();
            if (targetLoc != null && maxRent != null) {
                reply.append("Found **").append(dtoList.size()).append(" boarding(s)** in **").append(capitalize(targetLoc)).append("** under **LKR ").append(maxRent.toPlainString()).append("**:");
            } else if (targetLoc != null) {
                reply.append("Found **").append(dtoList.size()).append(" boarding(s)** in **").append(capitalize(targetLoc)).append("**:");
            } else if (maxRent != null) {
                reply.append("Found **").append(dtoList.size()).append(" boarding(s)** under **LKR ").append(maxRent.toPlainString()).append("**:");
            } else {
                reply.append("Found **").append(dtoList.size()).append(" boarding(s)** matching your query:");
            }

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
