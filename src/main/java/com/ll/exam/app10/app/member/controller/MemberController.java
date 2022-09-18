package com.ll.exam.app10.app.member.controller;


import com.ll.exam.app10.app.member.Service.MemberService;
import com.ll.exam.app10.app.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    @GetMapping("/join")
    public String showJoin(){
        return "/member/join";
    }

    @PostMapping("/join")
    public String join(String username, String password, String email, MultipartFile profileImg, HttpSession session){
       Member oldmember = memberService.getMemberByUserName(username);
        if(oldmember != null){
            return "redirect:/?errorMsg=Already done.";
        }

        Member member = memberService.join(username, "{noop}" + password, email, profileImg);
        session.setAttribute("loginedMemberId",member.getId());

        return "redirect:/member/profile";
    }
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model){
       Long loginedMemberId = (Long)session.getAttribute("loginedMemberId");
        boolean isLogined = loginedMemberId != null;

        if(isLogined == false ){
            return "redirect:/?errorMsg=Need to login!";
        }
        Member loginedMember = memberService.getMemberById(loginedMemberId);
        model.addAttribute("loginedMember",loginedMember);
        return "member/profile";
    }

}
