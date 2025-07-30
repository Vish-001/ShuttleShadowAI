package com.shuttleshadow.controllers;

import com.shuttleshadow.dto.LoginDTO;
import com.shuttleshadow.entities.Users;
import com.shuttleshadow.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginDTO());
        }
        return "login"; // NOT redirect here — render view
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginDTO") LoginDTO loginDto, // Fixed attribute name
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            session.removeAttribute("currentSessionId");
            session.removeAttribute("sessionMode");

            Users user = userService.findByUsername(loginDto.getUsername());

            if (user != null && userService.authenticate(loginDto.getUsername(), loginDto.getPassword()) != null) {
                session.setAttribute("user", user);
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login error. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/error")
    public String handleError() {
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") Users user) {
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
