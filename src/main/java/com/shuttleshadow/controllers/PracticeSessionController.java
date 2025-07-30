package com.shuttleshadow.controllers;

import com.shuttleshadow.dto.ZonePerformanceDTO;
import com.shuttleshadow.entities.Mode;
import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Users;
import com.shuttleshadow.entities.Zone;
import com.shuttleshadow.services.PracticeSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PracticeSessionController {

    @Autowired
    private PracticeSessionService sessionService;

    @GetMapping("/session/start")
    public String sessionOptions(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        return "session_mode";
    }

   @PostMapping("/session/start")
public String startSession(@RequestParam("mode") String mode,  // Accept as String instead of Enum
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
    Users user = (Users) session.getAttribute("user");
    if (user == null) return "redirect:/login";

    try {
        // Convert mode to uppercase for consistency
        String normalizedMode = mode.toUpperCase();
        
        // Validate mode (either "MEDIUM" or "IMPROVE")
        if (!"MEDIUM".equals(normalizedMode) && !"IMPROVE".equals(normalizedMode)) {
            redirectAttributes.addFlashAttribute("error", "Invalid session mode");
            return "redirect:/dashboard";
        }

        // Convert to Enum (if still needed for service layer)
        Mode sessionMode = Mode.valueOf(normalizedMode);
        
        PracticeSession newSession = sessionService.startSession(user, sessionMode);
        session.setAttribute("currentSessionId", newSession.getId());
        session.setAttribute("sessionMode", normalizedMode); // Store as uppercase String
        return "redirect:/session/run";
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("error", "Invalid session mode");
        return "redirect:/dashboard";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Failed to start session");
        return "redirect:/dashboard";
    }
}

    @GetMapping("/session/run")
    public String runSessionPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Long sessionId = (Long) session.getAttribute("currentSessionId");
        String mode = (String) session.getAttribute("sessionMode");

        if (sessionId == null || mode == null) {
            // Clear any broken session state
            session.removeAttribute("currentSessionId");
            session.removeAttribute("sessionMode");
            redirectAttributes.addFlashAttribute("error", "No active session found");
            return "redirect:/dashboard";
        }

        try {
            PracticeSession sessionEntity = sessionService.getSessionById(sessionId);
            if (sessionEntity == null || !sessionEntity.getUser().getId().equals(user.getId())) {
                session.removeAttribute("currentSessionId");
                session.removeAttribute("sessionMode");
                redirectAttributes.addFlashAttribute("error", "Invalid session");
                return "redirect:/dashboard";
            }

            model.addAttribute("mode", mode);
            model.addAttribute("sessionId", sessionId);
            return "session_run";
        } catch (Exception e) {
            // Clear broken session state on error
            session.removeAttribute("currentSessionId");
            session.removeAttribute("sessionMode");
            redirectAttributes.addFlashAttribute("error", "Session error: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/session/force-new")
    public String forceNewSession(HttpSession session, RedirectAttributes redirectAttributes) {
        // Clear any existing session state
        session.removeAttribute("currentSessionId");
        session.removeAttribute("sessionMode");
        redirectAttributes.addFlashAttribute("info", "You can now start a new session");
        return "redirect:/session/start";
    }


    @PostMapping("/session/cancel")
    public String cancelSession(HttpSession session, RedirectAttributes redirectAttributes) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Long sessionId = (Long) session.getAttribute("currentSessionId");
        if (sessionId != null) {
            try {
                sessionService.endSession(sessionId);
                redirectAttributes.addFlashAttribute("success", "Session cancelled");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to cancel session");
            }
        }
        session.removeAttribute("currentSessionId");
        session.removeAttribute("sessionMode");
        return "redirect:/dashboard";
    }

    @PostMapping("/session/submit")
    @ResponseBody
    public ResponseEntity<String> submitSessionResults(
            @RequestBody Map<String, Object> requestData,
            HttpSession session) {
        try {
            // Log entire request for debugging
            System.out.println("Received session submission: " + requestData);

            if (!requestData.containsKey("sessionId") || !requestData.containsKey("zonePerformances")) {
                return ResponseEntity.badRequest().body("Missing sessionId or zonePerformances");
            }

            Long sessionId = Long.parseLong(requestData.get("sessionId").toString());
            List<ZonePerformanceDTO> results = new ArrayList<>();

            List<?> rawList = (List<?>) requestData.get("zonePerformances");

            for (Object obj : rawList) {
                if (!(obj instanceof Map)) {
                    System.out.println("Invalid entry, not a map: " + obj);
                    continue;
                }

                Map<?, ?> perf = (Map<?, ?>) obj;

                Object zoneVal = perf.get("zone");
                Object avgRtVal = perf.get("averageReactionTime");
                Object hitsVal = perf.get("hits");

                if (zoneVal == null || avgRtVal == null || hitsVal == null) {
                    System.out.println("Skipping malformed entry: " + perf);
                    continue;
                }

                String zoneStr = zoneVal.toString();
                double avgReactionTime;
                int hits;

                try {
                    avgReactionTime = ((Number) avgRtVal).doubleValue();
                    hits = ((Number) hitsVal).intValue();
                } catch (Exception ex) {
                    System.out.println("Skipping entry due to invalid number types: " + perf);
                    continue;
                }

                try {
                    Zone zoneEnum = Zone.valueOf(zoneStr);
                    results.add(new ZonePerformanceDTO(zoneEnum, avgReactionTime, hits));
                } catch (IllegalArgumentException iae) {
                    System.out.println("Invalid zone: " + zoneStr);
                    continue;
                }
            }

            if (results.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid performance data submitted.");
            }

            sessionService.saveSessionResults(sessionId, results);

            session.removeAttribute("currentSessionId");
            session.removeAttribute("sessionMode");

            return ResponseEntity.ok("Session saved successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error submitting session: " + e.getMessage());
        }
    }


    public static class SessionSubmission {
        private Long sessionId;
        private List<ZonePerformanceDTO> zonePerformances;

        // Getters and setters
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public List<ZonePerformanceDTO> getZonePerformances() { return zonePerformances; }
        public void setZonePerformances(List<ZonePerformanceDTO> zonePerformances) {
            this.zonePerformances = zonePerformances;
        }
    }
}
