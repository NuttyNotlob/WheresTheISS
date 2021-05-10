package com.abolton.WheresTheISS.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/")
    public String homeMethod(Model model) {
        // Add the data taken from the SteamSpy API
        // model.addAttribute("steamSpyGames", steamSpyDataGrabberService.getSteamSpyGames());
        return "home";
    }
}
