package com.nilesh.cym.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class PageViewController {

    @GetMapping({"/", "/login", "/login/"})
    public String login(Model model) {
        populate(model, "Call Your Mechanic", "login", "public");
        return "pages/login";
    }

    @GetMapping({"/profile/setup", "/profile/setup/"})
    public String profileSetup(Model model) {
        populate(model, "Complete Profile", "profile-setup", "public");
        return "pages/profile-setup";
    }

    @GetMapping({"/app/user/home", "/app/user/home/"})
    public String userHome(Model model) {
        populate(model, "User Home", "user-home", "user");
        return "pages/user-home";
    }

    @GetMapping({"/app/user/nearby", "/app/user/nearby/"})
    public String nearbyMechanics(Model model) {
        populate(model, "Nearby Mechanics", "user-nearby", "user");
        return "pages/user-nearby";
    }

    @GetMapping({"/app/user/booking", "/app/user/booking/"})
    public String bookingConfirm(Model model) {
        populate(model, "Confirm Booking", "user-booking", "user");
        return "pages/user-booking";
    }

    @GetMapping({"/app/user/tracking", "/app/user/tracking/"})
    public String liveTracking(Model model) {
        populate(model, "Live Tracking", "user-tracking", "user");
        return "pages/user-tracking";
    }

    @GetMapping("/app/user/tracking/{bookingId}")
    public String liveTracking(@PathVariable Long bookingId, Model model) {
        populate(model, "Live Tracking", "user-tracking", "user");
        model.addAttribute("bookingId", bookingId);
        return "pages/user-tracking";
    }

    @GetMapping({"/app/user/vehicles", "/app/user/vehicles/"})
    public String vehicles(Model model) {
        populate(model, "My Vehicles", "user-vehicles", "user");
        return "pages/user-vehicles";
    }

    @GetMapping({"/app/user/profile", "/app/user/profile/"})
    public String userProfile(Model model) {
        populate(model, "Profile", "user-profile", "user");
        return "pages/user-profile";
    }

    @GetMapping("/app/user/rating/{bookingId}")
    public String userRating(@PathVariable Long bookingId, Model model) {
        populate(model, "Rate Mechanic", "user-rating", "user");
        model.addAttribute("bookingId", bookingId);
        return "pages/user-rating";
    }

    @GetMapping({"/app/mechanic/register", "/app/mechanic/register/"})
    public String mechanicRegister(Model model) {
        populate(model, "Become a Mechanic", "mechanic-register", "mechanic");
        return "pages/mechanic-register";
    }

    @GetMapping({"/app/mechanic/dashboard", "/app/mechanic/dashboard/"})
    public String mechanicDashboard(Model model) {
        populate(model, "Mechanic Dashboard", "mechanic-dashboard", "mechanic");
        return "pages/mechanic-dashboard";
    }

    @GetMapping({"/app/mechanic/requests", "/app/mechanic/requests/"})
    public String incomingRequests(Model model) {
        populate(model, "Incoming Requests", "mechanic-requests", "mechanic");
        return "pages/mechanic-requests";
    }

    @GetMapping({"/app/mechanic/job", "/app/mechanic/job/"})
    public String activeJob(Model model) {
        populate(model, "Active Job", "mechanic-job", "mechanic");
        return "pages/mechanic-job";
    }

    @GetMapping({"/app/mechanic/earnings", "/app/mechanic/earnings/"})
    public String earnings(Model model) {
        populate(model, "Earnings", "mechanic-earnings", "mechanic");
        return "pages/mechanic-earnings";
    }

    @GetMapping({"/app/mechanic/profile", "/app/mechanic/profile/"})
    public String mechanicProfile(Model model) {
        populate(model, "Mechanic Profile", "mechanic-profile", "mechanic");
        return "pages/mechanic-profile";
    }

    private void populate(Model model, String title, String pageName, String pageRole) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageName", pageName);
        model.addAttribute("pageRole", pageRole);
    }
}
