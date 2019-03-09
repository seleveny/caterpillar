package com.amc.controller;

import com.amc.utils.GenerateGene;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class showController {

    @RequestMapping("/show")
    private String show(HttpServletRequest httpServletRequest){
        String code = httpServletRequest.getParameter("code");
        return "<img src='data:image/gif;base64,'" + GenerateGene.createImage(code) +"'></img>";
    }
}
