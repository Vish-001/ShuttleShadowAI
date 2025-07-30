package com.shuttleshadow.controllers;

import com.shuttleshadow.dto.SessionDTO;
import com.shuttleshadow.dto.ZonePerformanceDTO;
import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Users;
import com.shuttleshadow.entities.Zone;
import com.shuttleshadow.entities.ZonePerformance;
import com.shuttleshadow.services.PracticeSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private PracticeSessionService sessionService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) Boolean success) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            List<PracticeSession> sessions = sessionService.getSessionsByUser(user);

            List<SessionDTO> sessionDTOs = sessions.stream()
                    .map(s -> {
                        // Convert performances to DTOs
                        List<ZonePerformanceDTO> zpDTOs = s.getZonePerformances().stream()
                                .map(zp -> new ZonePerformanceDTO(
                                        zp.getZone(),
                                        zp.getAverageReactionTime(),
                                        zp.getHits()
                                ))
                                .collect(Collectors.toList());

                        // Find weakest zone (max reaction time)
                        ZonePerformance weakestPerformance = s.getZonePerformances().stream()
                                .max(Comparator.comparingDouble(ZonePerformance::getAverageReactionTime))
                                .orElse(null);

                        Zone weakestZone = (weakestPerformance != null) ? weakestPerformance.getZone() : null;

                        // Use constructor with weakestZone
                        return new SessionDTO(
                                s.getId(),
                                s.getMode().name(),
                                s.getStartTime(),
                                s.getEndTime(),
                                zpDTOs,
                                weakestZone
                        );
                    }).collect(Collectors.toList());

            model.addAttribute("username", user.getUsername());
            model.addAttribute("sessions", sessionDTOs);

            Long currentSessionId = (Long) session.getAttribute("currentSessionId");
            if (currentSessionId != null) {
                PracticeSession activeSession = sessionService.getSessionById(currentSessionId);
                if (activeSession != null && activeSession.getEndTime() == null) {
                    model.addAttribute("activeSession", activeSession);
                } else {
                    session.removeAttribute("currentSessionId");
                    session.removeAttribute("sessionMode");
                }
            }

            if (Boolean.TRUE.equals(success)) {
                model.addAttribute("successMessage", "Session completed successfully!");
            }

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboard";
        }
    }

    @PostMapping("/session/{id}/delete")
    public String deleteSession(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            Long currentSessionId = (Long) session.getAttribute("currentSessionId");
            if (currentSessionId != null && currentSessionId.equals(id)) {
                session.removeAttribute("currentSessionId");
                session.removeAttribute("sessionMode");
            }

            sessionService.deleteSessionById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Session deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting session: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/session/{id}/details")
    public String sessionDetails(@PathVariable Long id, Model model, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        PracticeSession ps = sessionService.getSessionById(id);
        if (!ps.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }

        // Convert to DTOs
        List<ZonePerformanceDTO> zpDTOs = ps.getZonePerformances().stream()
                .map(zp -> new ZonePerformanceDTO(zp.getZone(), zp.getAverageReactionTime(), zp.getHits()))
                .toList();

        System.out.println("ZONE DATA FOR SESSION " + id);
        zpDTOs.forEach(zp -> System.out.println(zp.getZone() + " - " + zp.getAverageReactionTime()));

        model.addAttribute("session", ps);
        model.addAttribute("zonePerformances", zpDTOs);  // 🟢 Important!

        return "session_details";
    }

}
